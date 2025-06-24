package com.tpi.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;


@Configuration
@EnableWebFluxSecurity
public class GWConfig {

    /*
       Este bean toma como parametros los values de las URL a los otros dos microservicios que se encuentran
       como variables en el application.properties.
    */
    @Bean
    public RouteLocator configurarRutas(RouteLocatorBuilder builder,
                                        @Value("${agencia-api-gw.microservicio-agencia}") String uriAgencia) {

        return builder.routes()
                .route(p -> p
                        // La regla se activa si la ruta coincide con CUALQUIERA de estos patrones.
                        .path("/api/pruebas/**", "/api/vehiculos/**", "/api/crud/vehiculos/**",
                                "/api/empleados/**", "/api/crud/modelos/**", "/api/interesados/**",
                                "/api/reportes/**"
                        )
                        .uri(uriAgencia))
//                .route(p -> p
//                        .path("/api/v1/notificaciones/**")
//                        .filters(f -> f.stripPrefix(3))
//                        .uri(uriNotificaciones))
                .build();
    }

}
