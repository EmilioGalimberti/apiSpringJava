package com.tpi.notificaciones.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpi.notificaciones.dtos.NotificacionPromocionDto;
import com.tpi.notificaciones.dtos.NotificacionRadioExcedidoDto;
import com.tpi.notificaciones.dtos.NotificacionZonaPeligrosaDto;
import com.tpi.notificaciones.models.NotificacionPromocionEntity;
import com.tpi.notificaciones.models.NotificacionRadioExcedidoEntity;
import com.tpi.notificaciones.models.NotificacionZonaPeligrosaEntity;
import com.tpi.notificaciones.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDate;

@WebMvcTest(NotificacionController.class)
public class NotificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificacionService notificacionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testNotificarPromocion() throws Exception {
        NotificacionPromocionEntity entity = new NotificacionPromocionEntity();
        entity.setCodigoPromocion("PROMO123");
        entity.setFechaExpiracion(LocalDate.of(2025, 12, 31));
        entity.setMensaje("Descuento de fin de año"); // Si el entity tiene este campo

        when(notificacionService.createPromocion(Mockito.any())).thenReturn(entity);

        mockMvc.perform(post("/api/notificaciones/promocion/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoPromocion").value("PROMO123"))
                .andExpect(jsonPath("$.mensaje").value("Descuento de fin de año"));
    }

    @Test
    void testGetAllPromociones() throws Exception {
        NotificacionPromocionDto dto = new NotificacionPromocionDto();
        dto.setCodigoPromocion("Promo!");
        dto.setFechaExpiracion(LocalDate.of(2025, 12, 31));
        dto.setMensaje("Descuento de fin de año"); // Si el entity tiene este campo

        when(notificacionService.getAllPromociones())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/notificaciones/promocion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoPromocion").value("Promo!"))
                .andExpect(jsonPath("$[0].mensaje").value("Descuento de fin de año"));
    }

    @Test
    void testGetAllRadiosExcedidos() throws Exception {
        NotificacionRadioExcedidoEntity entity = new NotificacionRadioExcedidoEntity();
        //entity.setId(1L);
        entity.setMensaje("Radio excedido detectado");
        entity.setLatActual(-34.6037);
        entity.setLonActual(-58.3816);
        entity.setIdVehiculo(123);

        NotificacionRadioExcedidoDto dto = new NotificacionRadioExcedidoDto(entity);

        when(notificacionService.getAllRadiosExcedidos())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/notificaciones/seguridad/radio-excedido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mensaje").value("Radio excedido detectado"))
                .andExpect(jsonPath("$[0].latActual").value(-34.6037))
                .andExpect(jsonPath("$[0].lonActual").value(-58.3816))
                .andExpect(jsonPath("$[0].idVehiculo").value(123));
    }

    @Test
    void testGetAllZonasPeligrosas() throws Exception {
        NotificacionZonaPeligrosaEntity entity = new NotificacionZonaPeligrosaEntity();
        //entity.setId(1L);
        entity.setMensaje("Radio excedido detectado");
        entity.setLatActual(-34.6037);
        entity.setLonActual(-58.3816);
        entity.setIdVehiculo(123);

        NotificacionZonaPeligrosaDto dto = new NotificacionZonaPeligrosaDto(entity);
        when(notificacionService.getAllZonasPeligrosas())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/notificaciones/seguridad/zona-peligrosa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mensaje").value("Radio excedido detectado"))
                .andExpect(jsonPath("$[0].latActual").value(-34.6037))
                .andExpect(jsonPath("$[0].lonActual").value(-58.3816))
                .andExpect(jsonPath("$[0].idVehiculo").value(123));
    }
}
