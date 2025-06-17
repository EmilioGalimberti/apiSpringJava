package org.example.dtos.externos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CoordenadasDto {
    private Double latitud;
    private Double longitud;
}