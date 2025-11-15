package ar.edu.utn.dds.k3003.busqueda.service;

import ar.edu.utn.dds.k3003.busqueda.model.HechoIndexado;
import ar.edu.utn.dds.k3003.busqueda.repository.HechoIndexadoRepository;
import ar.edu.utn.dds.k3003.busqueda.dto.HechoDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.PdIDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Servicio para indexar hechos y PDIs en MongoDB.
 * Mantiene consistencia eventual con los módulos de Fuente y PDI.
 *
 * NOTA: No usa @Transactional porque MongoDB no soporta transacciones
 * en operaciones simples (solo en replica sets con sesiones).
 */
@Service
@Slf4j
public class IndexacionService {

    private final HechoIndexadoRepository repository;

    public IndexacionService(HechoIndexadoRepository repository) {
        this.repository = repository;
    }

    /**
     * Indexa o actualiza un hecho en MongoDB.
     * Si el hecho ya existe, actualiza sus campos.
     */
    public void indexarHecho(HechoDTO hechoDTO) {
        log.info("📝 Indexando hecho: {} - {}", hechoDTO.id(), hechoDTO.titulo());

        try {
            // Buscar si ya existe
            Optional<HechoIndexado> existente = repository.findById(hechoDTO.id());

            HechoIndexado indexado;
            if (existente.isPresent()) {
                indexado = existente.get();
                log.debug("   Actualizando hecho existente");
            } else {
                indexado = crearNuevoIndexado(hechoDTO);
                log.debug("   Creando nuevo hecho en índice");
            }

            actualizarDesdeDTO(indexado, hechoDTO);
            repository.save(indexado);

            log.info("✅ Hecho indexado exitosamente: {}", hechoDTO.id());

        } catch (Exception e) {
            log.error("❌ Error indexando hecho {}: {}", hechoDTO.id(), e.getMessage(), e);
            // Re-lanzar para que el llamador sepa que falló
            throw new RuntimeException("Error en indexación de hecho", e);
        }
    }

    /**
     * Indexa un PDI asociado a un hecho.
     * Si el hecho no existe en el índice, se omite el PDI.
     */
    public void indexarPdI(PdIDTO pdiDTO) {
        log.info("📝 Indexando PDI: {} para hecho: {}", pdiDTO.id(), pdiDTO.hechoId());

        try {
            Optional<HechoIndexado> hechoOpt = repository.findById(pdiDTO.hechoId());

            if (hechoOpt.isEmpty()) {
                log.warn("⚠️ Hecho {} no existe en índice. El PDI {} será indexado cuando llegue el hecho.",
                        pdiDTO.hechoId(), pdiDTO.id());
                // Opción: podrías guardar en una cola para reintentar más tarde
                return;
            }

            HechoIndexado hecho = hechoOpt.get();

            // Verificar si el PDI ya fue procesado
            if (hecho.tienePdI(pdiDTO.id())) {
                // Actualizar datos del PDI (OCR y etiquetas pueden haber cambiado)
                hecho.actualizarPdI(
                        pdiDTO.id(),
                        pdiDTO.ocrText(),
                        pdiDTO.etiquetasIA()
                );
                log.debug("   PDI {} actualizado en hecho {}", pdiDTO.id(), pdiDTO.hechoId());
            } else {
                // Agregar nuevo PDI
                hecho.agregarPdI(
                        pdiDTO.id(),
                        pdiDTO.contenido(),
                        pdiDTO.ocrText(),
                        pdiDTO.etiquetasIA()
                );
                log.debug("   PDI {} agregado a hecho {}", pdiDTO.id(), pdiDTO.hechoId());
            }

            repository.save(hecho);
            log.info("✅ PDI indexado exitosamente: {}", pdiDTO.id());

        } catch (Exception e) {
            log.error("❌ Error indexando PDI {}: {}", pdiDTO.id(), e.getMessage(), e);
            throw new RuntimeException("Error en indexación de PDI", e);
        }
    }

    /**
     * Marca un hecho como censurado (no aparecerá en búsquedas).
     * Esto ocurre cuando se acepta una solicitud de borrado.
     */
    public void censurarHecho(String hechoId) {
        log.info("🚫 Censurando hecho: {}", hechoId);

        try {
            Optional<HechoIndexado> hechoOpt = repository.findById(hechoId);

            if (hechoOpt.isEmpty()) {
                log.warn("⚠️ Hecho {} no existe en índice, no se puede censurar", hechoId);
                return;
            }

            HechoIndexado hecho = hechoOpt.get();

            if (hecho.isCensurado()) {
                log.debug("   Hecho {} ya estaba censurado", hechoId);
                return;
            }

            hecho.censurar();
            repository.save(hecho);

            log.info("✅ Hecho censurado exitosamente: {}", hechoId);

        } catch (Exception e) {
            log.error("❌ Error censurando hecho {}: {}", hechoId, e.getMessage(), e);
            throw new RuntimeException("Error en censura de hecho", e);
        }
    }

    /**
     * Elimina un hecho del índice completamente.
     * Uso: limpieza manual o casos especiales.
     */
    public void eliminarHecho(String hechoId) {
        log.info("🗑️ Eliminando hecho del índice: {}", hechoId);

        try {
            if (repository.existsById(hechoId)) {
                repository.deleteById(hechoId);
                log.info("✅ Hecho eliminado del índice: {}", hechoId);
            } else {
                log.warn("⚠️ Hecho {} no existe en índice", hechoId);
            }
        } catch (Exception e) {
            log.error("❌ Error eliminando hecho {}: {}", hechoId, e.getMessage(), e);
            throw new RuntimeException("Error eliminando hecho del índice", e);
        }
    }

    /**
     * Crea un nuevo documento HechoIndexado desde el DTO.
     */
    private HechoIndexado crearNuevoIndexado(HechoDTO dto) {
        return HechoIndexado.builder()
                .id(dto.id())
                .nombreColeccion(dto.nombreColeccion())
                .titulo(dto.titulo())
                .descripcion(dto.titulo())  // Usar título como descripción inicial
                .etiquetas(dto.etiquetas() != null ? new ArrayList<>(dto.etiquetas()) : new ArrayList<>())
                .categoria(dto.categoria() != null ? dto.categoria().name() : null)
                .ubicacion(dto.ubicacion())
                .fecha(dto.fecha())
                .origen(dto.origen())
                .censurado(false)
                .ultimaActualizacion(LocalDateTime.now())
                .version(1L)
                .build();
    }

    /**
     * Actualiza los campos de un HechoIndexado desde el DTO.
     */
    private void actualizarDesdeDTO(HechoIndexado indexado, HechoDTO dto) {
        indexado.setTitulo(dto.titulo());
        indexado.setNombreColeccion(dto.nombreColeccion());

        if (dto.etiquetas() != null) {
            indexado.setEtiquetas(new ArrayList<>(dto.etiquetas()));
        }

        if (dto.categoria() != null) {
            indexado.setCategoria(dto.categoria().name());
        }

        if (dto.ubicacion() != null) {
            indexado.setUbicacion(dto.ubicacion());
        }

        if (dto.fecha() != null) {
            indexado.setFecha(dto.fecha());
        }

        if (dto.origen() != null) {
            indexado.setOrigen(dto.origen());
        }

        indexado.setUltimaActualizacion(LocalDateTime.now());
        indexado.setVersion(indexado.getVersion() + 1);
    }
}