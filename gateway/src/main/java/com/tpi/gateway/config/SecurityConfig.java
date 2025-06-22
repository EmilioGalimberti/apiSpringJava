package com.tpi.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

/*
    Rol EMPLEADO puede:
    POST /api/pruebas/crear
    PATCH /api/pruebas/{id}/finalizar
    GET /api/pruebas/en-curso
    POST /api/notificaciones/**
     Acceso a CRUD de vehículos, modelos, empleados e interesados


     Rol VEHICULO puede:
     POST /api/vehiculos/posicion/new


     Rol ADMIN puede:
     GET /api/reportes/**


     Cualquier ruta bajo /api/** requiere autenticación.
     Resto de rutas sin restricción.
*/


     @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        http.authorizeExchange(exchanges -> exchanges

                        // EMPLEADO
                        .pathMatchers(HttpMethod.POST, "/api/pruebas/crear").hasRole("EMPLEADO")
                        .pathMatchers(HttpMethod.PATCH, "/api/pruebas/*/finalizar").hasRole("EMPLEADO")
                        .pathMatchers(HttpMethod.GET, "/api/pruebas/en-curso").hasRole("EMPLEADO")
                        .pathMatchers(HttpMethod.POST, "/api/notificaciones/**").hasRole("EMPLEADO")
                        .pathMatchers("/api/crud/vehiculos/**").hasRole("EMPLEADO")
                        .pathMatchers("/api/crud/modelos/**").hasRole("EMPLEADO")
                        .pathMatchers("/api/empleados/**").hasRole("EMPLEADO")
                        .pathMatchers("/api/interesados/**").hasRole("EMPLEADO")

                        // VEHICULO
                        .pathMatchers(HttpMethod.POST, "/api/vehiculos/posicion/new").hasRole("VEHICULO")

                        // ADMIN
                        .pathMatchers("/api/reportes/**").hasRole("ADMIN")
                        //.pathMatchers("/api/**").hasRole("ADMIN")



                        // Otros requerimientos
                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }


    private ReactiveJwtAuthenticationConverter grantedAuthoritiesExtractor() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt ->
                Flux.fromIterable(extractAuthoritiesFromJwt(jwt))
        );
        return converter;
    }

    private Collection<? extends GrantedAuthority> extractAuthoritiesFromJwt(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("authorities");

        if (roles == null) {
            var realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof java.util.Map<?, ?> map && map.get("roles") instanceof List<?> roleList) {
                roles = roleList.stream().map(Object::toString).toList();
            }
        }

        if (roles == null) {
            return List.of();
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }
}
