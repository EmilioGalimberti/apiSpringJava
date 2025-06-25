package org.example.service;

import org.example.dtos.PruebaDto;
import org.example.models.Empleado;
import org.example.models.Posicion;
import org.example.models.Prueba;
import org.example.models.Vehiculo;
import org.example.repositories.EmpleadoRepository;
import org.example.repositories.PosicionRepository;
import org.example.repositories.PruebaRepository;
import org.example.repositories.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private final PruebaService pruebaService;
    private final EmpleadoRepository empleadoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PruebaRepository pruebaRepository;
    private final PosicionRepository posicionRepository;

    private static final double RADIO_TIERRA_KM = 6371.0;

    @Autowired
    public ReporteService(PruebaService pruebaService, EmpleadoRepository empleadoRepository, VehiculoRepository vehiculoRepository, PruebaRepository pruebaRepository, PosicionRepository posicionRepository) {
        this.pruebaService = pruebaService;
        this.empleadoRepository = empleadoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.pruebaRepository = pruebaRepository;
        this.posicionRepository = posicionRepository;
    }

    public String generarReporteIncidentes() {
        List<PruebaDto> pruebas = pruebaService.getPruebasConIncidentes();
        if (pruebas.isEmpty()) {
            return "No hay pruebas con incidentes registradas.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Reporte de Pruebas con Incidentes\n");
        sb.append("=====================================\n");
        sb.append(String.format("%-5s | %-10s | %-20s | %-20s | %-20s | %-20s | %-10s\n",
                "ID", "Patente", "Empleado", "Interesado", "Fecha Inicio", "Fecha Fin", "Incidente"));
        sb.append("-----|------------|----------------------|----------------------|----------------------|----------------------|-----------\n");
        for (PruebaDto prueba : pruebas) {
            sb.append(String.format("%-5s | %-10s | %-20s | %-20s | %-20s | %-20s | %-10s\n",
                    prueba.getId(),
                    prueba.getVehiculo() != null ? prueba.getVehiculo().getPatente() : "N/A",
                    prueba.getEmpleado() != null ? prueba.getEmpleado().getNombre() + " " + prueba.getEmpleado().getApellido() : "N/A",
                    prueba.getInteresado() != null ? prueba.getInteresado().getNombre() + " " + prueba.getInteresado().getApellido() : "N/A",
                    prueba.getFechaHoraInicio() != null ? prueba.getFechaHoraInicio().toString() : "N/A",
                    prueba.getFechaHoraFin() != null ? prueba.getFechaHoraFin().toString() : "N/A",
                    prueba.getIncidente() != null && prueba.getIncidente() ? "SI" : "NO"
            ));
        }
        return sb.toString();
    }

    public String generarReporteIncidentesPorEmpleado(Long legajo) {
        Optional<Empleado> empleadoOpt = empleadoRepository.findById(legajo);
        if (empleadoOpt.isEmpty()) {
            return "Empleado con legajo " + legajo + " no encontrado.";
        }
        Empleado empleado = empleadoOpt.get();

        List<PruebaDto> pruebas = pruebaService.getIncidentesPorEmpleado(legajo);

        StringBuilder sb = new StringBuilder();
        sb.append("Reporte de Incidentes para el Empleado\n");
        sb.append("======================================\n");
        sb.append("Legajo: ").append(empleado.getLegajo()).append("\n");
        sb.append("Nombre: ").append(empleado.getNombre()).append(" ").append(empleado.getApellido()).append("\n\n");

        if (pruebas.isEmpty()) {
            sb.append("El empleado no tiene incidentes registrados.\n");
        } else {
            sb.append(String.format("%-5s | %-10s | %-25s | %s\n", "ID", "Patente", "Fecha Inicio", "Comentarios"));
            sb.append("-----|------------|---------------------------|----------------------------------\n");

            for (PruebaDto prueba : pruebas) {
                sb.append(String.format("%-5d | %-10s | %-25s | %s\n",
                        prueba.getId(),
                        prueba.getVehiculo() != null ? prueba.getVehiculo().getPatente() : "N/A",
                        prueba.getFechaHoraInicio() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(prueba.getFechaHoraInicio()) : "N/A",
                        prueba.getComentarios() != null ? prueba.getComentarios() : ""
                ));
            }
        }
        return sb.toString();
    }

    public String generarReportePruebasPorVehiculo(String patente) {
        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findByPatente(patente);
        if (vehiculoOpt.isEmpty()) {
            return "Vehículo con patente " + patente + " no encontrado.";
        }
        Vehiculo vehiculo = vehiculoOpt.get();

        List<Prueba> pruebas = pruebaRepository.findAllByVehiculo_Id(vehiculo.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("Reporte de Pruebas para el Vehículo: ").append(vehiculo.getPatente()).append("\n");
        sb.append("=========================================================\n");
        sb.append("Modelo: ").append(vehiculo.getModelo().getMarca().getNombre()).append(" ").append(vehiculo.getModelo().getDescripcion()).append("\n\n");

        if (pruebas.isEmpty()) {
            sb.append("El vehículo no tiene pruebas registradas.\n");
        } else {
            sb.append(String.format("%-5s | %-20s | %-20s | %-10s\n", "ID", "Empleado", "Fecha Inicio", "Incidente"));
            sb.append("-----|----------------------|----------------------|-----------\n");

            for (Prueba prueba : pruebas) {
                String nombreEmpleado = prueba.getEmpleado() != null ? prueba.getEmpleado().getNombre() + " " + prueba.getEmpleado().getApellido() : "N/A";
                String fechaInicio = prueba.getFechaHoraInicio() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(prueba.getFechaHoraInicio()) : "N/A";
                String incidente = prueba.getIncidente() != null && prueba.getIncidente() ? "SI" : "NO";

                sb.append(String.format("%-5s | %-20s | %-20s | %-10s\n",
                        prueba.getId(),
                        nombreEmpleado,
                        fechaInicio,
                        incidente
                ));
            }
        }
        return sb.toString();
    }

    public String generarReporteKilometraje(String patente, Date fechaInicio, Date fechaFin) {
        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findByPatente(patente);
        if (vehiculoOpt.isEmpty()) {
            return "Vehículo con patente " + patente + " no encontrado.";
        }
        Vehiculo vehiculo = vehiculoOpt.get();

        Calendar c = Calendar.getInstance();
        c.setTime(fechaFin);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        Date fechaFinAjustada = c.getTime();

        List<Prueba> pruebasDelVehiculo = pruebaRepository.findAllByVehiculo_Id(vehiculo.getId());

        List<Prueba> pruebasEnPeriodo = pruebasDelVehiculo.stream()
                .filter(p -> p.getFechaHoraFin() != null && !p.getFechaHoraInicio().after(fechaFinAjustada) && !p.getFechaHoraFin().before(fechaInicio))
                .collect(Collectors.toList());

        if (pruebasEnPeriodo.isEmpty()) {
            return "El vehículo no tiene pruebas finalizadas en el período especificado.";
        }

        double kilometrajeTotal = 0.0;

        for (Prueba prueba : pruebasEnPeriodo) {
            List<Posicion> posiciones = posicionRepository.findAllByVehiculo_IdAndFechaHoraBetweenOrderByFechaHoraAsc(
                    vehiculo.getId(),
                    prueba.getFechaHoraInicio().getTime(),
                    prueba.getFechaHoraFin().getTime()
            );

            for (int i = 0; i < posiciones.size() - 1; i++) {
                kilometrajeTotal += calcularDistancia(posiciones.get(i), posiciones.get(i + 1));
            }
        }

        String kilometrajeFormateado = String.format(java.util.Locale.forLanguageTag("es-ES"), "%.2f", kilometrajeTotal);


        if (kilometrajeTotal > 0) {
            return String.format("Reporte de Kilometraje para el Vehículo: %s\n" +
                            "Período: %s - %s\n\n" +
                            "Kilometraje total recorrido: %s km",
                    patente,
                    new SimpleDateFormat("dd/MM/yyyy").format(fechaInicio),
                    new SimpleDateFormat("dd/MM/yyyy").format(fechaFin),
                    kilometrajeFormateado);
        } else {
            return String.format("El vehículo %s no registró posiciones en el período especificado.", patente);
        }
    }

    private double calcularDistancia(Posicion p1, Posicion p2) {
        double dLat = Math.toRadians(p2.getLatitud() - p1.getLatitud());
        double dLon = Math.toRadians(p2.getLongitud() - p1.getLongitud());

        double lat1 = Math.toRadians(p1.getLatitud());
        double lat2 = Math.toRadians(p2.getLatitud());

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_KM * c;
    }
}