package ar.edu.utn.dds.k3003.busqueda.service;

import ar.edu.utn.dds.k3003.busqueda.model.HechoIndexado;
import ar.edu.utn.dds.k3003.busqueda.model.PdIIndexado;
import ar.edu.utn.dds.k3003.busqueda.repository.HechoIndexadoRepository;
import ar.edu.utn.dds.k3003.busqueda.dto.HechoDTO;
import ar.edu.utn.dds.k3003.busqueda.dto.PdIDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexacionService {

    private final HechoIndexadoRepository repository;

    /**
     * Indexa un nuevo hecho
     */
    @Transactional
    public void indexarHecho(HechoDTO hechoDTO) {
        try {
            log.info("📝 Indexando hecho: id={}, titulo='{}'",
                    hechoDTO.id(), hechoDTO.titulo());

            // Verificar si ya existe
            Optional<HechoIndexado> existente = repository.findByHechoId(hechoDTO.id());

            if (existente.isPresent()) {
                log.warn("⚠️ Hecho ya indexado: {}, actualizando...", hechoDTO.id());
                actualizarHecho(existente.get(), hechoDTO);
                return;
            }

            // Crear nuevo documento indexado
            HechoIndexado nuevo = HechoIndexado.builder()
                    .hechoId(hechoDTO.id())
                    .nombreColeccion(hechoDTO.nombreColeccion())
                    .origen(hechoDTO.origen())
                    .titulo(hechoDTO.titulo())
                    .descripcion(null)  // No viene en HechoDTO básico
                    .ubicacion(hechoDTO.ubicacion())
                    .categoria(hechoDTO.categoria() != null ? hechoDTO.categoria().name() : null)
                    .fecha(hechoDTO.fecha())
                    .etiquetas(hechoDTO.etiquetas() != null ? new ArrayList<>(hechoDTO.etiquetas()) : new ArrayList<>())
                    .pdis(new ArrayList<>())
                    .censurado(false)
                    .fechaCreacion(LocalDateTime.now())
                    .fechaIndexacion(LocalDateTime.now())
                    .ultimaActualizacion(LocalDateTime.now())
                    .version(1)
                    .colecciones(new ArrayList<>(java.util.List.of(hechoDTO.nombreColeccion())))
                    .build();

            repository.save(nuevo);

            log.info("✅ Hecho indexado exitosamente: {}", hechoDTO.id());
        } catch (Exception e) {
            log.error("❌ Error indexando hecho {}: {}", hechoDTO.id(), e.getMessage(), e);
            throw new RuntimeException("Error en indexación de hecho", e);
        }
    }

    /**
     * Indexa un PDI y lo agrega al hecho correspondiente
     */
    @Transactional
    public void indexarPdI(PdIDTO pdiDTO) {
        try {
            log.info("📝 Indexando PDI: id={}, hechoId={}",
                    pdiDTO.id(), pdiDTO.hechoId());

            // Buscar el hecho asociado
            Optional<HechoIndexado> hechoOpt = repository.findByHechoId(pdiDTO.hechoId());

            if (hechoOpt.isEmpty()) {
                log.warn("⚠️ Hecho no encontrado para PDI: hechoId={}, esperando sincronización...",
                        pdiDTO.hechoId());
                // El hecho llegará por evento posterior, no es error crítico
                return;
            }

            HechoIndexado hecho = hechoOpt.get();

            // Verificar si el PDI ya existe
            boolean yaExiste = hecho.getPdis().stream()
                    .anyMatch(p -> p.getPdiId().equals(pdiDTO.id()));

            if (yaExiste) {
                log.warn("⚠️ PDI ya indexado: {}", pdiDTO.id());
                return;
            }

            // Crear PDI indexado
            PdIIndexado pdiIndexado = PdIIndexado.builder()
                    .pdiId(pdiDTO.id())
                    .descripcion(pdiDTO.descripcion())
                    .lugar(pdiDTO.lugar())
                    .contenido(pdiDTO.contenido())
                    .momento(pdiDTO.momento())
                    .imagenUrl(pdiDTO.imagenUrl())
                    .ocrText(pdiDTO.ocrText())
                    .etiquetasIA(pdiDTO.etiquetasIA() != null ? new ArrayList<>(pdiDTO.etiquetasIA()) : new ArrayList<>())
                    .estadoProcesamiento(pdiDTO.estadoProcesamiento() != null ? pdiDTO.estadoProcesamiento().name() : null)
                    .fechaProcesamiento(pdiDTO.fechaProcesamiento())
                    .build();

            // Agregar al hecho
            hecho.agregarPdI(pdiIndexado);
            hecho.setUltimaActualizacion(LocalDateTime.now());
            hecho.setVersion(hecho.getVersion() + 1);

            // Merge de etiquetas IA con etiquetas del hecho
            if (pdiDTO.etiquetasIA() != null) {
                for (String etiqueta : pdiDTO.etiquetasIA()) {
                    if (!hecho.getEtiquetas().contains(etiqueta)) {
                        hecho.getEtiquetas().add(etiqueta);
                    }
                }
            }

            repository.save(hecho);

            log.info("✅ PDI indexado exitosamente: {} agregado al hecho {}",
                    pdiDTO.id(), pdiDTO.hechoId());
        } catch (Exception e) {
            log.error("❌ Error indexando PDI {}: {}", pdiDTO.id(), e.getMessage(), e);
            throw new RuntimeException("Error en indexación de PDI", e);
        }
    }

    /**
     * Marca un hecho como censurado (Req #7)
     */
    @Transactional
    public void censurarHecho(String hechoId, String solicitudId) {
        try {
            log.info("🚫 Censurando hecho: id={}, solicitudId={}",
                    hechoId, solicitudId);

            Optional<HechoIndexado> hechoOpt = repository.findByHechoId(hechoId);

            if (hechoOpt.isEmpty()) {
                log.warn("⚠️ Hecho no encontrado para censurar: {}", hechoId);
                return;
            }

            HechoIndexado hecho = hechoOpt.get();
            hecho.censurar(solicitudId);

            repository.save(hecho);

            log.info("✅ Hecho censurado exitosamente: {}", hechoId);
        } catch (Exception e) {
            log.error("❌ Error censurando hecho {}: {}", hechoId, e.getMessage(), e);
            throw new RuntimeException("Error en censura de hecho", e);
        }
    }

    /**
     * Actualiza un hecho existente
     */
    private void actualizarHecho(HechoIndexado existente, HechoDTO nuevoDTO) {
        existente.setTitulo(nuevoDTO.titulo());
        existente.setUbicacion(nuevoDTO.ubicacion());
        existente.setFecha(nuevoDTO.fecha());
        existente.setCategoria(nuevoDTO.categoria() != null ? nuevoDTO.categoria().name() : null);

        if (nuevoDTO.etiquetas() != null) {
            existente.setEtiquetas(new ArrayList<>(nuevoDTO.etiquetas()));
        }

        // Agregar colección si es nueva
        if (!existente.getColecciones().contains(nuevoDTO.nombreColeccion())) {
            existente.getColecciones().add(nuevoDTO.nombreColeccion());
        }

        existente.setUltimaActualizacion(LocalDateTime.now());
        existente.setVersion(existente.getVersion() + 1);

        repository.save(existente);

        log.info("✅ Hecho actualizado: {}", nuevoDTO.id());
    }

    /**
     * Elimina un hecho del índice (opcional, para limpieza)
     */
    public void eliminarDelIndice(String hechoId) {
        try {
            log.info("🗑️ Eliminando hecho del índice: {}", hechoId);

            Optional<HechoIndexado> hechoOpt = repository.findByHechoId(hechoId);

            if (hechoOpt.isPresent()) {
                repository.delete(hechoOpt.get());
                log.info("✅ Hecho eliminado del índice: {}", hechoId);
            } else {
                log.warn("⚠️ Hecho no encontrado en índice: {}", hechoId);
            }
        } catch (Exception e) {
            log.error("❌ Error eliminando hecho del índice {}: {}", hechoId, e.getMessage(), e);
        }
    }

    /**
     * Reindexar todos los datos (para sincronización inicial)
     */
    @Transactional
    public void reindexarTodo() {
        log.info("🔄 Iniciando reindexación completa...");

        // Limpiar índice actual
        long count = repository.count();
        repository.deleteAll();
        log.info("🗑️ {} documentos eliminados del índice", count);

        log.info("✅ Índice limpio, listo para sincronización inicial");
    }
}