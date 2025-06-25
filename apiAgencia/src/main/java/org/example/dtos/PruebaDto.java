package org.example.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.models.Prueba;

import java.util.Date;

@Data
@AllArgsConstructor
public class PruebaDto {
    private Integer id;
    private VehiculoDto vehiculo;
    private EmpleadoDto empleado;
    private InteresadoDto interesado;
    private Date fechaHoraInicio;
    private Date fechaHoraFin;
    private String comentarios;
    private Boolean incidente;

    public PruebaDto(Prueba prueba) {
        this.id = prueba.getId();
        this.vehiculo = new VehiculoDto(prueba.getVehiculo());
        this.empleado = new EmpleadoDto(prueba.getEmpleado());
        this.interesado = new InteresadoDto(prueba.getInteresado());
        this.fechaHoraInicio = prueba.getFechaHoraInicio();
        this.fechaHoraFin = prueba.getFechaHoraFin();
        this.comentarios = prueba.getComentarios();
        this.incidente = prueba.getIncidente();
    }

    public PruebaDto() {}
}
