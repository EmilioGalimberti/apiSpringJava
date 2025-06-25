package org.example.service.CRUD;

import org.example.dtos.EmpleadoDto;
import org.example.models.Empleado;
import org.example.repositories.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class EmpleadoCRUDService {

    private final EmpleadoRepository empleadoRepository;

    @Autowired
    public EmpleadoCRUDService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional
    public EmpleadoDto create(EmpleadoDto empleadoDto) {
        Empleado empleado = new Empleado();
        empleado.setNombre(empleadoDto.getNombre());
        empleado.setApellido(empleadoDto.getApellido());
        empleado.setTelefonoContacto(empleadoDto.getTelefonoContacto());
        Empleado savedEmpleado = empleadoRepository.save(empleado);
        return new EmpleadoDto(savedEmpleado);
    }

    public List<EmpleadoDto> findAll() {
        return StreamSupport.stream(empleadoRepository.findAll().spliterator(), false)
                .map(EmpleadoDto::new)
                .toList();
    }

    public EmpleadoDto findById(Long id) {
        return empleadoRepository.findById(id)
                .map(EmpleadoDto::new)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado con legajo: " + id));
    }

    @Transactional
    public EmpleadoDto update(Long id, EmpleadoDto empleadoDto) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado con legajo: " + id));
        empleado.setNombre(empleadoDto.getNombre());
        empleado.setApellido(empleadoDto.getApellido());
        empleado.setTelefonoContacto(empleadoDto.getTelefonoContacto());
        Empleado updatedEmpleado = empleadoRepository.save(empleado);
        return new EmpleadoDto(updatedEmpleado);
    }

    @Transactional
    public void delete(Long id) {
        if (!empleadoRepository.existsById(id)) {
            throw new IllegalArgumentException("Empleado no encontrado con legajo: " + id);
        }
        empleadoRepository.deleteById(id);
    }
}