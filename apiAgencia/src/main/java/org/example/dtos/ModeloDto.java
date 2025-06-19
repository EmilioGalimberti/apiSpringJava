package org.example.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.models.Modelo;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModeloDto {
    private Long id;
    private String descripcion;
    private Integer idMarca;
    private String nombreMarca; // Útil para devolver también el nombre de la marca en las respuestas

    // Constructor que facilita la conversión de una Entidad a un DTO
    public ModeloDto(Modelo modelo) {
        this.id = modelo.getId();
        this.descripcion = modelo.getDescripcion();
        if (modelo.getMarca() != null) {
            this.idMarca = modelo.getMarca().getId();
            this.nombreMarca = modelo.getMarca().getNombre();
        }
    }
}