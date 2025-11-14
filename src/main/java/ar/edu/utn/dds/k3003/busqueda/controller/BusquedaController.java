package ar.edu.utn.dds.k3003.busqueda.controller;

import ar.edu.utn.dds.k3003.busqueda.dto.PageResultDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.SearchRequestDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.SearchResultDTO;
import ar.edu.utn.dds.k3003.busqueda.service.BusquedaService;
import ar.edu.utn.dds.k3003.busqueda.service.IndexacionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buscar")
@Slf4j
public class BusquedaController {

    private final BusquedaService busquedaService;
    private final IndexacionService indexacionService;

    public BusquedaController(BusquedaService busquedaService,
                              IndexacionService indexacionService) {
        this.busquedaService = busquedaService;
        this.indexacionService = indexacionService;
    }

    /**
     * Endpoint principal de búsqueda
     * GET /api/buscar?query=incendio&tag=CABA&tag=emergencia&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<PageResultDTO<SearchResultDTO>> buscar(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> tag,
            @RequestParam(required = false) String coleccion,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "relevancia") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        log.info("🔍 GET /api/buscar - query={}, tags={}, page={}, size={}",
                query, tag, page, size);

        SearchRequestDTO request = SearchRequestDTO.builder()
                .query(query)
                .tags(tag != null ? tag : List.of())
                .coleccion(coleccion)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        PageResultDTO<SearchResultDTO> resultado = busquedaService.buscar(request);

        return ResponseEntity.ok(resultado);
    }

    /**
     * Búsqueda con POST (para queries complejas)
     */
    @PostMapping
    public ResponseEntity<PageResultDTO<SearchResultDTO>> buscarPost(
            @Valid @RequestBody SearchRequestDTO request
    ) {
        log.info("🔍 POST /api/buscar - request={}", request);

        PageResultDTO<SearchResultDTO> resultado = busquedaService.buscar(request);

        return ResponseEntity.ok(resultado);
    }

    /**
     * Estadísticas del índice
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        log.info("📊 GET /api/buscar/stats");

        Map<String, Object> stats = busquedaService.obtenerEstadisticas();

        return ResponseEntity.ok(stats);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Búsqueda"
        ));
    }

    /**
     * Endpoint interno para reindexación completa
     */
    @PostMapping("/reindex")
    public ResponseEntity<Map<String, String>> reindexar() {
        log.warn("🔄 POST /api/buscar/reindex - Iniciando reindexación");

        try {
            indexacionService.reindexarTodo();
            return ResponseEntity.ok(Map.of(
                    "status", "OK",
                    "mensaje", "Reindexación iniciada"
            ));
        } catch (Exception e) {
            log.error("❌ Error en reindexación: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "ERROR",
                    "mensaje", e.getMessage()
            ));
        }
    }
}