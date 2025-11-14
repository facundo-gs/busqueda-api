package ar.edu.utn.dds.k3003.busqueda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class DdsBusquedaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DdsBusquedaApplication.class, args);
	}

}
