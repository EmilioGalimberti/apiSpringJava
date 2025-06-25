package org.example.dtos.externos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data // Genera getters, setters, toString, etc.
@NoArgsConstructor // Genera un constructor sin argumentos, útil para Jackson
public class RestriccionesDto {

    // Mapea el campo JSON "ubicacion_agencia" a este atributo.
    @JsonProperty("ubicacion_agencia")
    private UbicacionDto ubicacionAgencia;

    // Mapea "radio_maximo_metros" a este atributo.
    @JsonProperty("radio_maximo_metros")
    private Double radioMaximoMetros;

    // Mapea "zonas_peligrosas" a una lista de objetos ZonaPeligrosaDto.
    @JsonProperty("zonas_peligrosas")
    private List<ZonaPeligrosaDto> zonasPeligrosas;

}