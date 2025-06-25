package org.example.controller.CRUD;

import org.example.dtos.InteresadoDto;
import org.example.service.CRUD.InteresadoCRUDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interesados")
public class InteresadoCRUDController {

    private final InteresadoCRUDService interesadoCRUDService;

    @Autowired
    public InteresadoCRUDController(InteresadoCRUDService interesadoCRUDService) {
        this.interesadoCRUDService = interesadoCRUDService;
    }

    @PostMapping
    public ResponseEntity<InteresadoDto> createInteresado(@RequestBody InteresadoDto dto) {
        return new ResponseEntity<>(interesadoCRUDService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<InteresadoDto>> getAllInteresados() {
        return ResponseEntity.ok(interesadoCRUDService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInteresadoById(@PathVariable Long id) {
        try {
            InteresadoDto interesadoDto = interesadoCRUDService.findById(id);
            return ResponseEntity.ok(interesadoDto);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInteresado(@PathVariable Long id, @RequestBody InteresadoDto dto) {
        try {
            InteresadoDto updatedInteresado = interesadoCRUDService.update(id, dto);
            return ResponseEntity.ok(updatedInteresado);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInteresado(@PathVariable Long id) {
        try {
            interesadoCRUDService.delete(id);
            // Para DELETE exitoso, se devuelve 204 No Content
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}