package org.example.service;


import org.example.dtos.externos.RestriccionesDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ExternalApisService {

    private final WebClient webClient;

    @Value("${agencia.microservicio-restricciones.url}")
    private String urlRestricciones;

    //@Value(("${tpi-agencia.microservicio-notificaciones.url}"))
    //private String urlNotificaciones;

    @Autowired
    public ExternalApisService(WebClient webClient) {
        this.webClient = webClient;
    }

    // AHORA: El método devuelve un Mono<RestriccionesDto>, la "promesa" del objeto.
    // La anotación @Cacheable sigue funcionando, pero cacheará el Mono.
    @Cacheable("restrictionsApiCache")
    public Mono<RestriccionesDto> getRestricciones() {
        // Simplemente retornamos el Mono. ¡No más .block()!
        return webClient.get()
                .uri(urlRestricciones)
                .retrieve()
                .bodyToMono(RestriccionesDto.class);
    }

    // AHORA: Devolvemos un Flux, que es un "stream" de notificaciones.
    // Esto es más idiomático en reactivo que devolver un Mono<List<...>>.
//    public Flux<NotificacionRadioExcedidoDto> getNotificacionesRadioExcedido() {
//        // Simplemente retornamos el Flux.
//        return webClient.get()
//                .uri(urlNotificaciones + "/notificaciones/seguridad/radio-excedido")
//                .retrieve()
//                .bodyToFlux(NotificacionRadioExcedidoDto.class);
//    }
}