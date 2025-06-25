package org.example.CRUD;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Main;
import org.example.dtos.VehiculoDto;
import org.example.models.Marca;
import org.example.models.Modelo;
import org.example.models.Vehiculo;
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
class VehiculoCrudControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Inyectamos todos los repositorios para la preparación y limpieza
    @Autowired private VehiculoRepository vehiculoRepository;
    @Autowired private ModeloRepository modeloRepository;
    @Autowired private MarcaRepository marcaRepository;
    @Autowired private PruebaRepository pruebaRepository;
    @Autowired private PosicionRepository posicionRepository;


    private Marca marcaDePrueba;
    private Modelo modeloDePrueba;
    private Vehiculo vehiculoDePrueba;

    @BeforeEach
    void setUp() {
        // Limpiamos las tablas en el orden correcto de dependencia (hijos primero)
        posicionRepository.deleteAll();
        pruebaRepository.deleteAll();
        vehiculoRepository.deleteAll();
        modeloRepository.deleteAll();
        marcaRepository.deleteAll();

        // Creamos una Marca y un Modelo base para poder crear Vehículos
        marcaDePrueba = marcaRepository.save(new Marca(null, "Toyota", null));
        modeloDePrueba = modeloRepository.save(new Modelo(null, marcaDePrueba, "Hilux", null));

        // Creamos un vehículo base para los tests de GET, PUT y DELETE
        vehiculoDePrueba = vehiculoRepository.save(new Vehiculo(null, "ABC123CD", modeloDePrueba, null, null));
    }

    @Test
    void createVehiculo_cuandoDatosSonValidos_deberiaRetornar201Creado() throws Exception {
        // Arrange
        VehiculoDto nuevoVehiculoDto = new VehiculoDto();
        nuevoVehiculoDto.setPatente("XYZ987FG");
        nuevoVehiculoDto.setIdModelo(modeloDePrueba.getId());

        // Act & Assert
        mockMvc.perform(post("/api/crud/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoVehiculoDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.patente").value("XYZ987FG"))
                .andExpect(jsonPath("$.idModelo").value(modeloDePrueba.getId()));
    }

    @Test
    void createVehiculo_cuandoModeloNoExiste_deberiaRetornar400BadRequest() throws Exception {
        // Arrange
        long idModeloInexistente = -99L;
        VehiculoDto nuevoVehiculoDto = new VehiculoDto();
        nuevoVehiculoDto.setPatente("FAIL123");
        nuevoVehiculoDto.setIdModelo(idModeloInexistente);

        // Act & Assert
        mockMvc.perform(post("/api/crud/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoVehiculoDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Modelo no encontrado con ID: " + idModeloInexistente));
    }

    @Test
    void getAllVehiculos_cuandoExistenVehiculos_deberiaRetornar200OkConLista() throws Exception {
        // Arrange: el vehiculoDePrueba ya fue creado en setUp()

        // Act & Assert
        mockMvc.perform(get("/api/crud/vehiculos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].patente").value("ABC123CD"));
    }

    @Test
    void getVehiculoById_cuandoIdExiste_deberiaRetornar200OkConVehiculo() throws Exception {
        // Arrange
        Integer idExistente = vehiculoDePrueba.getId();

        // Act & Assert
        mockMvc.perform(get("/api/crud/vehiculos/{id}", idExistente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idExistente))
                .andExpect(jsonPath("$.patente").value("ABC123CD"));
    }

    @Test
    void updateVehiculo_cuandoIdExiste_deberiaRetornar200OkConVehiculoActualizado() throws Exception {
        // Arrange
        Integer idExistente = vehiculoDePrueba.getId();
        VehiculoDto datosParaActualizar = new VehiculoDto();
        datosParaActualizar.setPatente("DEF456GH"); // Patente actualizada
        datosParaActualizar.setIdModelo(modeloDePrueba.getId());

        // Act & Assert
        mockMvc.perform(put("/api/crud/vehiculos/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosParaActualizar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idExistente))
                .andExpect(jsonPath("$.patente").value("DEF456GH"));
    }

    @Test
    void deleteVehiculo_cuandoIdExiste_deberiaRetornar204NoContent() throws Exception {
        // Arrange
        Integer idExistente = vehiculoDePrueba.getId();

        // Act
        mockMvc.perform(delete("/api/crud/vehiculos/{id}", idExistente))
                .andExpect(status().isNoContent());

        // Assert
        // Verificamos que el vehículo realmente fue borrado de la base de datos.
        assertThat(vehiculoRepository.existsById(idExistente)).isFalse();
    }

    @Test
    void deleteVehiculo_cuandoIdNoExiste_deberiaRetornar404NotFound() throws Exception {
        // Arrange
        Integer idInexistente = -99;

        // Act & Assert
        mockMvc.perform(delete("/api/crud/vehiculos/{id}", idInexistente))
                .andExpect(status().isNotFound());
    }
}