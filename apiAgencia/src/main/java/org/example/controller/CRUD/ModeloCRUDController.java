package org.example.controller.CRUD;

import org.example.dtos.ModeloDto;
import org.example.service.CRUD.ModeloCRUDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crud/modelos")
public class ModeloCRUDController {

    private final ModeloCRUDService modeloService;

    @Autowired
    public ModeloCRUDController(ModeloCRUDService modeloService) {
        this.modeloService = modeloService;
    }

    @PostMapping
    public ResponseEntity<?> createModelo(@RequestBody ModeloDto dto) {
        try {
            ModeloDto nuevoModelo = modeloService.create(dto);
            return new ResponseEntity<>(nuevoModelo, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<ModeloDto>> getAllModelos() {
        return ResponseEntity.ok(modeloService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getModeloById(@PathVariable Long id) {
        try {
            ModeloDto modeloDto = modeloService.findById(id);
            return ResponseEntity.ok(modeloDto);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateModelo(@PathVariable Long id, @RequestBody ModeloDto dto) {
        try {
            ModeloDto updatedModelo = modeloService.update(id, dto);
            return ResponseEntity.ok(updatedModelo);
        } catch (IllegalArgumentException e) {
            // Puede ser 404 si no encuentra el modelo, o 400 si no encuentra la marca.
            if (e.getMessage().contains("Modelo no encontrado")) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteModelo(@PathVariable Long id) {
        try {
            modeloService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}