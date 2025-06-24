package org.example.repositories;

import org.example.models.Posicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PosicionRepository extends JpaRepository<Posicion, Integer> {
    //@Query("SELECT p FROM Posicion p WHERE p.vehiculo.id = :idVehiculo AND p.fechaHora BETWEEN :inicio AND :fin")
    //List<Posicion> findByIdVehiculoAndFechaHoraBetween(@Param("idVehiculo") Integer idVehiculo,
    //                                                   @Param("inicio") Date inicio,
    //                                                   @Param("fin") Date fin);

    // Encuentra la última posición de un vehículo por ID, ordenado por fecha y hora descendente
    //Posicion findFirstByVehiculoIdOrderByFechaHoraDesc(Integer id);

    List<Posicion> findAllByVehiculo_IdAndFechaHoraBetweenOrderByFechaHoraAsc(Integer vehiculoId, Long fechaInicio, Long fechaFin);
}
