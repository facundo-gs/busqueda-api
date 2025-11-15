package ar.edu.utn.dds.k3003.busqueda.repository;

import ar.edu.utn.dds.k3003.busqueda.model.HechoIndexado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio MongoDB para búsqueda de hechos indexados.
 *
 * NOTA: El warning "Cannot resolve symbol '$text'" es del IDE (IntelliJ),
 * no afecta la ejecución. MongoDB sí reconoce $text.
 */
@Repository
public interface HechoIndexadoRepository extends MongoRepository<HechoIndexado, String> {

    /**
     * Búsqueda full-text usando el índice de texto de MongoDB.
     * Busca en: titulo, descripcion, ubicacion, pdiContenido, ocrTexts
     *
     * IMPORTANTE: Requiere índice de texto creado previamente.
     */
    @Query("{ $text: { $search: ?0 }, censurado: false }")
    Page<HechoIndexado> buscarPorTexto(String texto, Pageable pageable);

    /**
     * Búsqueda por texto + filtro de tags (criterio AND).
     * Un hecho coincide si tiene AL MENOS UNA de las etiquetas solicitadas
     * (ya sea en etiquetas manuales o etiquetas generadas por IA).
     */
    @Query("{ " +
            "$text: { $search: ?0 }, " +
            "$or: [ " +
            "  { 'tags': { $in: ?1 } }, " +
            "  { 'etiquetas_ia': { $in: ?1 } } " +
            "], " +
            "censurado: false " +
            "}")
    Page<HechoIndexado> buscarPorTextoYTags(String texto, List<String> tags, Pageable pageable);

    /**
     * Búsqueda por texto + filtro de tags con criterio AND estricto.
     * Un hecho coincide si tiene TODAS las etiquetas solicitadas.
     */
    @Query("{ " +
            "$text: { $search: ?0 }, " +
            "$or: [ " +
            "  { 'tags': { $all: ?1 } }, " +
            "  { 'etiquetas_ia': { $all: ?1 } } " +
            "], " +
            "censurado: false " +
            "}")
    Page<HechoIndexado> buscarPorTextoYTodosLosTags(String texto, List<String> tags, Pageable pageable);

    /**
     * Busca por título y colección para evitar duplicados.
     */
    Optional<HechoIndexado> findByTituloAndNombreColeccion(String titulo, String nombreColeccion);

    /**
     * Busca todos los hechos no censurados de una colección.
     */
    List<HechoIndexado> findByNombreColeccionAndCensuradoFalse(String nombreColeccion);

    /**
     * Verifica si existe un hecho con ese título (para deduplicación).
     */
    boolean existsByTituloAndCensuradoFalse(String titulo);

    /**
     * Busca hechos por IDs específicos.
     */
    List<HechoIndexado> findByIdIn(List<String> ids);

    /**
     * Cuenta resultados de búsqueda full-text.
     */
    @Query(value = "{ $text: { $search: ?0 }, censurado: false }", count = true)
    long contarPorTexto(String texto);

    /**
     * Cuenta resultados de búsqueda full-text con tags.
     */
    @Query(value = "{ " +
            "$text: { $search: ?0 }, " +
            "$or: [ " +
            "  { 'tags': { $in: ?1 } }, " +
            "  { 'etiquetas_ia': { $in: ?1 } } " +
            "], " +
            "censurado: false " +
            "}", count = true)
    long contarPorTextoYTags(String texto, List<String> tags);

    /**
     * Cuenta hechos censurados.
     */
    long countByCensuradoTrue();

    /**
     * Cuenta hechos no censurados (activos).
     */
    long countByCensuradoFalse();
}