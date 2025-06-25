package org.example.config; // O el paquete donde esté tu configuración

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// 1. MUY IMPORTANTE: La clase debe tener @Configuration
@Configuration
public class AppConfigWebClient {
   // 2. MUY IMPORTANTE: El método que crea el objeto debe tener @Bean
   // Le dice a Spring: "Aquí hay una receta para un objeto de tipo WebClient".
    @Bean
    public WebClient webClient() {
        // Aquí Spring aprende a "crear" un WebClient.
        // Puedes añadirle configuración extra si lo necesitas.
        return WebClient.builder().build();
    }
}