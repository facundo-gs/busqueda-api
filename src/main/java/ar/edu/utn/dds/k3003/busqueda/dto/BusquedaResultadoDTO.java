package ar.edu.utn.dds.k3003.busqueda.dto;

import ar.edu.utn.dds.k3003.busqueda.model.HechoIndexado;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para un resultado individual de búsqueda.
 */
public record BusquedaResultadoDTO(
        String id,
        String titulo,
        String nombreColeccion,
        String descripcion,
        String ubicacion,
        String categoria,
        LocalDateTime fecha,
        List<String> etiquetas,
        List<String> etiquetasIA,
        String origen,
        double score
) {
    /**
     * Crea un DTO desde la entidad HechoIndexado.
     */
    public static BusquedaResultadoDTO from(HechoIndexado hecho) {
        return new BusquedaResultadoDTO(
                hecho.getId(),
                hecho.getTitulo(),
                hecho.getNombreColeccion(),
                hecho.getDescripcion(),
                hecho.getUbicacion(),
                hecho.getCategoria(),
                hecho.getFecha(),
                hecho.getEtiquetas(),
                hecho.getEtiquetasIA(),
                hecho.getOrigen(),
                0.0
        );
    }
}