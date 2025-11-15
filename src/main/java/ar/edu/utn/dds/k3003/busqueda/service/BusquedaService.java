package ar.edu.utn.dds.k3003.busqueda.service;

import ar.edu.utn.dds.k3003.busqueda.dto.BusquedaRequestDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.BusquedaResponseDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.BusquedaResultadoDTO;
import ar.edu.utn.dds.k3003.busqueda.model.HechoIndexado;
import ar.edu.utn.dds.k3003.busqueda.repository.HechoIndexadoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para búsqueda de hechos con soporte para paginación y filtros.
 */
@Service
@Slf4j
public class BusquedaService {

    private final HechoIndexadoRepository repository;

    public BusquedaService(HechoIndexadoRepository repository) {
        this.repository = repository;
    }

    /**
     * Búsqueda principal con soporte para paginación y filtros.
     */
    public BusquedaResponseDTO buscar(BusquedaRequestDTO request) {
        log.info("🔍 Búsqueda: '{}', tags: {}, página: {}",
                request.consulta(), request.tags(), request.pagina());

        Pageable pageable = PageRequest.of(
                request.pagina(),
                request.tamanio(),
                Sort.by(Sort.Direction.DESC, "ultimaActualizacion")
        );

        Page<HechoIndexado> resultadosPage;

        if (request.tags() != null && !request.tags().isEmpty()) {
            resultadosPage = repository.buscarPorTextoYTags(
                    request.consulta(),
                    request.tags(),
                    pageable
            );
        } else {
            resultadosPage = repository.buscarPorTexto(
                    request.consulta(),
                    pageable
            );
        }

        List<BusquedaResultadoDTO> resultadosDeduplicados = deduplicarPorTitulo(
                resultadosPage.getContent()
        );

        log.info("✅ Encontrados {} resultados (únicos: {})",
                resultadosPage.getTotalElements(),
                resultadosDeduplicados.size());

        return BusquedaResponseDTO.of(
                resultadosDeduplicados,
                request.pagina(),
                request.tamanio(),
                resultadosPage.getTotalElements()
        );
    }

    /**
     * Deduplicación: mantiene un solo resultado por título.
     * Criterio: el más reciente (ultimaActualizacion).
     */
    private List<BusquedaResultadoDTO> deduplicarPorTitulo(List<HechoIndexado> hechos) {
        Map<String, HechoIndexado> unicos = new LinkedHashMap<>();

        for (HechoIndexado hecho : hechos) {
            String titulo = hecho.getTitulo();
            HechoIndexado existente = unicos.get(titulo);

            if (existente == null ||
                    hecho.getUltimaActualizacion().isAfter(existente.getUltimaActualizacion())) {
                unicos.put(titulo, hecho);
            }
        }

        return unicos.values().stream()
                .map(BusquedaResultadoDTO::from)
                .collect(Collectors.toList());
    }
}