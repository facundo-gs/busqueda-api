package ar.edu.utn.dds.k3003.busqueda.controller;

import ar.edu.utn.dds.k3003.busqueda.dto.BusquedaRequestDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.BusquedaResponseDTO;
import ar.edu.utn.dds.k3003.busqueda.service.BusquedaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para búsqueda de hechos.
 * Usado por el bot de Telegram y otros clientes.
 */
@RestController
@RequestMapping("/api/busqueda")
@Slf4j
public class BusquedaController {

    private final BusquedaService busquedaService;

    public BusquedaController(BusquedaService busquedaService) {
        this.busquedaService = busquedaService;
    }

    /**
     * Endpoint principal de búsqueda.
     *
     * Ejemplos:
     * GET /api/busqueda?q=incendio&page=0&size=10
     * GET /api/busqueda?q=incendio&tags=CABA,urgente&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<BusquedaResponseDTO> buscar(
            @RequestParam("q") String consulta,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "page", defaultValue = "0") int pagina,
            @RequestParam(value = "size", defaultValue = "10") int tamanio
    ) {
        log.info("🔍 GET /api/busqueda q='{}' tags={} page={} size={}",
                consulta, tags, pagina, tamanio);

        try {
            BusquedaRequestDTO request = new BusquedaRequestDTO(consulta, tags, pagina, tamanio);
            BusquedaResponseDTO response = busquedaService.buscar(request);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Parámetros inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ Error en búsqueda: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}