package ar.edu.utn.dds.k3003.busqueda.dto;

import java.util.List;

/**
 * DTO para respuesta paginada de búsqueda.
 */
public record BusquedaResponseDTO(
        List<BusquedaResultadoDTO> resultados,
        int paginaActual,
        int tamanio,
        long totalResultados,
        int totalPaginas,
        boolean tieneSiguiente,
        boolean tieneAnterior
) {
    /**
     * Crea una respuesta paginada.
     */
    public static BusquedaResponseDTO of(
            List<BusquedaResultadoDTO> resultados,
            int pagina,
            int tamanio,
            long total
    ) {
        int totalPaginas = (int) Math.ceil((double) total / tamanio);
        return new BusquedaResponseDTO(
                resultados,
                pagina,
                tamanio,
                total,
                totalPaginas,
                pagina < totalPaginas - 1,
                pagina > 0
        );
    }
}