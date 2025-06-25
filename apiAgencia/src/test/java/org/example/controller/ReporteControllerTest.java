package org.example.controller;

import org.example.service.ReporteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(ReporteController.class)
class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReporteService reporteService;

    @Test
    void getReportePruebasConIncidentes_deberiaRetornarReporteDesdeServicio() throws Exception {
        String reporteSimulado = "Reporte de todos los incidentes.";
        when(reporteService.generarReporteIncidentes()).thenReturn(reporteSimulado);

        mockMvc.perform(get("/api/reportes/incidentes"))
                .andExpect(status().isOk())
                .andExpect(content().string(reporteSimulado));
    }

    @Test
    void getReporteIncidentesPorEmpleado_cuandoEmpleadoExiste_deberiaRetornarReporte() throws Exception {
        Long legajoExistente = 123L;
        String reporteSimulado = "Reporte para el empleado " + legajoExistente;
        when(reporteService.generarReporteIncidentesPorEmpleado(legajoExistente)).thenReturn(reporteSimulado);

        mockMvc.perform(get("/api/reportes/incidentes/empleado/{legajo}", legajoExistente))
                .andExpect(status().isOk())
                .andExpect(content().string(reporteSimulado));
    }

    @Test
    void getReporteIncidentesPorEmpleado_cuandoEmpleadoNoExiste_deberiaRetornarNotFound() throws Exception {
        Long legajoInexistente = 999L;
        String mensajeError = "Empleado no encontrado";
        when(reporteService.generarReporteIncidentesPorEmpleado(legajoInexistente)).thenReturn(mensajeError);

        mockMvc.perform(get("/api/reportes/incidentes/empleado/{legajo}", legajoInexistente))
                .andExpect(status().isNotFound())
                .andExpect(content().string(mensajeError));
    }

    @Test
    void getReporteDePruebasPorVehiculo_cuandoVehiculoExiste_deberiaRetornarReporte() throws Exception {
        String patenteExistente = "ABC123";
        String reporteSimulado = "Reporte para el vehiculo " + patenteExistente;
        when(reporteService.generarReportePruebasPorVehiculo(anyString())).thenReturn(reporteSimulado);

        mockMvc.perform(get("/api/reportes/vehiculo/{patente}", patenteExistente))
                .andExpect(status().isOk())
                .andExpect(content().string(reporteSimulado));
    }

    @Test
    void getReporteDePruebasPorVehiculo_cuandoVehiculoNoExiste_deberiaRetornarNotFound() throws Exception {
        String patenteInexistente = "XYZ987";
        String mensajeError = "Vehículo no encontrado";
        when(reporteService.generarReportePruebasPorVehiculo(patenteInexistente)).thenReturn(mensajeError);

        mockMvc.perform(get("/api/reportes/vehiculo/{patente}", patenteInexistente))
                .andExpect(status().isNotFound())
                .andExpect(content().string(mensajeError));
    }

    @Test
    void getReporteKilometraje_deberiaRetornarReporteDesdeServicio() throws Exception {
        String patente = "AA111AA";
        String fechaInicio = "2025-10-15";
        String fechaFin = "2025-10-15";
        String reporteSimulado = "Reporte de KM para " + patente;

        when(reporteService.generarReporteKilometraje(anyString(), any(Date.class), any(Date.class)))
                .thenReturn(reporteSimulado);

        mockMvc.perform(get("/api/reportes/kilometraje/{patente}", patente)
                        .param("fechaInicio", fechaInicio)
                        .param("fechaFin", fechaFin))
                .andExpect(status().isOk())
                .andExpect(content().string(reporteSimulado));
    }
} 