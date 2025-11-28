package cl.huertohogar.huertohogar_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HuertohogarApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HuertohogarApiApplication.class, args);
	}

}
