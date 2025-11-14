package ar.edu.utn.dds.k3003.busqueda.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "hechos_indexados")
@CompoundIndex(
        name = "censurado_coleccion_idx",
        def = "{'censurado': 1, 'nombreColeccion': 1}"
)
public class HechoIndexado {

    @Id
    private String id;  // MongoDB ObjectId

    // Identificación del hecho original
    @Indexed(unique = true)
    private String hechoId;

    @Indexed
    private String nombreColeccion;

    @Indexed
    private String origen;

    // Campos indexados para búsqueda full-text
    @TextIndexed(weight = 10)  // Mayor peso al título
    private String titulo;

    @TextIndexed(weight = 5)
    private String descripcion;

    @Indexed
    private String ubicacion;

    private String categoria;

    @Indexed
    private LocalDateTime fecha;

    // Etiquetas del hecho
    @Indexed
    private List<String> etiquetas = new ArrayList<>();

    // PDIs asociados (desnormalizados)
    private List<PdIIndexado> pdis = new ArrayList<>();

    // Control de visibilidad
    @Indexed
    private Boolean censurado = false;

    private LocalDateTime fechaCensura;

    private String solicitudBorradoId;

    // Metadatos de sincronización
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaIndexacion;

    private LocalDateTime ultimaActualizacion;

    @Indexed
    private Integer version = 1;

    // Para deduplicación
    private List<String> colecciones = new ArrayList<>();

    // Método helper para agregar PDI
    public void agregarPdI(PdIIndexado pdi) {
        if (this.pdis == null) {
            this.pdis = new ArrayList<>();
        }
        this.pdis.add(pdi);
    }

    // Método helper para marcar como censurado
    public void censurar(String solicitudId) {
        this.censurado = true;
        this.fechaCensura = LocalDateTime.now();
        this.solicitudBorradoId = solicitudId;
        this.ultimaActualizacion = LocalDateTime.now();
        this.version++;
    }
}