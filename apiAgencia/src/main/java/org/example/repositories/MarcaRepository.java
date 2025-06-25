package org.example.repositories;

import org.example.models.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, Integer> {
    // Spring Data JPA proporciona automáticamente métodos como save(), findById(), findAll(), etc.
    // El segundo parámetro genérico, 'Integer', debe coincidir con el tipo de dato del ID en tu entidad Marca.java.
}