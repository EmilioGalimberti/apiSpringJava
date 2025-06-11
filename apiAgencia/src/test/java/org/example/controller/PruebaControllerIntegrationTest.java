package org.example.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Main;
import org.example.dtos.EmpleadoDto;
import org.example.dtos.InteresadoDto;
import org.example.dtos.PruebaDto;
import org.example.dtos.VehiculoDto;
import org.example.models.*;
import org.example.repositories.*;
import org.example.service.PruebaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Clase de Pruebas de Integración para PruebaController.
 * @SpringBootTest carga el contexto completo de la aplicación Spring.
 * @AutoConfigureMockMvc configura MockMvc para simular peticiones HTTP.
 * @Transactional asegura que la base de datos se revierta a su estado original después de cada test.
 */
@SpringBootTest(classes = Main.class) // Especifica la clase principal para asegurar que se cargue la configuración correcta
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class PruebaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Para simular las peticiones HTTP

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos Java a JSON y viceversa

    // Inyecta los repositorios y servicios para crear datos de prueba y realizar acciones
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private EmpleadoRepository empleadoRepository;
    @Autowired
    private InteresadoRepository interesadoRepository;
    @Autowired
    private PruebaService pruebaService;
    @Autowired
    private MarcaRepository marcaRepository;
    @Autowired
    private ModeloRepository modeloRepository;
    @Autowired
    private PruebaRepository pruebaRepository;

    // Entidades de prueba que se reiniciarán antes de cada test
    private Vehiculo vehiculoDePrueba;
    private Empleado empleadoDePrueba;
    private Interesado interesadoDePrueba;

    /**
     * Este método se ejecuta antes de cada @Test.
     * Prepara un conjunto de datos base válidos (un vehículo, un empleado y un interesado)
     * para que los tests puedan usarlos.
     */
    @BeforeEach
    void setUp() {
        // 1. Crear y guardar la entidad padre: Marca
        Marca marca = new Marca();
        marca.setNombre("MarcaTest");
        marca = marcaRepository.save(marca);

        // 2. Crear y guardar la entidad dependiente: Modelo
        Modelo modelo = new Modelo();
        modelo.setDescripcion("ModeloTest");
        modelo.setMarca(marca);
        modelo = modeloRepository.save(modelo);

        // 3. Ahora sí, crear y guardar el Vehiculo con su Modelo asignado
        vehiculoDePrueba = new Vehiculo();
        vehiculoDePrueba.setPatente("TEST" + System.currentTimeMillis());
        vehiculoDePrueba.setModelo(modelo); // <-- ¡Línea clave!
        vehiculoDePrueba = vehiculoRepository.save(vehiculoDePrueba);

        // --- El resto del setup para Empleado e Interesado sigue igual ---
        empleadoDePrueba = new Empleado("Empleado", "Test", 123456789);
        empleadoDePrueba = empleadoRepository.save(empleadoDePrueba);

        Date fechaLicenciaVigente = new Date(System.currentTimeMillis() + 86400000L * 30);
        interesadoDePrueba = new Interesado("DNI", "VALIDO" + System.currentTimeMillis(), "Interesado", "Valido", false, 12345, fechaLicenciaVigente);
        interesadoDePrueba = interesadoRepository.save(interesadoDePrueba);
    }

    // =================================================================
    // TEST DE CREACIÓN EXITOSA
    // =================================================================
    @Test
    void crearNuevaPrueba_cuandoDatosSonValidos_deberiaRetornar201CreadoConPruebaDto() throws Exception {
        // Preparar el DTO de entrada con los datos válidos del setup
        PruebaDto pruebaDtoParaCrear = new PruebaDto(null,
                new VehiculoDto(vehiculoDePrueba.getId(), vehiculoDePrueba.getPatente(), null),
                new EmpleadoDto(empleadoDePrueba.getLegajo(), empleadoDePrueba.getNombre(), empleadoDePrueba.getApellido(), empleadoDePrueba.getTelefonoContacto()),
                new InteresadoDto(interesadoDePrueba.getId(), interesadoDePrueba.getTipoDocumento(), interesadoDePrueba.getDocumento(), interesadoDePrueba.getNombre(), interesadoDePrueba.getApellido(), interesadoDePrueba.getRestringido(), interesadoDePrueba.getNroLicencia(), interesadoDePrueba.getFechaVencimientoLicencia()),
                null, null, null);

        // Ejecutar la solicitud POST y verificar las expectativas
        mockMvc.perform(post("/api/pruebas/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pruebaDtoParaCrear)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.vehiculo.id").value(vehiculoDePrueba.getId()))
                .andExpect(jsonPath("$.empleado.legajo").value(empleadoDePrueba.getLegajo()))
                .andExpect(jsonPath("$.interesado.id").value(interesadoDePrueba.getId()));
    }

    // =================================================================
    // TEST PARA ERROR INESPERADO DEL SERVIDOR (500)
    // =================================================================

