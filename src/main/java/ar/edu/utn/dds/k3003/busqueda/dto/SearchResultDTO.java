package ar.edu.utn.dds.k3003.busqueda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO {

    private String hechoId;
    private String titulo;
    private String descripcion;
    private String nombreColeccion;
    private List<String> colecciones;  // Si aparece en múltiples
    private String ubicacion;
    private String categoria;
    private LocalDateTime fecha;
    private List<String> etiquetas;

    // Información de PDIs
    private Integer cantidadPdis;
    private Boolean tieneImagenes;

    // Score de relevancia
    private Double score;

    // Fragmentos destacados (para highlighting)
    private List<String> fragmentos;
}