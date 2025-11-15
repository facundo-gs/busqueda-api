package ar.edu.utn.dds.k3003.busqueda.dto;

import java.util.List;

/**
 * DTO para solicitud de búsqueda.
 */
public record BusquedaRequestDTO(
        String consulta,
        List<String> tags,
        int pagina,
        int tamanio
) {
    public BusquedaRequestDTO {
        if (consulta == null || consulta.isBlank()) {
            throw new IllegalArgumentException("La consulta no puede estar vacía");
        }
        if (pagina < 0) pagina = 0;
        if (tamanio <= 0) tamanio = 10;
        if (tamanio > 50) tamanio = 50;
    }
}