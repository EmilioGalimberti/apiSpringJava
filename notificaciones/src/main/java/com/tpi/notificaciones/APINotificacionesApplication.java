package com.tpi.notificaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class APINotificacionesApplication {

	public static void main(String[] args) {
		SpringApplication.run(APINotificacionesApplication.class, args);
	}

}
