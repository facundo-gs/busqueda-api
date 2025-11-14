package ar.edu.utn.dds.k3003.busqueda.repository;

import ar.edu.utn.dds.k3003.busqueda.model.HechoIndexado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HechoIndexadoRepositoryCustom {

    Page<HechoIndexado> searchByText(String query, Pageable pageable);

    Page<HechoIndexado> searchByTextAndTags(String query, List<String> tags, Pageable pageable);

    Page<HechoIndexado> searchByTags(List<String> tags, Pageable pageable);

    Page<HechoIndexado> searchByTextAndColeccion(String query, String coleccion, Pageable pageable);

    long countNoCensurados();

    Page<HechoIndexado> findAllNoCensurados(Pageable pageable);
}
