package com.tpi.notificaciones.service;

import com.tpi.notificaciones.dtos.NotificacionPromocionDto;
import com.tpi.notificaciones.dtos.PosicionDto;
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
import java.util.Collections;

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

    private NotificacionPromocionDto promocionDto;

    @BeforeEach
    void setUp() {
        promocionDto = new NotificacionPromocionDto();
        promocionDto.setCodigoPromocion("PROMO123");
        promocionDto.setMensaje("¡Gran promo!");
        promocionDto.setFechaExpiracion(LocalDate.of(2025, 12, 31));
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
}
