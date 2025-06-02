package org.example.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Interesados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interesado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TIPO_DOCUMENTO", nullable = false)
    private String tipoDocumento;

    @Column(name = "DOCUMENTO", nullable = false, unique = true)
    private String documento;

    @Column(name = "NOMBRE", nullable = false)
    private String nombre;

    @Column(name = "APELLIDO", nullable = false)
    private String apellido;

    @Column(name = "RESTRINGIDO")
    private Boolean restringido;

    @Column(name = "NRO_LICENCIA")
    private Integer nroLicencia;

    @Column(name = "FECHA_VENCIMIENTO_LICENCIA")
    private Date fechaVencimientoLicencia;

    @EqualsAndHashCode.Exclude // Excluir de equals y hashCode
    @ToString.Exclude         // Excluir de toString
    @OneToMany(mappedBy = "interesado")
    private Set<Prueba> pruebas = new HashSet<>();

    public Interesado(String tipoDocumento, String documento, String nombre, String apellido, Boolean restringido, Integer nroLicencia, Date fechaVencimientoLicencia) {
        this.tipoDocumento = tipoDocumento;
        this.documento = documento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.restringido = restringido;
        this.nroLicencia = nroLicencia;
        this.fechaVencimientoLicencia = fechaVencimientoLicencia;
    }
}