package org.example.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PosicionDto {
    private Integer id;
    private VehiculoDto vehiculo;
    private Coordenadas coordenadas;
    private String mensaje;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coordenadas {
        private double lat;
        private double lon;
    }

}
