package org.example.service;

import org.example.dtos.EmpleadoDto;
import org.example.dtos.PruebaDto;
import org.example.models.Empleado;
import org.example.models.Posicion;
import org.example.models.Prueba;
import org.example.models.Vehiculo;
import org.example.repositories.EmpleadoRepository;
import org.example.repositories.PosicionRepository;
import org.example.repositories.PruebaRepository;
import org.example.repositories.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private PruebaService pruebaService;

    @Mock
    private EmpleadoRepository empleadoRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private PruebaRepository pruebaRepository;

    @Mock
    private PosicionRepository posicionRepository;

    @InjectMocks
    private ReporteService reporteService;
    private Empleado empleadoDePrueba;
    private PruebaDto pruebaConIncidente;
    private Vehiculo vehiculoDePruebaKm;
    private Prueba pruebaDePruebaKm;
    private List<Posicion> posicionesDePrueba;

    @BeforeEach
    void setUp() {
        empleadoDePrueba = new Empleado("Juan", "Perez", 12345);
        empleadoDePrueba.setLegajo(1L);

        pruebaConIncidente = new PruebaDto();
        pruebaConIncidente.setId(100);
        pruebaConIncidente.setIncidente(true);
        pruebaConIncidente.setEmpleado(new EmpleadoDto(1L, "Juan", "Perez", 12345));

        // --- NUEVOS DATOS PARA TEST DE KILOMETRAJE ---
        vehiculoDePruebaKm = new Vehiculo();
        vehiculoDePruebaKm.setId(10);
        vehiculoDePruebaKm.setPatente("AA111AA");

        pruebaDePruebaKm = new Prueba();
        pruebaDePruebaKm.setId(100);
        pruebaDePruebaKm.setVehiculo(vehiculoDePruebaKm);
        pruebaDePruebaKm.setFechaHoraInicio(new Date(1760564400000L)); // 15/10/2025 11:00
        pruebaDePruebaKm.setFechaHoraFin(new Date(1760566200000L));   // 15/10/2025 11:30

        Posicion p1 = new Posicion(1, vehiculoDePruebaKm, 1760564700000L, -31.417, -64.183);
        Posicion p2 = new Posicion(2, vehiculoDePruebaKm, 1760565000000L, -31.420, -64.188);
        Posicion p3 = new Posicion(3, vehiculoDePruebaKm, 1760565300000L, -31.415, -64.195);
        posicionesDePrueba = List.of(p1, p2, p3);
    }

    @Test
    void generarReporteIncidentes_cuandoNoHayIncidentes_deberiaRetornarMensaje() {
        // Arrange
        when(pruebaService.getPruebasConIncidentes()).thenReturn(Collections.emptyList());

        // Act
        String reporte = reporteService.generarReporteIncidentes();

        // Assert
        assertEquals("No hay pruebas con incidentes registradas.", reporte);
    }

    @Test
    void generarReporteIncidentes_cuandoHayIncidentes_deberiaRetornarTabla() {
        // Arrange
        when(pruebaService.getPruebasConIncidentes()).thenReturn(List.of(pruebaConIncidente));

        // Act
        String reporte = reporteService.generarReporteIncidentes();

        // Assert
        assertTrue(reporte.contains("Reporte de Pruebas con Incidentes"));
        assertTrue(reporte.contains("ID"));
        assertTrue(reporte.contains("Patente"));
        assertTrue(reporte.contains("Juan Perez")); // Nombre del empleado
        assertTrue(reporte.contains("100")); // ID de la prueba
    }

    @Test
    void generarReporteIncidentesPorEmpleado_cuandoEmpleadoNoExiste_deberiaRetornarMensajeError() {
        // Arrange
        Long legajoInexistente = 999L;
        when(empleadoRepository.findById(legajoInexistente)).thenReturn(Optional.empty());

        // Act
        String reporte = reporteService.generarReporteIncidentesPorEmpleado(legajoInexistente);

        // Assert
        assertEquals("Empleado con legajo 999 no encontrado.", reporte);
    }

    @Test
    void generarReporteIncidentesPorEmpleado_cuandoEmpleadoNoTieneIncidentes_deberiaRetornarMensaje() {
        // Arrange
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleadoDePrueba));
        when(pruebaService.getIncidentesPorEmpleado(1L)).thenReturn(Collections.emptyList());

        // Act
        String reporte = reporteService.generarReporteIncidentesPorEmpleado(1L);

        // Assert
        assertTrue(reporte.contains("Reporte de Incidentes para el Empleado"));
        assertTrue(reporte.contains("Legajo: 1"));
        assertTrue(reporte.contains("Nombre: Juan Perez"));
        assertTrue(reporte.contains("El empleado no tiene incidentes registrados."));
    }

    @Test
    void generarReporteIncidentesPorEmpleado_cuandoEmpleadoTieneIncidentes_deberiaRetornarReporteCompleto() {
        // Arrange
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleadoDePrueba));
        when(pruebaService.getIncidentesPorEmpleado(1L)).thenReturn(List.of(pruebaConIncidente));

        // Act
        String reporte = reporteService.generarReporteIncidentesPorEmpleado(1L);

        // Assert
        assertTrue(reporte.contains("Reporte de Incidentes para el Empleado"));
        assertTrue(reporte.contains("Legajo: 1"));
        assertTrue(reporte.contains("ID"));
        assertTrue(reporte.contains("100")); // ID de la prueba
        assertFalse(reporte.contains("El empleado no tiene incidentes registrados."));
    }

    @Test
    void generarReporteKilometraje_cuandoVehiculoNoExiste_deberiaRetornarMensajeError() {
        // Arrange
        when(vehiculoRepository.findByPatente("PATENTE_INEXISTENTE")).thenReturn(Optional.empty());

        // Act
        String reporte = reporteService.generarReporteKilometraje("PATENTE_INEXISTENTE", new Date(), new Date());

        // Assert
        assertEquals("Vehículo con patente PATENTE_INEXISTENTE no encontrado.", reporte);
    }

    @Test
    void generarReporteKilometraje_cuandoNoHayPruebasFinalizadas_deberiaRetornarMensaje() {
        // Arrange
        when(vehiculoRepository.findByPatente(anyString())).thenReturn(Optional.of(vehiculoDePruebaKm));
        when(pruebaRepository.findAllByVehiculo_Id(anyInt())).thenReturn(Collections.emptyList());

        // Act
        String reporte = reporteService.generarReporteKilometraje("AA111AA", new Date(), new Date());

        // Assert
        assertEquals("El vehículo no tiene pruebas finalizadas en el período especificado.", reporte);
    }

    @Test
    void generarReporteKilometraje_cuandoHayPruebasYPosiciones_deberiaCalcularDistanciaCorrectamente() {
        // Arrange
        when(vehiculoRepository.findByPatente("AA111AA")).thenReturn(Optional.of(vehiculoDePruebaKm));
        when(pruebaRepository.findAllByVehiculo_Id(10)).thenReturn(List.of(pruebaDePruebaKm));
        when(posicionRepository.findAllByVehiculo_IdAndFechaHoraBetweenOrderByFechaHoraAsc(
                10, pruebaDePruebaKm.getFechaHoraInicio().getTime(), pruebaDePruebaKm.getFechaHoraFin().getTime()))
                .thenReturn(posicionesDePrueba);

        // Act
        String reporte = reporteService.generarReporteKilometraje("AA111AA", new Date(1760564300000L), new Date(1760566300000L));

        // Assert
        assertTrue(reporte.contains("Reporte de Kilometraje para el Vehículo: AA111AA"));

        assertTrue(reporte.contains("Kilometraje total recorrido: 1,45 km"));
    }
}