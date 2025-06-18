package org.example.dtos;

import lombok.Data;

@Data
public class PosicionDto {
    private Integer id;
    private VehiculoDto vehiculo;
    private Coordenadas coordenadas;
    private String mensaje;

    @Data
    public static class Coordenadas {
        private double lat;
        private double lon;
    }

}
