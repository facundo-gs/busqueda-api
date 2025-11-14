package ar.edu.utn.dds.k3003.busqueda.listener;

import ar.edu.utn.dds.k3003.busqueda.events.HechoCreatedEvent;
import ar.edu.utn.dds.k3003.busqueda.events.PdICreatedEvent;
import ar.edu.utn.dds.k3003.busqueda.events.SolicitudAceptadaEvent;
import ar.edu.utn.dds.k3003.busqueda.service.IndexacionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IndexacionEventListener {

    private final IndexacionService indexacionService;

    public IndexacionEventListener(IndexacionService indexacionService) {
        this.indexacionService = indexacionService;
    }

    /**
     * Escucha eventos de creación de hechos
     */
    @EventListener
    @Async
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void onHechoCreated(HechoCreatedEvent event) {
        log.info("🎧 Evento recibido: HechoCreatedEvent id={}", event.getHecho().id());

        try {
            indexacionService.indexarHecho(event.getHecho());
        } catch (Exception e) {
            log.error("❌ Error procesando HechoCreatedEvent: {}", e.getMessage(), e);
            throw e;  // Para que @Retryable funcione
        }
    }

    /**
     * Escucha eventos de creación de PDIs
     */
    @EventListener
    @Async
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void onPdICreated(PdICreatedEvent event) {
        log.info("🎧 Evento recibido: PdICreatedEvent id={}, hechoId={}",
                event.getPdi().id(), event.getPdi().hechoId());

        try {
            indexacionService.indexarPdI(event.getPdi());
        } catch (Exception e) {
            log.error("❌ Error procesando PdICreatedEvent: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Escucha eventos de solicitudes aceptadas (censura)
     */
    @EventListener
    @Async
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void onSolicitudAceptada(SolicitudAceptadaEvent event) {
        log.info("🎧 Evento recibido: SolicitudAceptadaEvent hechoId={}, solicitudId={}",
                event.getHechoId(), event.getSolicitudId());

        try {
            indexacionService.censurarHecho(event.getHechoId(), event.getSolicitudId());
        } catch (Exception e) {
            log.error("❌ Error procesando SolicitudAceptadaEvent: {}", e.getMessage(), e);
            throw e;
        }
    }
}