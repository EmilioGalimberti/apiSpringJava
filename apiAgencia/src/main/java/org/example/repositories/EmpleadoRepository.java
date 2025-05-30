/*
Los repositorios son la capa de acceso a datos de tu aplicación. Sirven como una abstracción sobre la base de datos, p
roporcionando una forma sencilla y orientada a objetos para realizar operaciones CRUD (Crear, Leer, Actualizar, Borrar)
y otras consultas más complejas sobre tus entidades.
 */


package org.example.repositories;


import org.example.models.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


//Aca podemso usar CrudRepository PREGUNTAAR BIEN

@Repository // Indica que esta interfaz es un componente de Spring de tipo repositorio
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    // JpaRepository<[Clase de Entidad], [Tipo de dato de la clave primaria]>
    // Spring Data JPA ya proporciona métodos CRUD básicos: save, findById, findAll, delete, etc.
    // Puedes agregar métodos personalizados aquí si necesitas consultas específicas, ej:
    // Optional<Empleado> findByNombreAndApellido(String nombre, String apellido);
}