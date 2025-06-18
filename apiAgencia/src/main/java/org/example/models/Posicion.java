package org.example.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "Posiciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Posicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ID_VEHICULO", nullable = false)
    private Vehiculo vehiculo;

    @Column(name = "FECHA_HORA", nullable = false)
    private Date fechaHora;

    @Column(name = "LATITUD", nullable = false)
    private Double latitud;

    @Column(name = "LONGITUD", nullable = false)
    private Double longitud;
}