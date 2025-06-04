package org.example.controller;

import org.example.dtos.PruebaDto;
import org.example.service.PruebaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/pruebas")
public class PruebaController {

    private final PruebaService pruebaService;

    @Autowired // Opcional si solo tienes un constructor a partir de Spring 4.3
    public PruebaController(PruebaService pruebaService) {
        this.pruebaService = pruebaService;
    }

    // crear una prueba  | Punto A)
    /**
     * Endpoint para crear una nueva prueba.
     * Valida que el cliente no tenga la licencia vencida ni esté restringido,
     * y que el vehículo no esté siendo probado actualmente. [cite: 13, 15, 43, 45]
     *
     * @param pruebaDTO Los datos de la prueba a crear.
     * @return ResponseEntity con la PruebaDto creada y estado HTTP 201 (Created),
     * o un estado de error si la validación falla.
     */
    //@PostMapping: Mapea las solicitudes HTTP POST a la ruta /api/pruebas
    @PostMapping("/crear")
    //@RequestBody PruebaDto pruebaDto: Spring convierte el cuerpo JSON de la solicitud en un objeto PruebaDto.
    public ResponseEntity<?> crearPrueba(@RequestBody PruebaDto pruebaDTO) {
        try {
            PruebaDto nuevaPrueba = pruebaService.crearPrueba(pruebaDTO);
            // Para cumplir estrictamente con REST, podrías incluir la URI del nuevo recurso:
            return ResponseEntity.created(URI.create("/api/pruebas/" + nuevaPrueba.getId())).body(nuevaPrueba);
            //return new ResponseEntity<>(nuevaPrueba, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            // Esta excepción es lanzada por PruebaService si alguna validación falla
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Para cualquier otra excepción inesperada
            // Es buena idea loggear esta excepción también
            return new ResponseEntity<>("Ocurrió un error inesperado al crear la prueba.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
