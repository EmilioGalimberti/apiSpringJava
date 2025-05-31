package org.example.repositories;

import org.example.models.Posicion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosicionRepository extends JpaRepository<Posicion, Long> {
    //@Query("SELECT p FROM Posicion p WHERE p.vehiculo.id = :idVehiculo AND p.fechaHora BETWEEN :inicio AND :fin")
    //List<Posicion> findByIdVehiculoAndFechaHoraBetween(@Param("idVehiculo") Integer idVehiculo,
    //                                                   @Param("inicio") Date inicio,
    //                                                   @Param("fin") Date fin);

    // Encuentra la última posición de un vehículo por ID, ordenado por fecha y hora descendente
    //Posicion findFirstByVehiculoIdOrderByFechaHoraDesc(Integer id);

}
