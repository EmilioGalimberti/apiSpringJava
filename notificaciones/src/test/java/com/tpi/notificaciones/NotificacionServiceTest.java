package com.tpi.notificaciones.service;

import com.tpi.notificaciones.dtos.*;
import com.tpi.notificaciones.models.*;
import com.tpi.notificaciones.repositories.NotificacionPromocionRepository;
import com.tpi.notificaciones.repositories.NotificacionRadioExcedidoRepository;
import com.tpi.notificaciones.repositories.NotificacionZonaPeligrosaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificacionServiceTest {

    @Mock
    private NotificacionPromocionRepository promocionRepository;

    @Mock
    private NotificacionRadioExcedidoRepository radioExcedidoRepository;

    @Mock
    private NotificacionZonaPeligrosaRepository zonaPeligrosaRepository;

    @Mock
    private TwilioSmsService smsService;

    @InjectMocks
    private NotificacionService notificacionService;

    // --- Datos de prueba ---
    private NotificacionPromocionDto promocionDto;
    private PosicionDto posicionDto;

    @BeforeEach
    void setUp() {
        // Datos para tests de promociones
        promocionDto = new NotificacionPromocionDto();
        promocionDto.setCodigoPromocion("PROMO123");
        promocionDto.setMensaje("¡Gran promo!");
        promocionDto.setFechaExpiracion(LocalDate.of(2025, 12, 31));

        // Datos para tests de alertas de posición
        VehiculoDto vehiculo = new VehiculoDto();
        vehiculo.setId(1);
        vehiculo.setPatente("AB123CD");

        // SOLUCIÓN: Instanciamos la clase anidada `Coordenadas` que está DENTRO de `PosicionDto`.
        // El nombre correcto de la clase es `Coordenadas`, sin el sufijo "Dto".
        PosicionDto.Coordenadas coordenadas = new PosicionDto.Coordenadas(-31.417, -64.183);

        posicionDto = new PosicionDto();
        posicionDto.setVehiculo(vehiculo);
        posicionDto.setCoordenadas(coordenadas); // Ahora el tipo de objeto coincide con lo que espera el método.
        posicionDto.setMensaje("Mensaje de prueba de posición.");
    }

    @Test
    void testCreatePromocion() {
        ArgumentCaptor<NotificacionPromocionEntity> captor = ArgumentCaptor.forClass(NotificacionPromocionEntity.class);
        NotificacionPromocionEntity saved = new NotificacionPromocionEntity();
        saved.setCodigoPromocion("PROMO123");
        saved.setMensaje("¡Gran promo!");
        saved.setFechaExpiracion(LocalDate.of(2025, 12, 31));
        when(promocionRepository.save(any())).thenReturn(saved);
        NotificacionPromocionEntity result = notificacionService.createPromocion(promocionDto);
        verify(smsService).sendSmsToMultipleRecipients(contains("PROMOCION"));
        verify(promocionRepository).save(captor.capture());
        assertEquals("PROMO123", result.getCodigoPromocion());
        assertEquals("¡Gran promo!", result.getMensaje());
        assertNotNull(captor.getValue().getFechaNotificacion());
    }

    @Test
    void testGetAllPromocionesReturnsDtoList() {
        NotificacionPromocionEntity entity = new NotificacionPromocionEntity();
        entity.setCodigoPromocion("PROMO123");
        entity.setMensaje("Mensaje promo");
        entity.setFechaExpiracion(LocalDate.of(2025, 12, 31));
        when(promocionRepository.findAll()).thenReturn(Collections.singletonList(entity));
        var result = notificacionService.getAllPromociones();
        assertEquals(1, result.spliterator().getExactSizeIfKnown());
        assertEquals("PROMO123", result.iterator().next().getCodigoPromocion());
    }

    @Test
    void createRadioExcedido_DeberiaGuardarEntidadYEnviarSms() {
        ArgumentCaptor<NotificacionRadioExcedidoEntity> captorEntidad = ArgumentCaptor.forClass(NotificacionRadioExcedidoEntity.class);
        ArgumentCaptor<String> captorSms = ArgumentCaptor.forClass(String.class);

        notificacionService.createRadioExcedido(posicionDto);

        verify(radioExcedidoRepository).save(captorEntidad.capture());
        NotificacionRadioExcedidoEntity entidadGuardada = captorEntidad.getValue();

        assertNotNull(entidadGuardada.getFechaNotificacion());
        assertEquals(posicionDto.getCoordenadas().getLat(), entidadGuardada.getLatActual());
        assertEquals(posicionDto.getCoordenadas().getLon(), entidadGuardada.getLonActual());
        assertEquals(posicionDto.getVehiculo().getId(), entidadGuardada.getIdVehiculo());
        assertEquals(posicionDto.getMensaje(), entidadGuardada.getMensaje());

        verify(smsService).sendSmsToMultipleRecipients(captorSms.capture());
        String mensajeSms = captorSms.getValue();
        assertTrue(mensajeSms.contains("ALERTA"));
        assertTrue(mensajeSms.contains("excedio el radio maximo"));
        assertTrue(mensajeSms.contains(posicionDto.getVehiculo().getPatente().toUpperCase()));
    }

    @Test
    void createZonaPeligrosa_DeberiaGuardarEntidadYEnviarSms() {
        ArgumentCaptor<NotificacionZonaPeligrosaEntity> captorEntidad = ArgumentCaptor.forClass(NotificacionZonaPeligrosaEntity.class);
        ArgumentCaptor<String> captorSms = ArgumentCaptor.forClass(String.class);

        notificacionService.createZonaPeligrosa(posicionDto);

        verify(zonaPeligrosaRepository).save(captorEntidad.capture());
        NotificacionZonaPeligrosaEntity entidadGuardada = captorEntidad.getValue();

        assertNotNull(entidadGuardada.getFechaNotificacion());
        assertEquals("ALTO", entidadGuardada.getNivelPeligro());
        assertEquals(posicionDto.getCoordenadas().getLat(), entidadGuardada.getLatActual());
        assertEquals(posicionDto.getCoordenadas().getLon(), entidadGuardada.getLonActual());

        verify(smsService).sendSmsToMultipleRecipients(captorSms.capture());
        String mensajeSms = captorSms.getValue();
        assertTrue(mensajeSms.contains("ALERTA"));
        assertTrue(mensajeSms.contains("ingreso a una zona peligrosa"));
        assertTrue(mensajeSms.contains(posicionDto.getVehiculo().getPatente().toUpperCase()));
    }

    @Test
    void getAllRadiosExcedidos_DeberiaRetornarListaDeDtos() {
        NotificacionRadioExcedidoEntity entity = new NotificacionRadioExcedidoEntity();
        entity.setId(1);
        entity.setFechaNotificacion(LocalDateTime.now());
        entity.setMensaje("Test radio");

        when(radioExcedidoRepository.findAll()).thenReturn(List.of(entity));

        Iterable<NotificacionRadioExcedidoDto> resultado = notificacionService.getAllRadiosExcedidos();
        List<NotificacionRadioExcedidoDto> listaResultado = (List<NotificacionRadioExcedidoDto>) resultado;

        assertNotNull(listaResultado);
        assertEquals(1, listaResultado.size());
        assertEquals("Test radio", listaResultado.get(0).getMensaje());
        assertEquals(1, listaResultado.get(0).getId());
    }

    @Test
    void getAllZonasPeligrosas_DeberiaRetornarListaDeDtos() {
        NotificacionZonaPeligrosaEntity entity = new NotificacionZonaPeligrosaEntity();
        entity.setId(1);
        entity.setMensaje("Test zona");
        entity.setNivelPeligro("ALTO");

        when(zonaPeligrosaRepository.findAll()).thenReturn(List.of(entity));

        Iterable<NotificacionZonaPeligrosaDto> resultado = notificacionService.getAllZonasPeligrosas();
        List<NotificacionZonaPeligrosaDto> listaResultado = (List<NotificacionZonaPeligrosaDto>) resultado;

        assertNotNull(listaResultado);
        assertEquals(1, listaResultado.size());
        assertEquals("Test zona", listaResultado.get(0).getMensaje());
        assertEquals("ALTO", listaResultado.get(0).getNivelPeligro());
    }
}
