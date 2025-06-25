package org.example.controller.CRUD;

import org.example.dtos.VehiculoDto;
import org.example.service.CRUD.VehiculoCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crud/vehiculos") // Usamos una ruta diferente para no colisionar
public class VehiculoCrudController {

    private final VehiculoCrudService vehiculoCrudService;

    @Autowired
    public VehiculoCrudController(VehiculoCrudService vehiculoCrudService) {
        this.vehiculoCrudService = vehiculoCrudService;
    }

    @PostMapping
    public ResponseEntity<?> createVehiculo(@RequestBody VehiculoDto vehiculoDto) {
        try {
            VehiculoDto nuevoVehiculo = vehiculoCrudService.create(vehiculoDto);
            return new ResponseEntity<>(nuevoVehiculo, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<VehiculoDto>> getAllVehiculos() {
        return ResponseEntity.ok(vehiculoCrudService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVehiculoById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(vehiculoCrudService.findById(id));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehiculo(@PathVariable Integer id, @RequestBody VehiculoDto vehiculoDto) {
        try {
            return ResponseEntity.ok(vehiculoCrudService.update(id, vehiculoDto));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehiculo(@PathVariable Integer id) {
        try {
            vehiculoCrudService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}