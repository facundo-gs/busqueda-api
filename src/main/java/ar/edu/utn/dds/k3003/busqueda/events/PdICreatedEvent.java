package ar.edu.utn.dds.k3003.busqueda.events;

import ar.edu.utn.dds.k3003.busqueda.dto.PdIDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PdICreatedEvent extends ApplicationEvent {

    private final PdIDTO pdi;

    public PdICreatedEvent(Object source, PdIDTO pdi) {
        super(source);
        this.pdi = pdi;
    }
}