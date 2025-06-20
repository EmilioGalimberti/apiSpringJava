//Parte vieja sin autenticación con OA2
/*
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

 */



package com.tpi.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(grantedAuthoritiesExtractor())
                        )
                );

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
