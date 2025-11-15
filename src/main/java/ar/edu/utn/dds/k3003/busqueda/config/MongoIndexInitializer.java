package ar.edu.utn.dds.k3003.busqueda.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;


//Verificador de índices (no los crea, solo verifica).
//Los índices se crean automáticamente por @TextIndexed y auto-index-creation=true
@Configuration
@Slf4j
public class MongoIndexInitializer {

    @Bean
    public CommandLineRunner verifyMongoIndexes(MongoTemplate mongoTemplate) {
        return args -> {
            log.info("Verificando índices de MongoDB...");

            try {
                var indexes = mongoTemplate.indexOps("hechos_indexados").getIndexInfo();

                log.info("Índices encontrados: {}", indexes.size());
                indexes.forEach(idx ->
                        log.info("Índice: {}", idx.getName())
                );

                // Verificar que existe el índice de texto
                boolean hasTextIndex = indexes.stream()
                        .anyMatch(idx -> idx.getName().contains("text"));

                if (hasTextIndex) {
                    log.info("Índice de texto completo configurado correctamente");
                } else {
                    log.warn("Índice de texto no encontrado. Se creará automáticamente al guardar el primer documento.");
                }

            } catch (Exception e) {
                log.error("Error verificando índices: {}", e.getMessage());
            }
        };
    }
}