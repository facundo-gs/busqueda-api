package ar.edu.utn.dds.k3003.busqueda.service;

import ar.edu.utn.dds.k3003.busqueda.dto.BusquedaRequestDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.BusquedaResponseDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.BusquedaResultadoDTO;
import ar.edu.utn.dds.k3003.busqueda.model.HechoIndexado;
import ar.edu.utn.dds.k3003.busqueda.repository.HechoIndexadoRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BusquedaService {

    public static final String RESULTADO = "resultado";
    private final HechoIndexadoRepository repository;
    private final MeterRegistry meterRegistry;

    public BusquedaResponseDTO buscar(BusquedaRequestDTO request) {
        long startNanos = System.nanoTime();
        String tipoConsulta = (request.tags() != null && !request.tags().isEmpty())
                ? "con_tags" : "sin_tags";

        log.info("🔍 Búsqueda: '{}', tags: {}, página: {}",
                request.consulta(), request.tags(), request.pagina());

        try {
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

            BusquedaResponseDTO response = BusquedaResponseDTO.of(
                    resultadosDeduplicados,
                    request.pagina(),
                    request.tamanio(),
                    resultadosPage.getTotalElements()
            );

            // Registrar métricas de éxito
            registrarMetricasBusqueda(request, response, "ok", tipoConsulta, startNanos);

            return response;

        } catch (Exception e) {
            log.error("❌ Error en búsqueda: {}", e.getMessage(), e);
            registrarMetricasError(tipoConsulta, startNanos);
            throw e;
        }
    }

    private void registrarMetricasBusqueda(BusquedaRequestDTO request,
                                           BusquedaResponseDTO response,
                                           String resultado,
                                           String tipoConsulta,
                                           long startNanos) {
        long duracionNanos = System.nanoTime() - startNanos;

        // Contador de búsquedas realizadas
        meterRegistry.counter(
                "metamapa.busqueda.consultas",
                RESULTADO, resultado,
                "tipo", tipoConsulta,
                "tiene_resultados", response.totalResultados() > 0 ? "si" : "no"
        ).increment();

        // Tiempo de respuesta de búsqueda
        meterRegistry.timer(
                "metamapa.busqueda.latencia",
                RESULTADO, resultado,
                "tipo", tipoConsulta
        ).record(duracionNanos, TimeUnit.NANOSECONDS);

        // Histograma de cantidad de resultados
        meterRegistry.summary("metamapa.busqueda.cantidad_resultados")
                .record(response.totalResultados());
    }

    private void registrarMetricasError(String tipoConsulta, long startNanos) {
        long duracionNanos = System.nanoTime() - startNanos;

        meterRegistry.counter(
                "metamapa.busqueda.consultas",
                RESULTADO, "error",
                "tipo", tipoConsulta,
                "tiene_resultados", "no"
        ).increment();

        meterRegistry.timer(
                "metamapa.busqueda.latencia",
                RESULTADO, "error",
                "tipo", tipoConsulta
        ).record(duracionNanos, TimeUnit.NANOSECONDS);
    }

    private List<BusquedaResultadoDTO> deduplicarPorTitulo(List<HechoIndexado> hechos) {
        Map<String, HechoIndexado> unicos = new LinkedHashMap<>();

        for (HechoIndexado hecho : hechos) {
            String titulo = hecho.getTitulo();
            HechoIndexado existente = unicos.get(titulo);

            if (existente == null ||
                    (hecho.getUltimaActualizacion() != null &&
                            existente.getUltimaActualizacion() != null &&
                            hecho.getUltimaActualizacion().isAfter(existente.getUltimaActualizacion()))) {
                unicos.put(titulo, hecho);
            }
        }

        return unicos.values().stream()
                .map(BusquedaResultadoDTO::from)
                .collect(Collectors.toList());
    }
}