package ar.edu.utn.dds.k3003.busqueda.repository;

import ar.edu.utn.dds.k3003.busqueda.model.HechoIndexado;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HechoIndexadoRepository
        extends MongoRepository<HechoIndexado, String>,
        HechoIndexadoRepositoryCustom {

    Optional<HechoIndexado> findByHechoId(String hechoId);

    List<HechoIndexado> findByTitulo(String titulo);

    boolean existsByHechoId(String hechoId);
}