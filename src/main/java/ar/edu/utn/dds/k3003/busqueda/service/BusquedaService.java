package ar.edu.utn.dds.k3003.busqueda.service;

import ar.edu.utn.dds.k3003.busqueda.dto.PageResultDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.SearchRequestDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.SearchResultDTO;
import ar.edu.utn.dds.k3003.busqueda.model.HechoIndexado;
import ar.edu.utn.dds.k3003.busqueda.repository.HechoIndexadoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BusquedaService {

    private final HechoIndexadoRepository repository;

    public BusquedaService(HechoIndexadoRepository repository) {
        this.repository = repository;
    }

    /**
     * Búsqueda principal con todos los filtros
     */
    public PageResultDTO<SearchResultDTO> buscar(SearchRequestDTO request) {
        long startTime = System.currentTimeMillis();

        log.info("🔍 Búsqueda iniciada: query='{}', tags={}, page={}, size={}",
                request.getQuery(), request.getTags(), request.getPage(), request.getSize());

        // Construir Pageable
        Pageable pageable = construirPageable(request);

        // Ejecutar búsqueda según parámetros
        Page<HechoIndexado> pageResult = ejecutarBusqueda(request, pageable);

        // Deduplicar por título
        List<HechoIndexado> deduplicados = deduplicarPorTitulo(pageResult.getContent());

        // Convertir a DTOs
        List<SearchResultDTO> resultados = deduplicados.stream()
                .map(this::toSearchResultDTO)
                .collect(Collectors.toList());

        long endTime = System.currentTimeMillis();
        long tiempoRespuesta = endTime - startTime;

        log.info("✅ Búsqueda completada: {} resultados en {}ms",
                resultados.size(), tiempoRespuesta);

        return PageResultDTO.<SearchResultDTO>builder()
                .resultados(resultados)
                .page(request.getPage())
                .size(request.getSize())
                .totalResultados(pageResult.getTotalElements())
                .totalPaginas(pageResult.getTotalPages())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .query(request.getQuery())
                .tags(request.getTags())
                .tiempoRespuestaMs(tiempoRespuesta)
                .build();
    }

    /**
     * Ejecuta la búsqueda según los parámetros
     */
    private Page<HechoIndexado> ejecutarBusqueda(SearchRequestDTO request, Pageable pageable) {
        boolean tieneQuery = request.getQuery() != null && !request.getQuery().trim().isEmpty();
        boolean tieneTags = request.getTags() != null && !request.getTags().isEmpty();
        boolean tieneColeccion = request.getColeccion() != null && !request.getColeccion().trim().isEmpty();

        if (tieneQuery && tieneTags) {
            log.debug("Búsqueda: texto + tags");
            return repository.searchByTextAndTags(request.getQuery(), request.getTags(), pageable);
        } else if (tieneQuery && tieneColeccion) {
            log.debug("Búsqueda: texto + colección");
            return repository.searchByTextAndColeccion(request.getQuery(), request.getColeccion(), pageable);
        } else if (tieneQuery) {
            log.debug("Búsqueda: solo texto");
            return repository.searchByText(request.getQuery(), pageable);
        } else if (tieneTags) {
            log.debug("Búsqueda: solo tags");
            return repository.searchByTags(request.getTags(), pageable);
        } else {
            log.debug("Búsqueda: todos los hechos");
            return repository.findAllNoCensurados(pageable);
        }
    }

    /**
     * Construye el Pageable con ordenamiento
     */
    private Pageable construirPageable(SearchRequestDTO request) {
        Sort sort;

        switch (request.getSortBy().toLowerCase()) {
            case "fecha":
                sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), "fecha");
                break;
            case "titulo":
                sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), "titulo");
                break;
            case "relevancia":
            default:
                // Para búsquedas full-text, MongoDB devuelve ordenado por score automáticamente
                sort = Sort.unsorted();
                break;
        }

        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    /**
     * Deduplicación por título (Req #6)
     * Si varios hechos tienen el mismo título, se queda con el primero
     */
    private List<HechoIndexado> deduplicarPorTitulo(List<HechoIndexado> hechos) {
        Map<String, HechoIndexado> unicos = hechos.stream()
                .collect(Collectors.toMap(
                        HechoIndexado::getTitulo,
                        h -> h,
                        (existente, nuevo) -> {
                            // Merge de colecciones
                            List<String> colecciones = new ArrayList<>(existente.getColecciones());
                            if (!colecciones.contains(nuevo.getNombreColeccion())) {
                                colecciones.add(nuevo.getNombreColeccion());
                            }
                            existente.setColecciones(colecciones);
                            return existente;
                        }
                ));

        return new ArrayList<>(unicos.values());
    }

    /**
     * Convierte HechoIndexado a SearchResultDTO
     */
    private SearchResultDTO toSearchResultDTO(HechoIndexado hecho) {
        return SearchResultDTO.builder()
                .hechoId(hecho.getHechoId())
                .titulo(hecho.getTitulo())
                .descripcion(hecho.getDescripcion())
                .nombreColeccion(hecho.getNombreColeccion())
                .colecciones(hecho.getColecciones())
                .ubicacion(hecho.getUbicacion())
                .categoria(hecho.getCategoria())
                .fecha(hecho.getFecha())
                .etiquetas(hecho.getEtiquetas())
                .cantidadPdis(hecho.getPdis() != null ? hecho.getPdis().size() : 0)
                .tieneImagenes(hecho.getPdis() != null &&
                        hecho.getPdis().stream().anyMatch(p -> p.getImagenUrl() != null))
                .score(null)  // MongoDB no expone score directamente en Java API
                .fragmentos(null)  // Para implementación futura de highlighting
                .build();
    }

    /**
     * Estadísticas de búsqueda
     */
    public Map<String, Object> obtenerEstadisticas() {
        long totalHechos = repository.count();
        long hechosnoCensurados = repository.countNoCensurados();
        long hechosCensurados = totalHechos - hechosnoCensurados;

        return Map.of(
                "totalHechosIndexados", totalHechos,
                "hechosBuscables", hechosnoCensurados,
                "hechosCensurados", hechosCensurados
        );
    }
}