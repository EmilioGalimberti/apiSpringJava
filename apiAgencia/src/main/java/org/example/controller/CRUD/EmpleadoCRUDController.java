package org.example.controller.CRUD;

import org.example.dtos.EmpleadoDto;
import org.example.service.CRUD.EmpleadoCRUDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoCRUDController {

    private final EmpleadoCRUDService empleadoCRUDService;

    @Autowired
    public EmpleadoCRUDController(EmpleadoCRUDService empleadoCRUDService) {
        this.empleadoCRUDService = empleadoCRUDService;
    }

    @PostMapping
    public ResponseEntity<EmpleadoDto> createEmpleado(@RequestBody EmpleadoDto empleadoDto) {
        EmpleadoDto nuevoEmpleado = empleadoCRUDService.create(empleadoDto);
        return new ResponseEntity<>(nuevoEmpleado, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EmpleadoDto>> getAllEmpleados() {
        return ResponseEntity.ok(empleadoCRUDService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmpleadoById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(empleadoCRUDService.findById(id));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmpleado(@PathVariable Long id, @RequestBody EmpleadoDto empleadoDto) {
        try {
            return ResponseEntity.ok(empleadoCRUDService.update(id, empleadoDto));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmpleado(@PathVariable Long id) {
        try {
            empleadoCRUDService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}