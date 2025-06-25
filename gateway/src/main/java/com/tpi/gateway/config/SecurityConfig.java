package com.tpi.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration; // ¡ESTA ES LA IMPORTACIÓN QUE CAUSA EL PROBLEMA!
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        http
                .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Permitir las peticiones preflight de CORS. Debe ir primero.
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // --- Tus reglas de negocio ---
                        // EMPLEADO
                        .pathMatchers("/api/pruebas").hasAnyRole("ADMIN", "EMPLEADO")
                        .pathMatchers("/api/pruebas/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .pathMatchers(HttpMethod.POST, "/api/pruebas/crear").hasAnyRole("ADMIN", "EMPLEADO")
                        .pathMatchers(HttpMethod.PATCH, "/api/pruebas/*/finalizar").hasAnyRole("ADMIN", "EMPLEADO")
                        .pathMatchers(HttpMethod.GET, "/api/pruebas/en-curso").hasAnyRole("ADMIN", "EMPLEADO")
                        .pathMatchers(HttpMethod.POST, "/api/notificaciones/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .pathMatchers("/api/crud/vehiculos/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .pathMatchers("/api/crud/modelos/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .pathMatchers("/api/interesados/**").hasAnyRole("ADMIN", "EMPLEADO")

                        // VEHICULO
                        .pathMatchers(HttpMethod.POST, "/api/vehiculos/posicion/new").hasAnyRole("ADMIN", "VEHICULO")
                        .pathMatchers(HttpMethod.GET, "/api/vehiculos/restricciones-actuales").hasAnyRole("ADMIN", "VEHICULO")

                        // ADMIN
                        .pathMatchers("/api/reportes/**").hasRole("ADMIN")
                        .pathMatchers("/api/notificaciones/**").hasRole("ADMIN")
                        .pathMatchers("/api/**").hasRole("ADMIN")
                        // REGLA FINAL Y ESTRICTA: Denegar cualquier otra petición.
                        .anyExchange().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200","http://localhost:5500", "http://localhost:8080", "http://127.0.0.1:5500", "null"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Este método ahora devuelve un Flux<GrantedAuthority>, que es el tipo correcto.
    private ReactiveJwtAuthenticationConverter grantedAuthoritiesExtractor() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt ->
                Flux.fromIterable(extractAuthoritiesFromJwt(jwt)) // Usa Flux.fromIterable
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
        if (roles == null) { return List.of(); }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }
}