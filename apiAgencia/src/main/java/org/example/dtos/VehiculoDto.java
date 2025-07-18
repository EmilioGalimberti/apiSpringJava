package org.example.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.models.Vehiculo;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehiculoDto {
    private Integer id;
    private String patente;
    private Long IdModelo;

    public VehiculoDto(Vehiculo vehiculo) {
        this.id = vehiculo.getId();
        this.patente = vehiculo.getPatente();
        this.IdModelo = vehiculo.getModelo().getId();
    }
}
