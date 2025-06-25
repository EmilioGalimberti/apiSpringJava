package org.example.service;

import org.example.dtos.externos.RestriccionesDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

// Importaciones estáticas para aserciones de JUnit 5 y para los matchers de Mockito
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para ExternalApisService.
 * Este test sigue un estilo de bloqueo para verificar el resultado del Mono.
 */
@ExtendWith(MockitoExtension.class)
class ExternalApiServiceTest {

    @Mock
    private WebClient webClient;

    // Mocks para simular la cadena de llamadas de WebClient
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private ExternalApisService externalApiService;

    @BeforeEach
    void setUp() {
        // Inyectamos la URL de prueba
        ReflectionTestUtils.setField(externalApiService, "urlRestricciones", "http://test-url.com/restricciones");

        // Configuración de la cadena de mocks de WebClient
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    /**
     * Prueba el caso de éxito.
     * Verificamos que al bloquear el Mono, obtenemos el objeto esperado.
     */
    @Test
    void getRestricciones_cuandoLlamadaEsExitosa_deberiaRetornarRestriccionesDto() {
        // --- 1. Arrange (Preparar) ---
        RestriccionesDto restriccionesMock = new RestriccionesDto();
        // (Opcional: puedes añadir valores al mock si es necesario)
        // restriccionesMock.setAlgunaPropiedad("valor");

        // Configuramos el mock para que devuelva el DTO dentro de un Mono
        when(responseSpec.bodyToMono(RestriccionesDto.class)).thenReturn(Mono.just(restriccionesMock));

        // --- 2. Act (Actuar) ---
        // Llamamos al método y usamos .block() para esperar y obtener el resultado síncronamente.
        RestriccionesDto resultado = externalApiService.getRestricciones().block();

        // --- 3. Assert (Verificar) ---
        // Usamos aserciones estándar de JUnit 5.
        assertNotNull(resultado, "El resultado no debería ser nulo.");
        assertEquals(restriccionesMock, resultado, "El objeto devuelto no es el esperado.");
    }

    /**
     * Prueba el caso de error.
     * Verificamos que al intentar bloquear un Mono que contiene un error, se lanza una excepción.
     */
    @Test
    void getRestricciones_cuandoLlamadaFalla_deberiaLanzarExcepcion() {
        // --- 1. Arrange (Preparar) ---
        RuntimeException errorSimulado = new RuntimeException("Error 500");

        // Configuramos el mock para que devuelva un Mono que emite un error
        when(responseSpec.bodyToMono(RestriccionesDto.class)).thenReturn(Mono.error(errorSimulado));

        // --- 2. Act & 3. Assert (Actuar y Verificar) ---
        // Usamos assertThrows para verificar que la ejecución de la lambda
        // (que contiene la llamada con .block()) lanza la excepción que esperamos.
        RuntimeException excepcionLanzada = assertThrows(RuntimeException.class, () -> {
            externalApiService.getRestricciones().block();
        });

        // Opcional: podemos verificar que el mensaje de la excepción es el correcto.
        assertEquals("Error 500", excepcionLanzada.getMessage());
    }
}