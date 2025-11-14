package ar.edu.utn.dds.k3003.busqueda.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.TextIndexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdIIndexado {

    private String pdiId;

    @TextIndexed(weight = 3)
    private String descripcion;

    private String lugar;

    @TextIndexed(weight = 2)
    private String contenido;

    private LocalDateTime momento;

    private String imagenUrl;

    // Resultado de procesamiento OCR
    @TextIndexed(weight = 4)  // Texto extraído tiene alta relevancia
    private String ocrText;

    // Etiquetas generadas por IA
    private List<String> etiquetasIA = new ArrayList<>();

    private String estadoProcesamiento;

    private LocalDateTime fechaProcesamiento;
}