package org.example.controller;

import org.example.dtos.PosicionDto;
import org.example.dtos.externos.RestriccionesDto;
import org.example.service.ExternalApisService;
import org.example.service.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/vehiculos") // La URL base para este controlador
public class VehiculoController {

    private final VehiculoService vehiculoService;

    //es para probar
    private final ExternalApisService externalApisService;


    @Autowired
    public VehiculoController(VehiculoService vehiculoService, ExternalApisService externalApisService) {
        this.vehiculoService = vehiculoService;
        this.externalApisService = externalApisService;

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