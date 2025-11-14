package ar.edu.utn.dds.k3003.busqueda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResultDTO<T> {

    private List<T> resultados;

    private Integer page;

    private Integer size;

    private Long totalResultados;

    private Integer totalPaginas;

    private Boolean hasNext;

    private Boolean hasPrevious;

    // Metadatos de la búsqueda
    private String query;

    private List<String> tags;

    private Long tiempoRespuestaMs;
}