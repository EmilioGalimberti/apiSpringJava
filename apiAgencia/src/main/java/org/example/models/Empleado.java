package org.example.models;

import jakarta.persistence.*; // Usamos jakarta.persistence para JPA
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity // Indica que esta clase es una entidad JPA y se mapea a una tabla de BD
@Table(name = "Empleados") // Mapea a la tabla "Empleados"
@Data // Anotación de Lombok: Genera getters, setters, toString, equals y hashCode
@NoArgsConstructor // Lombok: Genera un constructor sin argumentos
@AllArgsConstructor // Lombok: Genera un constructor con todos los argumentos
public class Empleado {

    @Id // Marca el campo como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Generación automática de IDs (autoincrementales)
    @Column(name = "LEGAJO") // Mapea al nombre de la columna "LEGAJO" en la BD
    private Long legajo; // Usamos Long para IDs que pueden ser numéricos y autoincrementales

    @Column(name = "NOMBRE", nullable = false) // Mapea a la columna "NOMBRE", no puede ser nulo
    private String nombre;

    @Column(name = "APELLIDO", nullable = false) // Mapea a la columna "APELLIDO", no puede ser nulo
    private String apellido;

    @Column(name = "TELEFONO_CONTACTO") // Mapea a la columna "TELEFONO_CONTACTO"
    private Integer telefonoContacto;

    // Bidireccional en JPA , por ahora no la agrego ver si llagamos a tnesr que necesitar
    @EqualsAndHashCode.Exclude // Excluir de equals y hashCode
    @ToString.Exclude         // Excluir de toString para evitar bucles infinitos
    //Ver la opcion de agregarle el cascade
    @OneToMany(mappedBy = "empleado")
    private Set<Prueba> pruebas = new HashSet<>();

    //Constructor solo para mejorar la legibilidad
    public Empleado(String nombre, String apellido, Integer telefonoContacto) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefonoContacto = telefonoContacto;
    }
}