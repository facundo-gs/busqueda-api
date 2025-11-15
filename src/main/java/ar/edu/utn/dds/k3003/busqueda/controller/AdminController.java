package ar.edu.utn.dds.k3003.busqueda.controller;

import ar.edu.utn.dds.k3003.busqueda.dto.HechoDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.PdIDTO;
import ar.edu.utn.dds.k3003.busqueda.repository.HechoIndexadoRepository;
import ar.edu.utn.dds.k3003.busqueda.service.IndexacionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller para operaciones administrativas del índice.
 * Uso: sincronización inicial, limpieza, estadísticas.
 */
@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminController {

    private final IndexacionService indexacionService;
    private final HechoIndexadoRepository repository;

    public AdminController(IndexacionService indexacionService,
                           HechoIndexadoRepository repository) {
        this.indexacionService = indexacionService;
        this.repository = repository;
    }

    /**
     * Endpoint para carga masiva de hechos (sincronización inicial).
     * POST /api/admin/sync/hechos
     * Body: Lista de HechoDTO
     */
    @PostMapping("/sync/hechos")
    public ResponseEntity<Map<String, Object>> sincronizarHechos(
            @RequestBody List<HechoDTO> hechos) {
        log.info("🔄 Iniciando sincronización de {} hechos", hechos.size());

        int exitosos = 0;
        int errores = 0;

        for (HechoDTO hecho : hechos) {
            try {
                indexacionService.indexarHecho(hecho);
                exitosos++;
            } catch (Exception e) {
                log.error("Error indexando hecho {}: {}", hecho.id(), e.getMessage());
                errores++;
            }
        }

        Map<String, Object> resultado = Map.of(
                "total", hechos.size(),
                "exitosos", exitosos,
                "errores", errores
        );

        log.info("✅ Sincronización completada: {}", resultado);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Endpoint para carga masiva de PDIs.
     * POST /api/admin/sync/pdis
     * Body: Lista de PdIDTO
     */
    @PostMapping("/sync/pdis")
    public ResponseEntity<Map<String, Object>> sincronizarPdIs(
            @RequestBody List<PdIDTO> pdis) {
        log.info("🔄 Iniciando sincronización de {} PDIs", pdis.size());

        int exitosos = 0;
        int errores = 0;

        for (PdIDTO pdi : pdis) {
            try {
                indexacionService.indexarPdI(pdi);
                exitosos++;
            } catch (Exception e) {
                log.error("Error indexando PDI {}: {}", pdi.id(), e.getMessage());
                errores++;
            }
        }

        Map<String, Object> resultado = Map.of(
                "total", pdis.size(),
                "exitosos", exitosos,
                "errores", errores
        );

        log.info("✅ Sincronización de PDIs completada: {}", resultado);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Estadísticas del índice.
     * GET /api/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        long total = repository.count();
        long censurados = repository.countByCensuradoTrue();

        Map<String, Object> stats = Map.of(
                "totalHechosIndexados", total,
                "hechosActivos", total - censurados,
                "hechosCensurados", censurados
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * Limpiar todo el índice (PELIGROSO - solo para desarrollo).
     * DELETE /api/admin/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<String> limpiarIndice() {
        log.warn("⚠️ Limpiando todo el índice de búsqueda");
        repository.deleteAll();
        return ResponseEntity.ok("Índice limpiado");
    }
}