package org.example.service.CRUD;

import org.example.dtos.ModeloDto;
import org.example.models.Marca;
import org.example.models.Modelo;
import org.example.repositories.MarcaRepository;
import org.example.repositories.ModeloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class ModeloCRUDService {

    private final ModeloRepository modeloRepository;
    private final MarcaRepository marcaRepository;

    @Autowired
    public ModeloCRUDService(ModeloRepository modeloRepository, MarcaRepository marcaRepository) {
        this.modeloRepository = modeloRepository;
        this.marcaRepository = marcaRepository;
    }

    /**
     * Crea un nuevo modelo, asociándolo a una marca existente.
     * @param dto El DTO con los datos del modelo a crear.
     * @return El DTO del modelo recién creado.
     * @throws IllegalArgumentException si la marca especificada no existe.
     */
    @Transactional
    public ModeloDto create(ModeloDto dto) {
        // Se busca la entidad Marca a partir del ID que viene en el DTO.
        Marca marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada con ID: " + dto.getIdMarca()));

        Modelo modelo = new Modelo();
        modelo.setDescripcion(dto.getDescripcion());
        modelo.setMarca(marca);

        Modelo savedModelo = modeloRepository.save(modelo);
        return new ModeloDto(savedModelo);
    }

    /**
     * Devuelve todos los modelos registrados.
     * @return Una lista de DTOs de todos los modelos.
     */
    public List<ModeloDto> findAll() {
        return StreamSupport.stream(modeloRepository.findAll().spliterator(), false)
                .map(ModeloDto::new)
                .toList();
    }

    /**
     * Busca un modelo por su ID.
     * @param id El ID del modelo a buscar.
     * @return El DTO del modelo encontrado.
     * @throws IllegalArgumentException si no se encuentra un modelo con ese ID.
     */
    public ModeloDto findById(Long id) {
        return modeloRepository.findById(id)
                .map(ModeloDto::new)
                .orElseThrow(() -> new IllegalArgumentException("Modelo no encontrado con ID: " + id));
    }

    /**
     * Actualiza un modelo existente.
     * @param id El ID del modelo a actualizar.
     * @param dto El DTO con los nuevos datos.
     * @return El DTO del modelo actualizado.
     * @throws IllegalArgumentException si el modelo o la marca no se encuentran.
     */
    @Transactional
    public ModeloDto update(Long id, ModeloDto dto) {
        Modelo modelo = modeloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Modelo no encontrado con ID: " + id));

        Marca marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada con ID: " + dto.getIdMarca()));

        modelo.setDescripcion(dto.getDescripcion());
        modelo.setMarca(marca);

        Modelo updatedModelo = modeloRepository.save(modelo);
        return new ModeloDto(updatedModelo);
    }

    /**
     * Elimina un modelo por su ID.
     * @param id El ID del modelo a eliminar.
     * @throws IllegalArgumentException si el modelo no se encuentra.
     */
    @Transactional
    public void delete(Long id) {
        if (!modeloRepository.existsById(id)) {
            throw new IllegalArgumentException("Modelo no encontrado con ID: " + id);
        }
        modeloRepository.deleteById(id);
    }
}