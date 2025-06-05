package org.example.repositories;

import org.example.models.Modelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModeloRepository extends JpaRepository<Modelo, Long> {
    // El segundo parámetro genérico, 'Long', debe coincidir con el tipo de dato del ID en tu entidad Modelo.java.
}