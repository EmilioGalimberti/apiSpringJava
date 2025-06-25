package org.example.service;

import org.example.dtos.EmpleadoDto;
import org.example.dtos.PruebaDto;
import org.example.models.*;
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

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    // --- Mocks de Repositorios y Servicios ---
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

    // --- Clase bajo prueba ---
    @InjectMocks
    private ReporteService reporteService;

    // --- Datos de prueba ---
    private Empleado empleadoDePrueba;
    private PruebaDto pruebaConIncidenteDto;
    private Vehiculo vehiculoDePrueba;
    private Prueba pruebaDePrueba1;
    private Prueba pruebaDePrueba2;
    private List<Posicion> posicionesDePrueba;
    private Vehiculo vehiculoDePruebaKm;
    private Prueba pruebaDePruebaKm;


    @BeforeEach
    void setUp() {
        // --- Datos para Empleado y DTOs ---
        empleadoDePrueba = new Empleado("Juan", "Perez", 12345);
        empleadoDePrueba.setLegajo(1L);

        pruebaConIncidenteDto = new PruebaDto();
        pruebaConIncidenteDto.setId(100);
        pruebaConIncidenteDto.setIncidente(true);
        pruebaConIncidenteDto.setEmpleado(new EmpleadoDto(1L, "Juan", "Perez", 12345));

        // --- Datos para Vehículo y Pruebas asociadas ---
        Marca marca = new Marca();
        marca.setNombre("Ford");
        Modelo modelo = new Modelo();
        modelo.setDescripcion("Ranger");
        modelo.setMarca(marca);

        vehiculoDePrueba = new Vehiculo();
        vehiculoDePrueba.setId(10);
        vehiculoDePrueba.setPatente("AB123CD");
        vehiculoDePrueba.setModelo(modelo);

        pruebaDePrueba1 = new Prueba();
        pruebaDePrueba1.setId(1);
        pruebaDePrueba1.setVehiculo(vehiculoDePrueba);
        pruebaDePrueba1.setEmpleado(empleadoDePrueba);
        pruebaDePrueba1.setFechaHoraInicio(new Date(1678881600000L)); // 15/03/2023 12:00:00
        pruebaDePrueba1.setIncidente(true);

        pruebaDePrueba2 = new Prueba();
        pruebaDePrueba2.setId(2);
        pruebaDePrueba2.setVehiculo(vehiculoDePrueba);
        pruebaDePrueba2.setEmpleado(null); // Prueba sin empleado asignado
        pruebaDePrueba2.setFechaHoraInicio(new Date(1678968000000L)); // 16/03/2023 12:00:00
        pruebaDePrueba2.setIncidente(false);


        // --- Datos para pruebas de kilometraje ---
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
        when(pruebaService.getPruebasConIncidentes()).thenReturn(Collections.emptyList());
        String reporte = reporteService.generarReporteIncidentes();
        assertEquals("No hay pruebas con incidentes registradas.", reporte);
    }

    @Test
    void generarReporteIncidentes_cuandoHayIncidentes_deberiaRetornarTabla() {
        when(pruebaService.getPruebasConIncidentes()).thenReturn(List.of(pruebaConIncidenteDto));
        String reporte = reporteService.generarReporteIncidentes();
        assertTrue(reporte.contains("Reporte de Pruebas con Incidentes"));
        assertTrue(reporte.contains("Juan Perez"));
        assertTrue(reporte.contains("100"));
    }

    @Test
    void generarReporteIncidentesPorEmpleado_cuandoEmpleadoNoExiste_deberiaRetornarMensajeError() {
        Long legajoInexistente = 999L;
        when(empleadoRepository.findById(legajoInexistente)).thenReturn(Optional.empty());
        String reporte = reporteService.generarReporteIncidentesPorEmpleado(legajoInexistente);
        assertEquals("Empleado con legajo 999 no encontrado.", reporte);
    }

    @Test
    void generarReporteIncidentesPorEmpleado_cuandoEmpleadoNoTieneIncidentes_deberiaRetornarMensaje() {
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleadoDePrueba));
        when(pruebaService.getIncidentesPorEmpleado(1L)).thenReturn(Collections.emptyList());
        String reporte = reporteService.generarReporteIncidentesPorEmpleado(1L);
        assertTrue(reporte.contains("El empleado no tiene incidentes registrados."));
    }

    @Test
    void generarReporteIncidentesPorEmpleado_cuandoEmpleadoTieneIncidentes_deberiaRetornarReporteCompleto() {
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleadoDePrueba));
        when(pruebaService.getIncidentesPorEmpleado(1L)).thenReturn(List.of(pruebaConIncidenteDto));
        String reporte = reporteService.generarReporteIncidentesPorEmpleado(1L);
        assertTrue(reporte.contains("Reporte de Incidentes para el Empleado"));
        assertTrue(reporte.contains("100"));
    }

    @Test
    void generarReporteKilometraje_cuandoVehiculoNoExiste_deberiaRetornarMensajeError() {
        when(vehiculoRepository.findByPatente("PATENTE_INEXISTENTE")).thenReturn(Optional.empty());
        String reporte = reporteService.generarReporteKilometraje("PATENTE_INEXISTENTE", new Date(), new Date());
        assertEquals("Vehículo con patente PATENTE_INEXISTENTE no encontrado.", reporte);
    }

    @Test
    void generarReporteKilometraje_cuandoNoHayPruebasFinalizadas_deberiaRetornarMensaje() {
        when(vehiculoRepository.findByPatente(anyString())).thenReturn(Optional.of(vehiculoDePruebaKm));
        when(pruebaRepository.findAllByVehiculo_Id(anyInt())).thenReturn(Collections.emptyList());
        String reporte = reporteService.generarReporteKilometraje("AA111AA", new Date(), new Date());
        assertEquals("El vehículo no tiene pruebas finalizadas en el período especificado.", reporte);
    }

    @Test
    void generarReporteKilometraje_cuandoHayPruebasYPosiciones_deberiaCalcularDistanciaCorrectamente() {
        when(vehiculoRepository.findByPatente("AA111AA")).thenReturn(Optional.of(vehiculoDePruebaKm));
        when(pruebaRepository.findAllByVehiculo_Id(10)).thenReturn(List.of(pruebaDePruebaKm));
        when(posicionRepository.findAllByVehiculo_IdAndFechaHoraBetweenOrderByFechaHoraAsc(
                10, pruebaDePruebaKm.getFechaHoraInicio().getTime(), pruebaDePruebaKm.getFechaHoraFin().getTime()))
                .thenReturn(posicionesDePrueba);

        String reporte = reporteService.generarReporteKilometraje("AA111AA", new Date(1760564300000L), new Date(1760566300000L));
        assertTrue(reporte.contains("Kilometraje total recorrido: 1,45 km"));
    }

    // =================================================================
    // === NUEVOS TESTS PARA generarReportePruebasPorVehiculo ===
    // =================================================================

    @Test
    void generarReportePruebasPorVehiculo_cuandoVehiculoNoExiste_deberiaRetornarMensajeError() {
        // Arrange
        String patenteInexistente = "XX999XX";
        when(vehiculoRepository.findByPatente(patenteInexistente)).thenReturn(Optional.empty());

        // Act
        String reporte = reporteService.generarReportePruebasPorVehiculo(patenteInexistente);

        // Assert
        assertEquals("Vehículo con patente " + patenteInexistente + " no encontrado.", reporte);
    }

    @Test
    void generarReportePruebasPorVehiculo_cuandoVehiculoNoTienePruebas_deberiaRetornarMensaje() {
        // Arrange
        when(vehiculoRepository.findByPatente(vehiculoDePrueba.getPatente())).thenReturn(Optional.of(vehiculoDePrueba));
        when(pruebaRepository.findAllByVehiculo_Id(vehiculoDePrueba.getId())).thenReturn(Collections.emptyList());

        // Act
        String reporte = reporteService.generarReportePruebasPorVehiculo(vehiculoDePrueba.getPatente());

        // Assert
        assertTrue(reporte.contains("Reporte de Pruebas para el Vehículo: " + vehiculoDePrueba.getPatente()));
        assertTrue(reporte.contains("Modelo: Ford Ranger"));
        assertTrue(reporte.contains("El vehículo no tiene pruebas registradas."));
    }

    @Test
    void generarReportePruebasPorVehiculo_cuandoVehiculoTienePruebas_deberiaRetornarTablaCompleta() {
        // Arrange
        when(vehiculoRepository.findByPatente(vehiculoDePrueba.getPatente())).thenReturn(Optional.of(vehiculoDePrueba));
        when(pruebaRepository.findAllByVehiculo_Id(vehiculoDePrueba.getId())).thenReturn(List.of(pruebaDePrueba1, pruebaDePrueba2));

        // Act
        String reporte = reporteService.generarReportePruebasPorVehiculo(vehiculoDePrueba.getPatente());

        // Assert
        // 1. Verificar la cabecera del reporte
        assertTrue(reporte.contains("Reporte de Pruebas para el Vehículo: AB123CD"));
        assertTrue(reporte.contains("Modelo: Ford Ranger"));
        assertTrue(reporte.contains("ID    | Empleado             | Fecha Inicio         | Incidente"));

        // 2. Verificar la primera prueba (con empleado e incidente)
        String fechaPrueba1 = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(pruebaDePrueba1.getFechaHoraInicio());
        assertTrue(reporte.contains("1     | Juan Perez           | " + fechaPrueba1 + "     | SI"));

        // 3. Verificar la segunda prueba (sin empleado y sin incidente)
        String fechaPrueba2 = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(pruebaDePrueba2.getFechaHoraInicio());
        assertTrue(reporte.contains("2     | N/A                  | " + fechaPrueba2 + "     | NO"));
    }
}