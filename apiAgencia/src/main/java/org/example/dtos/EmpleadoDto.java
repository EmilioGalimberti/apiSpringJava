package org.example.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.models.Empleado;

@Data
@AllArgsConstructor
public class EmpleadoDto {
    private Long legajo;
    private String nombre;
    private String apellido;
    private Integer telefonoContacto;

    public EmpleadoDto(Empleado empleado) {
        this.legajo = empleado.getLegajo();
        this.nombre = empleado.getNombre();
        this.apellido = empleado.getApellido();
        this.telefonoContacto = empleado.getTelefonoContacto();
    }
}
