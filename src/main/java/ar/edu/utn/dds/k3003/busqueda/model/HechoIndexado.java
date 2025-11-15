package ar.edu.utn.dds.k3003.busqueda.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


//Documento para búsqueda de hechos indexados.
//Incluye datos de Hecho + PDIs asociados para búsqueda full-text
//Los nombres de @Field deben coincidir con los usados en las queries del repositorio.
@Document(collection = "hechos_indexados")
@CompoundIndex(name = "titulo_coleccion_idx", def = "{'titulo': 1, 'nombreColeccion': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HechoIndexado {

    @Id
    private String id;  // ID del hecho original (de PostgreSQL)

    private String nombreColeccion;

    @TextIndexed(weight = 10)
    private String titulo;

    @TextIndexed(weight = 5)
    private String descripcion;

    @TextIndexed(weight = 3)
    private String ubicacion;

    private String categoria;

    private LocalDateTime fecha;

    private String origen;

    @Field("tags")
    @Builder.Default
    private List<String> etiquetas = new ArrayList<>();

    // Contenido de texto de los PDIs (para búsqueda full-text)
    @TextIndexed(weight = 4)
    @Field("pdi_contenido")
    @Builder.Default
    private List<String> pdiContenido = new ArrayList<>();

    // Texto extraído por OCR de las imágenes
    @TextIndexed(weight = 2)
    @Field("ocr_text")
    @Builder.Default
    private List<String> ocrTexts = new ArrayList<>();

    // Etiquetas generadas por IA (de imágenes)
    @Field("etiquetas_ia")
    @Builder.Default
    private List<String> etiquetasIA = new ArrayList<>();

    // IDs de los PDIs asociados (para tracking)
    @Field("pdi_ids")
    @Builder.Default
    private Set<String> pdiIds = new HashSet<>();

    // Flag para excluir de búsquedas (solicitud de borrado aceptada)
    @Builder.Default
    private boolean censurado = false;

    // Timestamp de última modificación
    private LocalDateTime ultimaActualizacion;

    // Versión para control de concurrencia optimista
    @Builder.Default
    private Long version = 0L;

    public void agregarPdI(String pdiId, String contenido, String ocrText, List<String> etiquetasIA) {
        // Evitar agregar el mismo PDI múltiples veces
        if (pdiId != null && this.pdiIds.contains(pdiId)) {
            // Ya existe, actualizar en lugar de agregar
            return;
        }

        if (pdiId != null) {
            this.pdiIds.add(pdiId);
        }

        if (contenido != null && !contenido.isBlank()) {
            this.pdiContenido.add(contenido.trim());
        }

        if (ocrText != null && !ocrText.isBlank()) {
            this.ocrTexts.add(ocrText.trim());
        }

        if (etiquetasIA != null && !etiquetasIA.isEmpty()) {
            // Evitar duplicados en etiquetas IA
            for (String etiqueta : etiquetasIA) {
                if (!this.etiquetasIA.contains(etiqueta)) {
                    this.etiquetasIA.add(etiqueta);
                }
            }
        }

        this.ultimaActualizacion = LocalDateTime.now();
        this.version++;
    }

    public void actualizarPdI(String pdiId, String ocrText, List<String> etiquetasIA) {
        if (pdiId == null || !this.pdiIds.contains(pdiId)) {
            // El PDI no existe, agregarlo
            agregarPdI(pdiId, null, ocrText, etiquetasIA);
            return;
        }

        // El PDI ya existe, solo agregar nuevos datos
        if (ocrText != null && !ocrText.isBlank() && !this.ocrTexts.contains(ocrText.trim())) {
            this.ocrTexts.add(ocrText.trim());
        }

        if (etiquetasIA != null) {
            for (String etiqueta : etiquetasIA) {
                if (!this.etiquetasIA.contains(etiqueta)) {
                    this.etiquetasIA.add(etiqueta);
                }
            }
        }

        this.ultimaActualizacion = LocalDateTime.now();
        this.version++;
    }

    public void censurar() {
        this.censurado = true;
        this.ultimaActualizacion = LocalDateTime.now();
        this.version++;
    }

    public boolean tienePdI(String pdiId) {
        return this.pdiIds.contains(pdiId);
    }
}