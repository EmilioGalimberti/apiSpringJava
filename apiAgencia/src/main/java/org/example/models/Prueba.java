package org.example.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// En Prueba, generalmente no es necesario excluir las referencias ManyToOne
// de equals/hashCode/toString a menos que tengas un requisito específico.
// Esto se debe a que no forman un ciclo directo (no tienen un OneToMany que apunte de vuelta a Prueba)
// y generalmente quieres que estos atributos formen parte de la identidad lógica de una Prueba.
@Entity
@Table(name = "Pruebas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prueba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_VEHICULO", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne
    @JoinColumn(name = "ID_INTERESADO", nullable = false)
    private Interesado interesado;

    @ManyToOne
    @JoinColumn(name = "ID_EMPLEADO", nullable = false)
    private Empleado empleado;

    @Column(name = "FECHA_HORA_INICIO", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "FECHA_HORA_FIN")
    private LocalDateTime fechaHoraFin;

    @Column(name = "COMENTARIOS", length = 1000)
    private String comentarios;

    @Column(name = "EN_CURSO", nullable = false)
    private Boolean enCurso;

    public Prueba(Vehiculo vehiculo, Interesado interesado, Empleado empleado, LocalDateTime fechaHoraInicio) {
        this.vehiculo = vehiculo;
        this.interesado = interesado;
        this.empleado = empleado;
        this.fechaHoraInicio = fechaHoraInicio;
        this.enCurso = true;
    }
}