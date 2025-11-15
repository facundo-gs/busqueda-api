package ar.edu.utn.dds.k3003.busqueda.controller;

import ar.edu.utn.dds.k3003.busqueda.dto.HechoDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.PdIDTO;
import ar.edu.utn.dds.k3003.busqueda.service.IndexacionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


//Controller para recibir notificaciones de indexación desde otros módulos.

@RestController
@RequestMapping("/api/indexacion")
@Slf4j
public class IndexacionWebhookController {

    private final IndexacionService indexacionService;

    public IndexacionWebhookController(IndexacionService indexacionService) {
        this.indexacionService = indexacionService;
    }

    @PostMapping("/hecho")
    public ResponseEntity<String> indexarHecho(@RequestBody HechoDTO hechoDTO) {
        log.info("Webhook recibido: indexar hecho id={}", hechoDTO.id());
        try {
            indexacionService.indexarHecho(hechoDTO);
            return ResponseEntity.ok("Hecho indexado correctamente");
        } catch (Exception e) {
            log.error("❌ Error indexando hecho: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error indexando hecho: " + e.getMessage());
        }
    }

    @PostMapping("/pdi")
    public ResponseEntity<String> indexarPdI(@RequestBody PdIDTO pdiDTO) {
        log.info("Webhook recibido: indexar PDI id={} para hecho={}",
                pdiDTO.id(), pdiDTO.hechoId());
        try {
            indexacionService.indexarPdI(pdiDTO);
            return ResponseEntity.ok("PDI indexado correctamente");
        } catch (Exception e) {
            log.error("❌ Error indexando PDI: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error indexando PDI: " + e.getMessage());
        }
    }

    @PostMapping("/censurar/{hechoId}")
    public ResponseEntity<String> censurarHecho(@PathVariable String hechoId) {
        log.info("Webhook recibido: censurar hecho id={}", hechoId);
        try {
            indexacionService.censurarHecho(hechoId);
            return ResponseEntity.ok("Hecho censurado correctamente");
        } catch (Exception e) {
            log.error("❌ Error censurando hecho: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error censurando hecho: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Servicio de indexación activo");
    }
}