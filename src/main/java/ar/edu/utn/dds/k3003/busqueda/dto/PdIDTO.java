package ar.edu.utn.dds.k3003.busqueda.dto;

import ar.edu.utn.dds.k3003.busqueda.enums.EstadoProcesamiento;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PdIDTO(
        String id,
        String hechoId,
        String descripcion,
        String lugar,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime momento,
        String contenido,
        String imagenUrl,
        String ocrText,
        List<String> etiquetasIA,
        @Deprecated
        List<String> etiquetas,
        EstadoProcesamiento estadoProcesamiento,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime fechaProcesamiento
) {

    // Constructor para crear PDI sin imagen (compatibilidad hacia atrás)
    public PdIDTO(String id, String hechoId, String descripcion, String lugar,
                  LocalDateTime momento, String contenido, List<String> etiquetas) {
        this(id, hechoId, descripcion, lugar, momento, contenido, null, null, null, etiquetas, null, null);
    }

    // Constructor simplificado para PDI con imagen
    public PdIDTO(String id, String hechoId, String descripcion, String lugar,
                  LocalDateTime momento, String contenido, String imagenUrl) {
        this(id, hechoId, descripcion, lugar, momento, contenido, imagenUrl, null, null, null, null, null);
    }

}
