package org.example.repositories;

import org.example.models.Interesado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InteresadoRepository extends JpaRepository<Interesado, Long> {
    // Puedes agregar métodos personalizados aquí, por ejemplo:
    //Interesado findByDocumento(String documento);
}