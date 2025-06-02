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
@Table(name = "Vehiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "PATENTE", nullable = false, unique = true)
    private String patente;

    // No se excluye 'modelo' por las mismas razones que 'marca' en Modelo.java
    @ManyToOne
    @JoinColumn(name = "ID_MODELO", nullable = false)
    private Modelo modelo;

    @EqualsAndHashCode.Exclude // Excluir de equals y hashCode
    @ToString.Exclude         // Excluir de toString
    @OneToMany(mappedBy = "vehiculo")
    private Set<Prueba> pruebas = new HashSet<>();

    @EqualsAndHashCode.Exclude // Excluir de equals y hashCode
    @ToString.Exclude         // Excluir de toString
    @OneToMany(mappedBy = "vehiculo")
    private Set<Posicion> posiciones = new HashSet<>();
}