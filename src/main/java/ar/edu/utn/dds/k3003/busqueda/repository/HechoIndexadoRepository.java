package ar.edu.utn.dds.k3003.busqueda.repository;

import ar.edu.utn.dds.k3003.busqueda.model.HechoIndexado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

// El warning "Cannot resolve symbol '$text'" es deintellij,
// no afecta la ejecución. MongoDB sí reconoce $text.
@Repository
public interface HechoIndexadoRepository extends MongoRepository<HechoIndexado, String> {

    // Búsqueda full-text usando el índice de texto de MongoDB.
    // Busca en: titulo, descripcion, ubicacion, pdiContenido, ocrTexts
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

}