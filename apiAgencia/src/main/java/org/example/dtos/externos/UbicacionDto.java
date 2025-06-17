package org.example.dtos.externos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UbicacionDto {
    // Los nombres coinciden con el JSON ("latitud", "longitud"), así que no se necesita @JsonProperty.
    private Double latitud;
    private Double longitud;
}