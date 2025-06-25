package org.example.service.CRUD;

import org.example.dtos.InteresadoDto;
import org.example.models.Interesado;
import org.example.repositories.InteresadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class InteresadoCRUDService {

    private final InteresadoRepository interesadoRepository;

    @Autowired
    public InteresadoCRUDService(InteresadoRepository interesadoRepository) {
        this.interesadoRepository = interesadoRepository;
    }

    @Transactional
    public InteresadoDto create(InteresadoDto dto) {
        Interesado interesado = new Interesado();
        // Mapeo de DTO a Entidad
        interesado.setTipoDocumento(dto.getTipoDocumento());
        interesado.setDocumento(dto.getDocumento());
        interesado.setNombre(dto.getNombre());
        interesado.setApellido(dto.getApellido());
        interesado.setRestringido(dto.getRestringido());
        interesado.setNroLicencia(dto.getNroLicencia());
        interesado.setFechaVencimientoLicencia(dto.getFechaVencimientoLicencia());

        Interesado savedInteresado = interesadoRepository.save(interesado);
        return new InteresadoDto(savedInteresado);
    }

    public List<InteresadoDto> findAll() {
        return StreamSupport.stream(interesadoRepository.findAll().spliterator(), false)
                .map(InteresadoDto::new)
                .toList();
    }


    /**
     * Busca un interesado por su ID.
     * @param id El ID del interesado a buscar.
     * @return El DTO del interesado encontrado.
     * @throws IllegalArgumentException si no se encuentra un interesado con ese ID.
     */
    public InteresadoDto findById(Long id) {
        return interesadoRepository.findById(id)
                .map(InteresadoDto::new)
                .orElseThrow(() -> new IllegalArgumentException("Interesado no encontrado con ID: " + id));
    }

    /**
     * Actualiza un interesado existente.
     * @param id El ID del interesado a actualizar.
     * @param dto El DTO con los nuevos datos.
     * @return El DTO del interesado actualizado.
     * @throws IllegalArgumentException si el interesado no se encuentra.
     */
    @Transactional
    public InteresadoDto update(Long id, InteresadoDto dto) {
        Interesado interesado = interesadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interesado no encontrado con ID: " + id));

        // Actualiza los campos de la entidad con los datos del DTO
        interesado.setTipoDocumento(dto.getTipoDocumento());
        interesado.setDocumento(dto.getDocumento());
        interesado.setNombre(dto.getNombre());
        interesado.setApellido(dto.getApellido());
        interesado.setRestringido(dto.getRestringido() != null && dto.getRestringido());
        interesado.setNroLicencia(dto.getNroLicencia());
        interesado.setFechaVencimientoLicencia(dto.getFechaVencimientoLicencia());

        Interesado updatedInteresado = interesadoRepository.save(interesado);
        return new InteresadoDto(updatedInteresado);
    }

    /**
     * Elimina un interesado por su ID.
     * @param id El ID del interesado a eliminar.
     * @throws IllegalArgumentException si el interesado no se encuentra.
     */
    @Transactional
    public void delete(Long id) {
        if (!interesadoRepository.existsById(id)) {
            throw new IllegalArgumentException("Interesado no encontrado con ID: " + id);
        }
        interesadoRepository.deleteById(id);
    }



}