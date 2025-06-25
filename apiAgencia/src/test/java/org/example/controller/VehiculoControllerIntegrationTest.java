package org.example.controller;

import org.example.Main;
import org.example.dtos.PosicionDto;
import org.example.dtos.VehiculoDto;
import org.example.dtos.externos.CoordenadasDto;
import org.example.dtos.externos.RestriccionesDto;
import org.example.dtos.externos.UbicacionDto;
import org.example.dtos.externos.ZonaPeligrosaDto;
import org.example.models.*;
import org.example.repositories.*;
import org.example.service.ExternalApisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(DataSetupService.class)
class VehiculoControllerIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private PosicionRepository posicionRepository;
    @Autowired
    private DataSetupService dataSetupService; // Inyectamos el servicio de ayuda

    // ¡CAMBIO CLAVE! En lugar de MockWebServer, ahora mockeamos el servicio directamente.
    // Esto nos da control total sobre lo que devuelve, sin simular una llamada HTTP.
    @MockBean
    private ExternalApisService externalApiService;

    @Test
    void procesarPosicion_conDatosValidos_deberiaGuardarPosicionYRetornar200Ok() {
        // --- 1. Arrange (Preparar) ---

        // a) Preparamos los datos en la BD H2 usando nuestro servicio de ayuda.
        Vehiculo vehiculoDePrueba = dataSetupService.setupDatabase();

        // b) Preparamos el mock del ExternalApiService.
        RestriccionesDto restriccionesMock = new RestriccionesDto();
        restriccionesMock.setRadioMaximoMetros(1000000.0); // 1000 km de radio
        UbicacionDto ubicacion = new UbicacionDto();
        ubicacion.setLatitud(0.0);
        ubicacion.setLongitud(0.0);
        restriccionesMock.setUbicacionAgencia(ubicacion);
        restriccionesMock.setZonasPeligrosas(List.of()); // Sin zonas peligrosas para el caso de éxito

        // c) ¡LA NUEVA FORMA DE SIMULAR!
        // Le decimos al mock que cuando se llame a `getRestricciones`, devuelva nuestro objeto.
        // Es más directo y fiable que simular una respuesta HTTP.
        when(externalApiService.getRestricciones()).thenReturn(Mono.just(restriccionesMock));

        // d) Preparamos la petición de entrada.
        PosicionDto posicionEntrada = crearPosicionDto(vehiculoDePrueba.getId(), 0.001, 0.001);

        // --- 2. Act (Actuar) & 3. Assert (Verificar) ---
        webTestClient.post().uri("/api/vehiculos/posicion/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(posicionEntrada)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.mensaje").isEqualTo("La posicion actual del vehiculo fue registrada.");

        // Verificamos que se guardó en la BD.
        assertThat(posicionRepository.count()).isEqualTo(1);
    }

    @Test
    void procesarPosicion_cuandoEstaFueraDeRadio_deberiaRetornarMensajeDeFueraDeRadio() throws Exception {
        // a) Preparamos los datos en la BD H2 usando nuestro servicio de ayuda.
        Vehiculo vehiculoDePrueba = dataSetupService.setupDatabase();

        // b) Preparamos el mock del ExternalApiService.
        RestriccionesDto restriccionesMock = new RestriccionesDto();
        restriccionesMock.setRadioMaximoMetros(1000000.0); // 1000 km de radio
        UbicacionDto ubicacion = new UbicacionDto();
        ubicacion.setLatitud(0.0);
        ubicacion.setLongitud(0.0);
        restriccionesMock.setUbicacionAgencia(ubicacion);
        restriccionesMock.setZonasPeligrosas(List.of()); // Sin zonas peligrosas para el caso de éxito

        // Le decimos al mock que cuando se llame a `getRestricciones`, devuelva nuestro objeto.
        // Es más directo y fiable que simular una respuesta HTTP.
        when(externalApiService.getRestricciones()).thenReturn(Mono.just(restriccionesMock));


        PosicionDto posicionEntrada = crearPosicionDto(vehiculoDePrueba.getId(), 10, 10); // Posición lejana

        // --- ACT & ASSERT ---
        webTestClient.post().uri("/api/vehiculos/posicion/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(posicionEntrada)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.mensaje").isEqualTo("La posicion actual del vehiculo se encuentra por fuera del radio permitido por la agencia.");
    }

    @Test
    void procesarPosicion_cuandoEstaEnZonaRestringida_deberiaRetornarMensajeDeZonaRestringida() throws Exception {
        // --- ARRANGE (PREPARAR) ---

        // 1. Preparamos los datos en la BD H2 usando nuestro servicio de ayuda.
        Vehiculo vehiculoDePrueba = dataSetupService.setupDatabase();

        // 2. Preparamos el mock del ExternalApiService con una zona restringida.
        RestriccionesDto restriccionesMock = new RestriccionesDto();
        restriccionesMock.setRadioMaximoMetros(2000000.0); // Radio grande para que no falle por eso

        UbicacionDto ubicacionAgencia = new UbicacionDto();
        ubicacionAgencia.setLatitud(0.0);
        ubicacionAgencia.setLongitud(0.0);
        restriccionesMock.setUbicacionAgencia(ubicacionAgencia);

        // Creamos la zona peligrosa que rodeará al vehículo
        ZonaPeligrosaDto zonaDePrueba = new ZonaPeligrosaDto();
        CoordenadasDto coordenadasZona = new CoordenadasDto();
        // Ponemos el centro de la zona peligrosa en (0.1, 0.1), que está cerca de la agencia (0,0).
        coordenadasZona.setLatitud(0.1);
        coordenadasZona.setLongitud(0.1);
        zonaDePrueba.setCoordenadas(coordenadasZona);
        zonaDePrueba.setRadioMetros(100000); // Un radio de 100000 metros
        restriccionesMock.setZonasPeligrosas(List.of(zonaDePrueba));

        // 3. ¡EL CAMBIO CLAVE! En lugar de MockWebServer, le decimos al mock del servicio qué devolver.
        when(externalApiService.getRestricciones()).thenReturn(Mono.just(restriccionesMock));

        // 4. Preparamos la petición con una posición que está EXACTAMENTE en el centro de la zona restringida.
        PosicionDto posicionEntrada = crearPosicionDto(vehiculoDePrueba.getId(), 0.1, 0.1);

        // --- ACT (ACTUAR) & ASSERT (VERIFICAR) ---
        webTestClient.post().uri("/api/vehiculos/posicion/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(posicionEntrada)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.mensaje").isEqualTo("La posicion actual del vehiculo se encuentra dentro de un area restringida.");
    }


    private PosicionDto crearPosicionDto(Integer vehiculoId, double lat, double lon) {
        PosicionDto dto = new PosicionDto();
        PosicionDto.Coordenadas coords = new PosicionDto.Coordenadas();
        coords.setLat(lat);
        coords.setLon(lon);
        dto.setCoordenadas(coords);
        dto.setVehiculo(new VehiculoDto(vehiculoId, null, null));
        return dto;
    }
}

