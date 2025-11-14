package ar.edu.utn.dds.k3003.busqueda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableRetry
@EnableScheduling
public class EventConfig {

    /**
     * Configuración de eventos asíncronos
     */
    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();

        // Executor asíncrono para eventos
        SimpleAsyncTaskExecutor asyncExecutor = new SimpleAsyncTaskExecutor();
        asyncExecutor.setThreadNamePrefix("event-async-");
        eventMulticaster.setTaskExecutor(asyncExecutor);

        return eventMulticaster;
    }
}