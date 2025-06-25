package org.example.service.CRUD;

import org.example.dtos.VehiculoDto;
import org.example.models.Modelo;
import org.example.models.Vehiculo;
import org.example.repositories.ModeloRepository;
import org.example.repositories.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class VehiculoCrudService {

    private final VehiculoRepository vehiculoRepository;
    private final ModeloRepository modeloRepository;

    @Autowired
    public VehiculoCrudService(VehiculoRepository vehiculoRepository, ModeloRepository modeloRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.modeloRepository = modeloRepository;
    }

    @Transactional
    public VehiculoDto create(VehiculoDto vehiculoDto) {
        // Buscamos la entidad Modelo a partir del ID que viene en el DTO.
        Modelo modelo = modeloRepository.findById(vehiculoDto.getIdModelo())
                .orElseThrow(() -> new IllegalArgumentException("Modelo no encontrado con ID: " + vehiculoDto.getIdModelo()));

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPatente(vehiculoDto.getPatente());
        vehiculo.setModelo(modelo);

        Vehiculo savedVehiculo = vehiculoRepository.save(vehiculo);
        return new VehiculoDto(savedVehiculo);
    }

    public List<VehiculoDto> findAll() {
        return StreamSupport.stream(vehiculoRepository.findAll().spliterator(), false)
                .map(VehiculoDto::new)
                .toList();
    }

    public VehiculoDto findById(Integer id) {
        return vehiculoRepository.findById(id)
                .map(VehiculoDto::new)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));
    }

    @Transactional
    public VehiculoDto update(Integer id, VehiculoDto vehiculoDto) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));

        Modelo modelo = modeloRepository.findById(vehiculoDto.getIdModelo())
                .orElseThrow(() -> new IllegalArgumentException("Modelo no encontrado con ID: " + vehiculoDto.getIdModelo()));

        vehiculo.setPatente(vehiculoDto.getPatente());
        vehiculo.setModelo(modelo);

        Vehiculo updatedVehiculo = vehiculoRepository.save(vehiculo);
        return new VehiculoDto(updatedVehiculo);
    }

    @Transactional
    public void delete(Integer id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehículo no encontrado con ID: " + id);
        }
        vehiculoRepository.deleteById(id);
    }
}