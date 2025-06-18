package com.tpi.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class GWConfig {


    @Bean
    public RouteLocator configurarRutas(RouteLocatorBuilder builder,
                                        @Value("${tpi-agencia-api-gw.microservicio-agencia}") String uriAgencia,
                                        @Value("${tpi-agencia-api-gw.microservicio-notificaciones}") String uriNotificaciones) {

        return builder.routes()
                .route(p -> p
                        .path("/api/v1/agencia/**")
                        .filters(f -> f.stripPrefix(3))  // Quita los primeros tres segmentos (api, v1 y agencia porque en el microservicio las rutas no lo tienen)
                        .uri(uriAgencia))
                .route(p -> p
                        .path("/api/v1/notificaciones/**")
                        .filters(f -> f.stripPrefix(3))
                        .uri(uriNotificaciones))
                .build();
    }


    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        http.authorizeExchange(exchanges -> exchanges

                        // Solo empleados pueden crear pruebas y enviar notificaciones
                        .pathMatchers("/api/v1/agencia/pruebas/new")
                        .hasRole("EMPLEADO")

                        .pathMatchers("/api/v1/notificaciones/promocion")
                        .hasRole("EMPLEADO")

                        // Solo usuarios asociados a vehículos pueden enviar posiciones
                        .pathMatchers("/api/v1/agencia/pruebas/posicion")
                        .hasRole("VEHICULO")

                        // Solo administradores pueden ver reportes
                        .pathMatchers("/api/v1/agencia/reportes/**")
                        .hasRole("ADMIN")

                        // Para cualquier peticion hace falta estar autenticado.
                        .anyExchange()
                        .authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
    }


    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        var jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
        var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                new ReactiveJwtGrantedAuthoritiesConverterAdapter(grantedAuthoritiesConverter));
        return jwtAuthenticationConverter;
    }


}