// --- Servicio de Ayuda para la Preparación de Datos ---
// Esta clase se encarga de crear los datos en una transacción separada y confirmada (commit).
@Service
class DataSetupService {
    @Autowired VehiculoRepository vehiculoRepository;
    @Autowired PruebaRepository pruebaRepository;
    @Autowired PosicionRepository posicionRepository;
    @Autowired EmpleadoRepository empleadoRepository;
    @Autowired InteresadoRepository interesadoRepository;
    @Autowired MarcaRepository marcaRepository;
    @Autowired ModeloRepository modeloRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Vehiculo setupDatabase() {
        //limpieza de repositorios
        posicionRepository.deleteAll();
        pruebaRepository.deleteAll();
        vehiculoRepository.deleteAll();
        modeloRepository.deleteAll();
        marcaRepository.deleteAll();
        empleadoRepository.deleteAll();
        interesadoRepository.deleteAll();

        Marca m = marcaRepository.save(new Marca(null, "TestBrand", null));
        Modelo mo = modeloRepository.save(new Modelo(null, m, "TestModel", null));

        // Hacemos la patente única añadiendo la hora actual en milisegundos
        String patenteUnica = "TEST" + System.currentTimeMillis();
        Vehiculo v = vehiculoRepository.save(new Vehiculo(null, patenteUnica, mo, null, null));

        Empleado e = empleadoRepository.save(new Empleado(null, "Juan", "Test", 123, null));

        // Hacemos lo mismo para el documento del interesado para evitar futuros problemas.
        String documentoUnico = "DOC" + System.currentTimeMillis();
        Interesado i = interesadoRepository.save(new Interesado(null, "DNI", documentoUnico, "Pepe", "Prueba", false, 54321, new Date(System.currentTimeMillis() + 10000000), null));

        pruebaRepository.save(new Prueba(null, v, i, e, new Date(), null, "Prueba en curso para el test", null));

        return v;
    }
}