//    @Test
//    void crearNuevaPrueba_cuandoServicioFallaInesperadamente_deberiaRetornar500() throws Exception {
//        // 1. PREPARAR LA SIMULACIÓN (Arrange)
//        // Configuramos nuestro servicio simulado para que lance una RuntimeException
//        // (un error inesperado) cada vez que se llame a su méthod crearPrueba.
//        // Usamos 'any(PruebaDto.class)' para que coincida con cualquier objeto PruebaDto que se le pase.
//        when(pruebaService.crearPrueba(any(PruebaDto.class)))
//                .thenThrow(new RuntimeException("Error simulado de base de datos!"));
//
//        // Preparamos un DTO válido para enviar en el cuerpo de la solicitud.
//        // No importa qué datos tenga, porque el servicio simulado siempre lanzará el error.
//        PruebaDto pruebaDtoValido = new PruebaDto(null,
//                new VehiculoDto(vehiculoDePrueba.getId(), null, null),
//                new EmpleadoDto(empleadoDePrueba.getLegajo(), null, null, null),
//                new InteresadoDto(interesadoDePrueba.getId(), null, null, null, null, null, null, null),
//                null, null, null);
//
//        // 2. EJECUTAR Y VERIFICAR (Act & Assert)
//        mockMvc.perform(post("/api/pruebas/crear") // Asegúrate que la ruta sea correcta
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(pruebaDtoValido)))
//                .andExpect(status().isInternalServerError()) // Verificamos que el estado sea 500
//                .andExpect(content().string("Ocurrió un error inesperado al crear la prueba."));
//    }


    // =================================================================
    // TESTS PARA VALIDACIONES DE ERRORES
    // =================================================================

    @Test
    void crearNuevaPrueba_cuandoVehiculoNoExiste_deberiaRetornar400BadRequest() throws Exception {
        VehiculoDto vehiculoDtoInexistente = new VehiculoDto(-99, "NOEXISTE", 2L); // ID que no existe
        PruebaDto pruebaDtoParaCrear = new PruebaDto(null, vehiculoDtoInexistente, new EmpleadoDto(empleadoDePrueba.getLegajo(), null, null, null), new InteresadoDto(interesadoDePrueba.getId(), null, null, null, null, null, null, null), null, null, null);

        mockMvc.perform(post("/api/pruebas/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pruebaDtoParaCrear)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Vehículo no encontrado"));
    }

    @Test
    void crearNuevaPrueba_cuandoEmpleadoNoExiste_deberiaRetornar400BadRequest() throws Exception {
        EmpleadoDto empleadoDtoInexistente = new EmpleadoDto(-99L, "NOEXISTE", null, null); // Legajo que no existe
        PruebaDto pruebaDtoParaCrear = new PruebaDto(null, new VehiculoDto(vehiculoDePrueba.getId(), null, null), empleadoDtoInexistente, new InteresadoDto(interesadoDePrueba.getId(), null, null, null, null, null, null, null), null, null, null);

        mockMvc.perform(post("/api/pruebas/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pruebaDtoParaCrear)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Empleado no encontrado"));
    }

    @Test
    void crearNuevaPrueba_cuandoInteresadoTieneLicenciaVencida_deberiaRetornar400BadRequest() throws Exception {
        // Creamos un interesado específico para este test con licencia vencida
        Date fechaVencida = new Date(System.currentTimeMillis() - 86400000L); // Fecha de ayer
        Interesado interesadoVencido = new Interesado("DNI", "VENCIDO" + System.currentTimeMillis(), "Licencia", "Vencida", false, 67890, fechaVencida);
        interesadoVencido = interesadoRepository.save(interesadoVencido);
        PruebaDto pruebaDtoParaCrear = new PruebaDto(null, new VehiculoDto(vehiculoDePrueba.getId(), null, null), new EmpleadoDto(empleadoDePrueba.getLegajo(), null, null, null), new InteresadoDto(interesadoVencido.getId(), null, null, null, null, null, null, null), null, null, null);

        mockMvc.perform(post("/api/pruebas/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pruebaDtoParaCrear)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La licencia del interesado está vencida."));
    }

    @Test
    void crearNuevaPrueba_cuandoInteresadoEstaRestringido_deberiaRetornar400BadRequest() throws Exception {
        // El interesado del setup ya tiene 'restringido' en false, lo actualizamos para este test.
        interesadoDePrueba.setRestringido(true);
        interesadoRepository.save(interesadoDePrueba);
        PruebaDto pruebaDtoParaCrear = new PruebaDto(null, new VehiculoDto(vehiculoDePrueba.getId(), null, null), new EmpleadoDto(empleadoDePrueba.getLegajo(), null, null, null), new InteresadoDto(interesadoDePrueba.getId(), null, null, null, null, null, null, null), null, null, null);

        mockMvc.perform(post("/api/pruebas/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pruebaDtoParaCrear)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El interesado está restringido para probar vehículos."));
    }

    @Test
    void crearNuevaPrueba_cuandoVehiculoYaEstaEnPrueba_deberiaRetornar400BadRequest() throws Exception {
        // 1. Creamos una prueba válida para nuestro vehículo, simulando que ya está en curso.
        pruebaService.crearPrueba(new PruebaDto(null, new VehiculoDto(vehiculoDePrueba.getId(), null, null), new EmpleadoDto(empleadoDePrueba.getLegajo(), null, null, null), new InteresadoDto(interesadoDePrueba.getId(), null, null, null, null, null, null, null), null, null, null));

        // 2. Ahora, intentamos crear OTRA prueba con el MISMO vehículo.
        PruebaDto pruebaDuplicadaDto = new PruebaDto(null, new VehiculoDto(vehiculoDePrueba.getId(), null, null), new EmpleadoDto(empleadoDePrueba.getLegajo(), null, null, null), new InteresadoDto(interesadoDePrueba.getId(), null, null, null, null, null, null, null), null, null, null);

        mockMvc.perform(post("/api/pruebas/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pruebaDuplicadaDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El vehículo está siendo probado."));
    }

    // =================================================================
    // TEST COMPLEJO DE CREAR Y LUEGO BORRAR
    // =================================================================

    @Test
    void crearYLuegoBorrarPrueba_deberiaCompletarCicloExitosamente() throws Exception {
        // 1. PREPARAR DATOS PARA CREAR (usamos los datos válidos del setup)
        PruebaDto pruebaDtoParaCrear = new PruebaDto(null, new VehiculoDto(vehiculoDePrueba.getId(), null, null), new EmpleadoDto(empleadoDePrueba.getLegajo(), null, null, null), new InteresadoDto(interesadoDePrueba.getId(), null, null, null, null, null, null, null), null, null, null);

        // 2. EJECUTAR CREACIÓN Y OBTENER ID
        MvcResult resultCreacion = mockMvc.perform(post("/api/pruebas/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pruebaDtoParaCrear)))
                .andExpect(status().isCreated())
                .andReturn();

        // Extraer el ID de la prueba recién creada desde el cuerpo de la respuesta JSON
        String responseBody = resultCreacion.getResponse().getContentAsString();
        PruebaDto pruebaCreada = objectMapper.readValue(responseBody, PruebaDto.class);
        Integer idPruebaCreada = pruebaCreada.getId();

        // 3. EJECUTAR BORRADO
        mockMvc.perform(delete("/api/pruebas/delete/" + idPruebaCreada))
                .andExpect(status().isNoContent());

//        // 4. (OPCIONAL PERO RECOMENDADO) VERIFICAR QUE YA NO EXISTE
//        // Si intentamos borrarla de nuevo, debería darnos 404 Not Found
//        mockMvc.perform(delete("/api/pruebas/" + idPruebaCreada))
//                .andExpect(status().isNotFound());
    }

    // =================================================================
    // TEST PARA CASO DE BORRADO NO ENCONTRADO
    // =================================================================

    @Test
    void deletePrueba_cuandoIdNoExiste_deberiaRetornar404NotFound() throws Exception {
        // 1. PREPARAR DATOS
        // Definimos un ID que es muy improbable que exista en la base de datos.
        Integer idInexistente = -999;

        // 2. EJECUTAR Y VERIFICAR
        // Realizamos la petición DELETE a la URL con el ID inexistente
        // y esperamos que el estado de la respuesta sea 404 Not Found.
        mockMvc.perform(delete("/api/pruebas/delete/" + idInexistente))
                .andExpect(status().isNotFound());
    }

    // =================================================================
    // TESTS PARA GET /api/pruebas/en-curso
    // =================================================================

    /**
     * Prueba el caso donde no hay ninguna prueba en curso en la base de datos.
     * Debería devolver un estado 204 No Content.
     */
    @Test
    void getPruebasEnCurso_cuandoNoHayPruebas_deberiaRetornar204NoContent() throws Exception {
        // 1. PREPARAR DATOS (Arrange)
        // No se necesita preparación. Gracias a @Transactional, la base de datos está limpia
        // antes de este test, por lo que no hay pruebas en curso.

        // 2. EJECUTAR Y VERIFICAR (Act & Assert)
        mockMvc.perform(get("/api/pruebas/en-curso")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()); // Esperamos 204 No Content
    }

    /**
     * Prueba el caso donde sí existen pruebas en curso.
     * Debería devolver un estado 200 OK y una lista con las pruebas.
     */
    @Test
    void getPruebasEnCurso_cuandoExistenPruebas_deberiaRetornar200OkConListaDePruebas() throws Exception {
        // 1. PREPARAR DATOS (Arrange)
        // Creamos una prueba directamente en la base de datos que esté "en curso"
        // (es decir, con fechaHoraFin nula). Usamos las entidades creadas en el @BeforeEach.
        Prueba pruebaEnCurso = new Prueba();
        pruebaEnCurso.setVehiculo(vehiculoDePrueba);
        pruebaEnCurso.setEmpleado(empleadoDePrueba);
        pruebaEnCurso.setInteresado(interesadoDePrueba);
        pruebaEnCurso.setFechaHoraInicio(new Date());
        pruebaEnCurso.setFechaHoraFin(null); // <-- Clave: esto la marca como "en curso"
        pruebaRepository.save(pruebaEnCurso);

        // 2. EJECUTAR Y VERIFICAR (Act & Assert)
        mockMvc.perform(get("/api/pruebas/en-curso")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Esperamos 200 OK
                .andExpect(jsonPath("$").isArray()) // El cuerpo debe ser un array JSON
                .andExpect(jsonPath("$.length()").value(1)) // El array debe tener 1 elemento
                .andExpect(jsonPath("$[0].id").value(pruebaEnCurso.getId())) // Verificamos el ID de la prueba
                .andExpect(jsonPath("$[0].comentarios").isEmpty()) // Verificamos que los comentarios estén vacíos/nulos
                .andExpect(jsonPath("$[0].fechaHoraFin").isEmpty()); // Y que la fecha de fin sea nula
    }

    // =================================================================
    // TESTS PARA PATCH /api/pruebas/{id}/finalizar
    // =================================================================

    @Test
    void finalizarPrueba_cuandoPruebaExisteYEstaEnCurso_deberiaRetornar200OkConPruebaActualizada() throws Exception {
        // 1. Arrange: Creamos una prueba "en curso" en la BD.
        Prueba pruebaEnCurso = new Prueba(null, vehiculoDePrueba,interesadoDePrueba , empleadoDePrueba, new Date(), null, null);
        pruebaEnCurso = pruebaRepository.save(pruebaEnCurso);
        String comentario = "El cliente quedó muy satisfecho con el rendimiento.";

        // 2. Act & Assert: Realizamos la petición PATCH y verificamos la respuesta.
        mockMvc.perform(patch("/api/pruebas/{id}/finalizar", pruebaEnCurso.getId())
                        .param("comentario", comentario) // Añadimos el comentario como parámetro
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Esperamos 200 OK
                .andExpect(jsonPath("$.id").value(pruebaEnCurso.getId()))
                .andExpect(jsonPath("$.fechaHoraFin").isNotEmpty()) // Verificamos que la fecha de fin ya no es nula
                .andExpect(jsonPath("$.comentarios").value(comentario)); // Verificamos que el comentario se guardó
    }

    @Test
    void finalizarPrueba_cuandoPruebaNoExiste_deberiaRetornar404NotFound() throws Exception {
        // 1. Arrange: Un ID inexistente y un comentario.
        Integer idInexistente = -999;
        String comentario = "Este test no debería funcionar.";

        // 2. Act & Assert: Realizamos la petición PATCH y esperamos un 404.
        mockMvc.perform(patch("/api/pruebas/{id}/finalizar", idInexistente)
                        .param("comentario", comentario)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Esperamos 404 Not Found
                .andExpect(content().string("Prueba no encontrada"));
    }

    @Test
    void finalizarPrueba_cuandoPruebaYaEstaFinalizada_deberiaRetornar400BadRequest() throws Exception {
        // 1. Arrange: Creamos una prueba que YA tiene una fecha de fin.
        Prueba pruebaYaFinalizada = new Prueba(null, vehiculoDePrueba, interesadoDePrueba , empleadoDePrueba, new Date(), new Date(), "Prueba finalizada previamente.");
        pruebaYaFinalizada = pruebaRepository.save(pruebaYaFinalizada);
        String nuevoComentario = "Intentando finalizar de nuevo.";

        // 2. Act & Assert: Realizamos la petición PATCH y esperamos un 400.
        mockMvc.perform(patch("/api/pruebas/{id}/finalizar", pruebaYaFinalizada.getId())
                        .param("comentario", nuevoComentario)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()) // Esperamos 400 Bad Request
                .andExpect(content().string("La prueba ya ha sido finalizada."));
    }
}