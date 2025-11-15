package ar.edu.utn.dds.k3003.busqueda.service;

import ar.edu.utn.dds.k3003.busqueda.dto.ColeccionDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.HechoDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.PdIDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@Slf4j
public class SyncScheduler {

    private final IndexacionService indexacionService;
    private final RestClient restClient;

    @Value("${modules.fuente.url}")
    private String fuenteUrl;

    @Value("${modules.pdi.url}")
    private String pdiUrl;

    @Value("${busqueda.sync.enabled}")
    private boolean syncEnabled;

    public SyncScheduler(IndexacionService indexacionService,
                         RestClient.Builder restClientBuilder) {
        this.indexacionService = indexacionService;
        this.restClient = restClientBuilder.build();
    }

    /**
     * Sincronización inicial al arrancar
     */
    @EventListener(ApplicationReadyEvent.class)
    public void sincronizacionInicial() {
        if (!syncEnabled) {
            log.info("Sincronización inicial deshabilitada");
            return;
        }

        log.info("🔄 Iniciando sincronización inicial...");

        try {
            // Sincronizar hechos de todas las fuentes conocidas
            sincronizarHechos();

            // Sincronizar PDIs
            sincronizarPdIs();

            log.info("✅ Sincronización inicial completada");
        } catch (Exception e) {
            log.error("❌ Error en sincronización inicial: {}", e.getMessage(), e);
        }
    }

    /**
     * Sincronización periódica
     */
    @Scheduled(
            initialDelayString = "${busqueda.sync.initial-delay}",
            fixedDelayString = "${busqueda.sync.fixed-delay}"
    )
    public void sincronizacionPeriodica() {
        if (!syncEnabled) {
            return;
        }

        log.info("🔄 Sincronización periódica iniciada...");

        try {
            sincronizarHechos();
            sincronizarPdIs();

            log.info("✅ Sincronización periódica completada");
        } catch (Exception e) {
            log.error("❌ Error en sincronización periódica: {}", e.getMessage(), e);
        }
    }

    private void sincronizarHechos() {
        try {
            // Obtener todas las colecciones
            List<String> colecciones = obtenerColecciones();

            for (String coleccion : colecciones) {
                log.info("📥 Sincronizando hechos de colección: {}", coleccion);

                List<HechoDTO> hechos = obtenerHechosDeColeccion(coleccion);

                for (HechoDTO hecho : hechos) {
                    try {
                        indexacionService.indexarHecho(hecho);
                    } catch (Exception e) {
                        log.error("❌ Error indexando hecho {}: {}", hecho.id(), e.getMessage());
                    }
                }

                log.info("✅ {} hechos sincronizados de colección {}", hechos.size(), coleccion);
            }
        } catch (Exception e) {
            log.error("❌ Error sincronizando hechos: {}", e.getMessage(), e);
        }
    }

    private void sincronizarPdIs() {
        try {
            log.info("📥 Sincronizando PDIs...");

            List<PdIDTO> pdis = restClient.get()
                    .uri(pdiUrl + "/api/PdIs")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PdIDTO>>() {});

            if (pdis != null) {
                for (PdIDTO pdi : pdis) {
                    try {
                        indexacionService.indexarPdI(pdi);
                    } catch (Exception e) {
                        log.error("❌ Error indexando PDI {}: {}", pdi.id(), e.getMessage());
                    }
                }

                log.info("✅ {} PDIs sincronizados", pdis.size());
            }
        } catch (Exception e) {
            log.error("❌ Error sincronizando PDIs: {}", e.getMessage(), e);
        }
    }

    private List<String> obtenerColecciones() {
        try {
            // Obtener colecciones reales desde el módulo Fuente
            List<ColeccionDTO> colecciones = restClient.get()
                    .uri(fuenteUrl + "/api/colecciones")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ColeccionDTO>>() {});

            if (colecciones != null) {
                return colecciones.stream()
                        .map(ColeccionDTO::nombre)
                        .toList();
            }
            return List.of();
        } catch (Exception e) {
            log.error("Error obteniendo colecciones: {}", e.getMessage());
            return List.of();
        }
    }

    private List<HechoDTO> obtenerHechosDeColeccion(String coleccion) {
        return restClient.get()
                .uri(fuenteUrl + "/api/colecciones/" + coleccion + "/hechos")
                .retrieve()
                .body(new ParameterizedTypeReference<List<HechoDTO>>() {});
    }
}