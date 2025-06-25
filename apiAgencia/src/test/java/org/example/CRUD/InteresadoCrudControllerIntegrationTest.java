package org.example.CRUD;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Main;
import org.example.dtos.InteresadoDto;
import org.example.models.Interesado;
import org.example.repositories.InteresadoRepository;
import org.example.repositories.PruebaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class InteresadoCrudControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InteresadoRepository interesadoRepository;

    @Autowired
    private PruebaRepository pruebaRepository; // Necesario para la limpieza

    private Interesado interesadoDePrueba;

    // Se ejecuta antes de cada test para preparar datos base.
    @BeforeEach
    void setUp() {
        // Limpiamos las tablas "hijas" primero para evitar errores de constraints.
        pruebaRepository.deleteAll();
        // Ahora sí, podemos limpiar la tabla principal.
        interesadoRepository.deleteAll();

        // Creamos un interesado válido para usar en los tests de GET, PUT y DELETE.
        interesadoDePrueba = new Interesado();
        interesadoDePrueba.setTipoDocumento("DNI");
        interesadoDePrueba.setDocumento("12345678");
        interesadoDePrueba.setNombre("Juan");
        interesadoDePrueba.setApellido("Perez");
        interesadoDePrueba.setRestringido(false);
        interesadoDePrueba.setNroLicencia(11111);
        interesadoDePrueba.setFechaVencimientoLicencia(new Date(System.currentTimeMillis() + 86400000L * 30)); // Licencia vigente por 30 días
        interesadoDePrueba = interesadoRepository.save(interesadoDePrueba);
    }

    @Test
    void createInteresado_cuandoDatosSonValidos_deberiaRetornar201Creado() throws Exception {
        // Arrange
        InteresadoDto nuevoInteresadoDto = new InteresadoDto();
        nuevoInteresadoDto.setTipoDocumento("DNI");
        nuevoInteresadoDto.setDocumento("98765432");
        nuevoInteresadoDto.setNombre("Maria");
        nuevoInteresadoDto.setApellido("Gomez");
        nuevoInteresadoDto.setRestringido(false);
        nuevoInteresadoDto.setNroLicencia(22222);
        nuevoInteresadoDto.setFechaVencimientoLicencia(new Date(System.currentTimeMillis() + 86400000L * 30));

        // Act & Assert
        mockMvc.perform(post("/api/interesados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoInteresadoDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.documento").value("98765432"))
                .andExpect(jsonPath("$.nombre").value("Maria"));
    }

    @Test
    void getAllInteresados_cuandoExistenInteresados_deberiaRetornar200OkConLista() throws Exception {
        // Arrange: el interesadoDePrueba ya fue creado en setUp()

        // Act & Assert
        mockMvc.perform(get("/api/interesados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].documento").value("12345678"));
    }

    @Test
    void getInteresadoById_cuandoIdExiste_deberiaRetornar200OkConInteresado() throws Exception {
        // Arrange
        Long idExistente = interesadoDePrueba.getId();

        // Act & Assert
        mockMvc.perform(get("/api/interesados/{id}", idExistente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idExistente))
                .andExpect(jsonPath("$.apellido").value("Perez"));
    }

    @Test
    void getInteresadoById_cuandoIdNoExiste_deberiaRetornar404NotFound() throws Exception {
        // Arrange
        Long idInexistente = -99L;

        // Act & Assert
        mockMvc.perform(get("/api/interesados/{id}", idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Interesado no encontrado con ID: " + idInexistente));
    }

    @Test
    void updateInteresado_cuandoIdExiste_deberiaRetornar200OkConInteresadoActualizado() throws Exception {
        // Arrange
        Long idExistente = interesadoDePrueba.getId();
        InteresadoDto datosParaActualizar = new InteresadoDto();
        datosParaActualizar.setTipoDocumento("DNI");
        datosParaActualizar.setDocumento("12345678");
        datosParaActualizar.setNombre("Juan Carlos"); // Nombre actualizado
        datosParaActualizar.setApellido("Perez");
        datosParaActualizar.setRestringido(true); // Campo actualizado
        datosParaActualizar.setNroLicencia(11111);
        datosParaActualizar.setFechaVencimientoLicencia(interesadoDePrueba.getFechaVencimientoLicencia());

        // Act & Assert
        mockMvc.perform(put("/api/interesados/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosParaActualizar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Carlos"))
                .andExpect(jsonPath("$.restringido").value(true));
    }

    @Test
    void deleteInteresado_cuandoIdExiste_deberiaRetornar204NoContent() throws Exception {
        // Arrange
        Long idExistente = interesadoDePrueba.getId();

        // Act
        mockMvc.perform(delete("/api/interesados/{id}", idExistente))
                .andExpect(status().isNoContent());

        // Assert
        // Verificamos que el interesado realmente fue borrado de la base de datos.
        assertThat(interesadoRepository.existsById(idExistente)).isFalse();
    }

    @Test
    void deleteInteresado_cuandoIdNoExiste_deberiaRetornar404NotFound() throws Exception {
        // Arrange
        Long idInexistente = -99L;

        // Act & Assert
        mockMvc.perform(delete("/api/interesados/{id}", idInexistente))
                .andExpect(status().isNotFound());
    }
}