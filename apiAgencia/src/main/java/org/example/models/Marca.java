package org.example.models;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Marcas")
public class Marca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    private String nombre;



    //Revisra despues bien esto porque esta onToMany desde los dos lados y nose si estaria bien
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "marca")//cascade = CascadeType.PERSIST)
    private Set<Modelo> modelos = new HashSet<>();

}