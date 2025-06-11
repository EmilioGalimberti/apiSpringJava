package org.example.controller;

import org.example.dtos.PruebaDto;
import org.example.service.PruebaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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
            // Devuelve una respuesta HTTP 201 (Created) al cliente.
            // Incluye la URI del nuevo recurso creado en la cabecera 'Location'
            // y el DTO de la prueba recién creada (convertido a JSON) en el cuerpo de la respuesta.
            // Ejemplo cabecera Location: /api/pruebas/123
            return ResponseEntity.created(URI.create("/api/pruebas/" + nuevaPrueba.getId())).body(nuevaPrueba);

        } catch (IllegalArgumentException e) {
            // Esta excepción es lanzada por PruebaService si alguna validación falla
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Para cualquier otra excepción inesperada
            // Es buena idea loggear esta excepción también
            return new ResponseEntity<>("Ocurrió un error inesperado al crear la prueba.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Endpoint para eliminar una prueba
    /**
     * Endpoint para eliminar una prueba por su ID.
     *
     * @param id El ID de la prueba a eliminar.
     * @return ResponseEntity con estado 204 (No Content) si se elimina correctamente,
     * o 404 (Not Found) si la prueba no existe.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePrueba(@PathVariable Integer id) {
        try {
            pruebaService.deletePrueba(id);
            // Si la eliminación es exitosa, devuelve un 204 No Content.
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // Si el servicio lanza la excepción porque no encontró la prueba,
            // devolvemos un 404 Not Found.
            return ResponseEntity.notFound().build();
        }
    }

    //endponint para obtener pruebas en curso b)
    @GetMapping("/en-curso")
    public ResponseEntity<List<PruebaDto>> getPruebasEnCurso() {
        List<PruebaDto> pruebas = pruebaService.getPruebasEnCurso();

        if (pruebas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(pruebas);
    }

    //endpoint para finalizar prueba, c)
    /**
     * Endpoint para finalizar una prueba en curso.
     * Establece la fecha y hora de fin y agrega un comentario.
     *
     * @param id         El ID de la prueba a finalizar, obtenido de la ruta (path).
     * @param comentario El comentario a agregar, obtenido de los parámetros de la solicitud.
     * @return ResponseEntity con la PruebaDto actualizada y estado 200 (OK),
     * o un estado de error si la prueba no se encuentra o ya estaba finalizada.
     */
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizarPrueba(@PathVariable Integer id, @RequestParam String comentario) {
        try {
            PruebaDto pruebaActualizada = pruebaService.finalizarPrueba(id, comentario);
            return ResponseEntity.ok(pruebaActualizada);
        } catch (IllegalArgumentException e) {
            // Si la prueba no se encuentra, el servicio lanza IllegalArgumentException.
            // Lo traducimos a un 404 Not Found si el mensaje lo indica.
            if (e.getMessage().contains("no encontrada")) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }
            // Si la prueba ya estaba finalizada, devolvemos un 400 Bad Request.
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
