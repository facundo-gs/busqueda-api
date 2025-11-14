package ar.edu.utn.dds.k3003.busqueda.repository;

import ar.edu.utn.dds.k3003.busqueda.model.HechoIndexado;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HechoIndexadoRepositoryImpl implements HechoIndexadoRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<HechoIndexado> searchByText(String query, Pageable pageable) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(query);

        Query q = new Query(criteria)
                .addCriteria(Criteria.where("censurado").is(false))
                .with(pageable);

        List<HechoIndexado> content = mongoTemplate.find(q, HechoIndexado.class);
        long total = mongoTemplate.count(q.skip(0).limit(0), HechoIndexado.class);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<HechoIndexado> searchByTextAndTags(String query, List<String> tags, Pageable pageable) {
        TextCriteria text = TextCriteria.forDefaultLanguage().matching(query);

        Query q = new Query(text)
                .addCriteria(Criteria.where("etiquetas").all(tags))
                .addCriteria(Criteria.where("censurado").is(false))
                .with(pageable);

        List<HechoIndexado> content = mongoTemplate.find(q, HechoIndexado.class);
        long total = mongoTemplate.count(q.skip(0).limit(0), HechoIndexado.class);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<HechoIndexado> searchByTags(List<String> tags, Pageable pageable) {
        Query q = new Query()
                .addCriteria(Criteria.where("etiquetas").all(tags))
                .addCriteria(Criteria.where("censurado").is(false))
                .with(pageable);

        List<HechoIndexado> content = mongoTemplate.find(q, HechoIndexado.class);
        long total = mongoTemplate.count(q.skip(0).limit(0), HechoIndexado.class);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<HechoIndexado> searchByTextAndColeccion(String query, String coleccion, Pageable pageable) {
        TextCriteria text = TextCriteria.forDefaultLanguage().matching(query);

        Query q = new Query(text)
                .addCriteria(Criteria.where("nombreColeccion").is(coleccion))
                .addCriteria(Criteria.where("censurado").is(false))
                .with(pageable);

        List<HechoIndexado> content = mongoTemplate.find(q, HechoIndexado.class);
        long total = mongoTemplate.count(q.skip(0).limit(0), HechoIndexado.class);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countNoCensurados() {
        Query q = new Query(Criteria.where("censurado").is(false));
        return mongoTemplate.count(q, HechoIndexado.class);
    }

    @Override
    public Page<HechoIndexado> findAllNoCensurados(Pageable pageable) {
        Query q = new Query(Criteria.where("censurado").is(false))
                .with(pageable);

        List<HechoIndexado> content = mongoTemplate.find(q, HechoIndexado.class);
        long total = mongoTemplate.count(q.skip(0).limit(0), HechoIndexado.class);

        return new PageImpl<>(content, pageable, total);
    }
}

