package org.example.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface Posicion extends JpaRepository<Posicion, Long> {
    //@Query("SELECT p FROM Posicion p WHERE p.vehiculo.id = :idVehiculo AND p.fechaHora BETWEEN :inicio AND :fin")
    //List<Posicion> findByIdVehiculoAndFechaHoraBetween(@Param("idVehiculo") Integer idVehiculo,
    //                                                   @Param("inicio") Date inicio,
    //                                                   @Param("fin") Date fin);

    // Encuentra la última posición de un vehículo por ID, ordenado por fecha y hora descendente
    //Posicion findFirstByVehiculoIdOrderByFechaHoraDesc(Integer id);

}
