package org.example.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode; // Importar estas
import lombok.ToString; // Importar estas

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Modelos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Modelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    // Aquí no se excluye 'marca' de equals/hashCode/toString porque es el lado ManyToOne
    // y el bucle infinito es menos probable, y es común querer incluir esta relación para
    // la identidad del modelo (ej. Modelo "Civic" de "Honda").
    @ManyToOne
    @JoinColumn(name = "ID_MARCA", nullable = false)
    private Marca marca;

    @Column(name = "DESCRIPCION", nullable = false)
    private String descripcion;

    @EqualsAndHashCode.Exclude // Excluir de equals y hashCode
    @ToString.Exclude         // Excluir de toString
    @OneToMany(mappedBy = "modelo")
    private Set<Vehiculo> vehiculos = new HashSet<>();
}