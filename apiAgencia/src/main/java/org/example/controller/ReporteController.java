package org.example.controller;

import org.example.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    @Autowired
    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/incidentes")
    public ResponseEntity<String> getReportePruebasConIncidentes() {
        String reporte = reporteService.generarReporteIncidentes();
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/incidentes/empleado/{legajo}")
    public ResponseEntity<String> getReporteIncidentesPorEmpleado(@PathVariable Long legajo) {
        String reporte = reporteService.generarReporteIncidentesPorEmpleado(legajo);
        if (reporte.contains("no encontrado")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(reporte);
        }
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/vehiculo/{patente}")
    public ResponseEntity<String> getReporteDePruebasPorVehiculo(@PathVariable String patente) {
        String reporte = reporteService.generarReportePruebasPorVehiculo(patente);
        if (reporte.contains("no encontrado")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(reporte);
        }
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/kilometraje/{patente}")
    public ResponseEntity<String> getReporteKilometraje(
            @PathVariable String patente,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        String reporte = reporteService.generarReporteKilometraje(patente, fechaInicio, fechaFin);
        if (reporte.contains("no encontrado")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(reporte);
        }
        return ResponseEntity.ok(reporte);
    }
}