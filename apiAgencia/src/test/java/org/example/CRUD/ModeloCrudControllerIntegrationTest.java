package org.example.CRUD;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Main;
import org.example.dtos.ModeloDto;
import org.example.models.Marca;
import org.example.models.Modelo;
import org.example.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ModeloCrudControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModeloRepository modeloRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository; // Necesario para limpiar la BD
    @Autowired private PosicionRepository posicionRepository;
    @Autowired private PruebaRepository pruebaRepository;

    private Marca marcaDePrueba;
    private Modelo modeloDePrueba;

    // Se ejecuta antes de cada test para preparar los datos.
    @BeforeEach
    void setUp() {
        //limpieza
        posicionRepository.deleteAll();
        pruebaRepository.deleteAll();
        // Limpiamos las tablas en el orden correcto para evitar errores de constraints.
        // Un Vehículo depende de un Modelo, y un Modelo de una Marca.
        vehiculoRepository.deleteAll();
        modeloRepository.deleteAll();
        marcaRepository.deleteAll();

        // Creamos una marca base para poder crear modelos.
        marcaDePrueba = new Marca(null, "Toyota", null);
        marcaDePrueba = marcaRepository.save(marcaDePrueba);

        // Creamos un modelo base para los tests de GET, PUT y DELETE.
        modeloDePrueba = new Modelo(null, marcaDePrueba, "Hilux", null);
        modeloDePrueba = modeloRepository.save(modeloDePrueba);
    }

    @Test
    void createModelo_cuandoDatosSonValidos_deberiaRetornar201Creado() throws Exception {
        // Arrange
        ModeloDto nuevoModeloDto = new ModeloDto();
        nuevoModeloDto.setDescripcion("Corolla");
        nuevoModeloDto.setIdMarca(marcaDePrueba.getId());

        // Act & Assert
        mockMvc.perform(post("/api/crud/modelos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoModeloDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.descripcion").value("Corolla"))
                .andExpect(jsonPath("$.nombreMarca").value("Toyota"));
    }

    @Test
    void createModelo_cuandoMarcaNoExiste_deberiaRetornar400BadRequest() throws Exception {
        // Arrange
        Integer idMarcaInexistente = -99;
        ModeloDto nuevoModeloDto = new ModeloDto();
        nuevoModeloDto.setDescripcion("Modelo Fantasma");
        nuevoModeloDto.setIdMarca(idMarcaInexistente);

        // Act & Assert
        mockMvc.perform(post("/api/crud/modelos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoModeloDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Marca no encontrada con ID: " + idMarcaInexistente));
    }

    @Test
    void getAllModelos_cuandoExistenModelos_deberiaRetornar200OkConLista() throws Exception {
        // Arrange: el modeloDePrueba ya fue creado en setUp()

        // Act & Assert
        mockMvc.perform(get("/api/crud/modelos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].descripcion").value("Hilux"));
    }

    @Test
    void getModeloById_cuandoIdExiste_deberiaRetornar200OkConModelo() throws Exception {
        // Arrange
        Long idExistente = modeloDePrueba.getId();

        // Act & Assert
        mockMvc.perform(get("/api/crud/modelos/{id}", idExistente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idExistente))
                .andExpect(jsonPath("$.descripcion").value("Hilux"));
    }

    @Test
    void updateModelo_cuandoIdExiste_deberiaRetornar200OkConModeloActualizado() throws Exception {
        // Arrange
        Long idExistente = modeloDePrueba.getId();
        ModeloDto datosParaActualizar = new ModeloDto();
        datosParaActualizar.setDescripcion("Hilux SRX");
        datosParaActualizar.setIdMarca(marcaDePrueba.getId());

        // Act & Assert
        mockMvc.perform(put("/api/crud/modelos/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosParaActualizar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idExistente))
                .andExpect(jsonPath("$.descripcion").value("Hilux SRX"));
    }

    @Test
    void updateModelo_cuandoModeloNoExiste_deberiaRetornar404NotFound() throws Exception {
        // Arrange
        Long idModeloInexistente = -99L; // Un ID que sabemos que no existe.

        // Creamos un DTO con datos válidos, pero lo enviaremos a un ID que no existe.
        ModeloDto datosParaActualizar = new ModeloDto();
        datosParaActualizar.setDescripcion("Actualizacion Fallida");
        datosParaActualizar.setIdMarca(marcaDePrueba.getId());

        // Act & Assert
        mockMvc.perform(put("/api/crud/modelos/{id}", idModeloInexistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosParaActualizar)))
                .andExpect(status().isNotFound()) // Esperamos un 404 Not Found
                .andExpect(content().string("Modelo no encontrado con ID: " + idModeloInexistente));
    }

    @Test
    void updateModelo_cuandoMarcaAsociadaNoExiste_deberiaRetornar400BadRequest() throws Exception {
        // Arrange
        Long idModeloExistente = modeloDePrueba.getId(); // Usamos un modelo que sí existe.
        Integer idMarcaInexistente = -99; // Pero intentamos asociarlo a una marca que no existe.

        ModeloDto datosParaActualizar = new ModeloDto();
        datosParaActualizar.setDescripcion("Actualizacion con Marca Invalida");
        datosParaActualizar.setIdMarca(idMarcaInexistente);

        // Act & Assert
        mockMvc.perform(put("/api/crud/modelos/{id}", idModeloExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosParaActualizar)))
                .andExpect(status().isBadRequest()) // Esperamos un 400 Bad Request
                .andExpect(content().string("Marca no encontrada con ID: " + idMarcaInexistente));
    }

    @Test
    void deleteModelo_cuandoIdExiste_deberiaRetornar204NoContent() throws Exception {
        // Arrange
        Long idExistente = modeloDePrueba.getId();

        // Act
        mockMvc.perform(delete("/api/crud/modelos/{id}", idExistente))
                .andExpect(status().isNoContent());

        // Assert
        // Verificamos que el modelo realmente fue borrado de la base de datos.
        assertThat(modeloRepository.existsById(idExistente)).isFalse();
    }

    @Test
    void deleteModelo_cuandoIdNoExiste_deberiaRetornar404NotFound() throws Exception {
        // Arrange
        Long idInexistente = -99L;

        // Act & Assert
        mockMvc.perform(delete("/api/crud/modelos/{id}", idInexistente))
                .andExpect(status().isNotFound());
    }
}