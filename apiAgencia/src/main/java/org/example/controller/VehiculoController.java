package org.example.controller;

import org.example.dtos.PosicionDto;
import org.example.dtos.externos.RestriccionesDto;
import org.example.service.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/vehiculos") // La URL base para este controlador
public class VehiculoController {
    private final VehiculoService vehiculoService;

    @Autowired
    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;

    }

    /**
     * Recibe una nueva posición de vehículo, la procesa de forma reactiva y
     * devuelve el resultado.
     * @param posicionDto El DTO con la información de la posición.
     * @return Un Mono que emite la ResponseEntity con el resultado o el error.
     */
    // Nota: Tenías @PostMapping duplicado, he dejado solo uno.
    @PostMapping("/posicion/new")
    public Mono<ResponseEntity<?>> crearPosicion(@RequestBody PosicionDto posicionDto) {
        // Se llama al servicio, que devuelve una "promesa" de que en el futuro llegará un PosicionDto (Mono<PosicionDto>). El flujo continúa de inmediato sin bloquearse.
        return vehiculoService.procesarPosicion(posicionDto)
                .map(posicionProcesada -> {
                    // Al castear posicionProcesada a (Object), ResponseEntity.ok()
                    // creará un ResponseEntity<Object>.
                    // El Mono después de este .map() es Mono<ResponseEntity<Object>>.
                    return ResponseEntity.ok((Object) posicionProcesada);
                })
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    // Este lambda devuelve Mono<ResponseEntity<String>>.
                    // Esto es compatible con el Mono<ResponseEntity<Object>> del flujo principal.
                    return Mono.just(ResponseEntity.badRequest().body(ex.getMessage()));
                })
                .onErrorResume(IllegalStateException.class, ex -> {
                    // Este lambda también devuelve Mono<ResponseEntity<String>>.
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage()));
                })
                // La cadena hasta aquí produce un Mono cuyo tipo de elemento es ResponseEntity<Object>
                // (ya que String es un Object, este es el supertipo común).
                // Ahora, transformamos explícitamente cada elemento a ResponseEntity<?>
                // para que el tipo final del Mono sea Mono<ResponseEntity<?>>.
                .map(responseEntity -> (ResponseEntity<?>) responseEntity);
    }

    // Este método escuchará peticiones POST en /api/vehiculos/posiciones
    /**
     * Endpoint de depuración para obtener las restricciones actuales. del serivicio externo.
     */
    @GetMapping("/restricciones-actuales")
    public Mono<RestriccionesDto> getRestriccionesActuales() {
        return vehiculoService.getRestriccionesActuales();
    }
}