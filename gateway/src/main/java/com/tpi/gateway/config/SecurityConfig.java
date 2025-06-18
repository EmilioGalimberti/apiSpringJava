package com.tpi.gateway.config; // O el paquete donde tengas tu configuración

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
// ¡Anotación clave para seguridad reactiva!
@EnableWebFluxSecurity
public class SecurityConfig {

    //SE LA DESABILITE SOLO PARA PROBARLO Y QUE NO ME PIDIERA AUTENTICACION
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                // 1. Deshabilitar la protección CSRF (versión reactiva)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // 2. Configurar las reglas de autorización de peticiones (versión reactiva)
                .authorizeExchange(exchange -> exchange
                        // Permite el acceso a todas las rutas que empiecen con /api/ sin autenticación
                        // Esto es para que las peticiones puedan pasar del Gateway al microservicio.
                        .pathMatchers("/api/**").permitAll()
                        // Cualquier otra petición debe ser autenticada (si las hubiera).
                        .anyExchange().authenticated()
                );

        return http.build();
    }
}