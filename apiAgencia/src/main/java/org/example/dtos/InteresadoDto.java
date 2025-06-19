package org.example.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.models.Interesado;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteresadoDto {
    private Long id;
    private String tipoDocumento;
    private String documento;
    private String nombre;
    private String apellido;
    private Boolean restringido;
    private Integer nroLicencia;
    private Date fechaVencimientoLicencia;

    public InteresadoDto(Interesado interesado) {
        this.id = interesado.getId();
        this.tipoDocumento = interesado.getTipoDocumento();
        this.documento = interesado.getDocumento();
        this.nombre = interesado.getNombre();
        this.apellido = interesado.getApellido();
        this.restringido = interesado.getRestringido();
        this.nroLicencia = interesado.getNroLicencia();
        this.fechaVencimientoLicencia = interesado.getFechaVencimientoLicencia();
    }
}
