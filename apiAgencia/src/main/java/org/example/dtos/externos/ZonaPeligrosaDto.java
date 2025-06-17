package org.example.dtos.externos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ZonaPeligrosaDto {

    @JsonProperty("id_zona")
    private String idZona;

    @JsonProperty("nombre_zona")
    private String nombreZona;

    // El nombre "coordenadas" coincide, no necesita @JsonProperty.
    private CoordenadasDto coordenadas;

    @JsonProperty("radio_metros")
    private Integer radioMetros;
}