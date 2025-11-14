package ar.edu.utn.dds.k3003.busqueda.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SolicitudAceptadaEvent extends ApplicationEvent {

    private final String hechoId;
    private final String solicitudId;

    public SolicitudAceptadaEvent(Object source, String hechoId, String solicitudId) {
        super(source);
        this.hechoId = hechoId;
        this.solicitudId = solicitudId;
    }
}