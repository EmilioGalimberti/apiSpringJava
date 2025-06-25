package org.example.CRUD;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Main;
import org.example.dtos.EmpleadoDto;
import org.example.models.Empleado;
import org.example.repositories.EmpleadoRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class EmpleadoCrudControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    private Empleado empleadoDePrueba;
    @Autowired
    private PruebaRepository pruebaRepository;

    // Se ejecuta antes de cada test para preparar datos base.
    @BeforeEach
    void setUp() {
        pruebaRepository.deleteAll();

        empleadoRepository.deleteAll(); // Limpiamos para asegurar un estado inicial
        empleadoDePrueba = new Empleado(null, "Juan", "Perez", 1122334455, null);
        empleadoDePrueba = empleadoRepository.save(empleadoDePrueba);
    }

    @Test
    void createEmpleado_cuandoDatosSonValidos_deberiaRetornar201Creado() throws Exception {
        // Arrange
        EmpleadoDto nuevoEmpleadoDto = new EmpleadoDto(); // Se usa el constructor vacío
        nuevoEmpleadoDto.setNombre("Maria");
        nuevoEmpleadoDto.setApellido("Gomez");
        nuevoEmpleadoDto.setTelefonoContacto(99887766);

        // Act & Assert
        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoEmpleadoDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.legajo").exists())
                .andExpect(jsonPath("$.nombre").value("Maria"))
                .andExpect(jsonPath("$.apellido").value("Gomez"));
    }

    @Test
    void getAllEmpleados_cuandoExistenEmpleados_deberiaRetornar200OkConLista() throws Exception {
        // Arrange: el empleadoDePrueba ya fue creado en setUp()

        // Act & Assert
        mockMvc.perform(get("/api/empleados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Juan"));
    }

    @Test
    void getEmpleadoById_cuandoIdExiste_deberiaRetornar200OkConEmpleado() throws Exception {
        // Arrange
        Long idExistente = empleadoDePrueba.getLegajo();

        // Act & Assert
        mockMvc.perform(get("/api/empleados/{id}", idExistente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legajo").value(idExistente))
                .andExpect(jsonPath("$.apellido").value("Perez"));
    }

    @Test
    void getEmpleadoById_cuandoIdNoExiste_deberiaRetornar404NotFound() throws Exception {
        // Arrange
        Long idInexistente = -99L;

        // Act & Assert
        mockMvc.perform(get("/api/empleados/{id}", idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Empleado no encontrado con legajo: " + idInexistente));
    }

    @Test
    void updateEmpleado_cuandoIdExiste_deberiaRetornar200OkConEmpleadoActualizado() throws Exception {
        // Arrange
        Long idExistente = empleadoDePrueba.getLegajo(); // Obtiene el ID del empleado "Juan Perez"

        // Preparas los NUEVOS datos para la actualización
        EmpleadoDto datosParaActualizar = new EmpleadoDto();
        datosParaActualizar.setNombre("Maria");
        datosParaActualizar.setApellido("Gomez");
        datosParaActualizar.setTelefonoContacto(99887766);

        // Act & Assert
        mockMvc.perform(put("/api/empleados/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosParaActualizar)))
                .andExpect(status().isOk())
                // ¡CORRECCIÓN! Ahora verificamos que la respuesta contenga los datos enviados.
                .andExpect(jsonPath("$.nombre").value("Maria"))
                .andExpect(jsonPath("$.apellido").value("Gomez"))
                .andExpect(jsonPath("$.telefonoContacto").value(99887766));
    }

    @Test
    void updateEmpleado_cuandoIdNoExiste_deberiaRetornar404NotFound() throws Exception {
        // Arrange
        Long idInexistente = -99L;
        EmpleadoDto datosParaActualizar = new EmpleadoDto(); // Se usa el constructor vacío
        datosParaActualizar.setNombre("Maria");
        datosParaActualizar.setApellido("Gomez");
        datosParaActualizar.setTelefonoContacto(99887766);
        // Act & Assert
        mockMvc.perform(put("/api/empleados/{id}", idInexistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosParaActualizar)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmpleado_cuandoIdExiste_deberiaRetornar204NoContent() throws Exception {
        // Arrange
        Long idExistente = empleadoDePrueba.getLegajo();

        // Act
        mockMvc.perform(delete("/api/empleados/{id}", idExistente))
                .andExpect(status().isNoContent());

        // Assert
        // Verificamos que el empleado realmente fue borrado de la base de datos.
        assertThat(empleadoRepository.existsById(idExistente)).isFalse();
    }

    @Test
    void deleteEmpleado_cuandoIdNoExiste_deberiaRetornar404NotFound() throws Exception {
        // Arrange
        Long idInexistente = -99L;

        // Act & Assert
        mockMvc.perform(delete("/api/empleados/{id}", idInexistente))
                .andExpect(status().isNotFound());
    }
}