package ar.edu.utn.dds.k3003.busqueda.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDTO {

    private String query;  // Texto a buscar (puede ser null para buscar solo por tags)

    @Builder.Default
    private List<String> tags = new ArrayList<>();  // Filtros por etiquetas (AND)

    private String coleccion;  // Filtro por colección (opcional)

    @Min(0)
    @Builder.Default
    private Integer page = 0;

    @Min(1)
    @Max(50)
    @Builder.Default
    private Integer size = 10;

    // Para ordenamiento
    @Builder.Default
    private String sortBy = "relevancia";  // relevancia, fecha, titulo

    @Builder.Default
    private String sortDirection = "desc";  // asc, desc
}