package ar.edu.utn.dds.k3003.busqueda.events;

import ar.edu.utn.dds.k3003.busqueda.dto.HechoDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class HechoCreatedEvent extends ApplicationEvent {

    private final HechoDTO hecho;

    public HechoCreatedEvent(Object source, HechoDTO hecho) {
        super(source);
        this.hecho = hecho;
    }
}