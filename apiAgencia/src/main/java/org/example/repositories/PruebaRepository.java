package org.example.repositories;

import org.example.models.Prueba;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PruebaRepository extends JpaRepository<Prueba, Integer> {
    /*
        La siguiente query hace una consulta sobre si existe alguna prueba
        perteneciente a un vehiculo(buscado segun su id) cuya fecha y hora de
        fin sean nulos, es decir, este en curso

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Prueba p WHERE p.vehiculo.id = ?1 AND p.fechaHoraFin IS NULL")
    boolean existsByVehiculoIdAndFechaHoraFinIsNull(@Param("idVehiculo") Integer idVehiculo);

    List<Prueba> findByFechaHoraFinIsNull();
   */
    /*
        La siguiente query hace una consulta que busca una prueba
        perteneciente a un vehiculo(buscado segun su id) y haya sido notificada

    @Query("SELECT p FROM Prueba p WHERE p.vehiculo.id = :vehiculoId " +
            "AND (p.fechaHoraFin IS NULL AND :fechaNotificacion BETWEEN p.fechaHoraInicio AND CURRENT_TIMESTAMP " +
            "OR :fechaNotificacion BETWEEN p.fechaHoraInicio AND p.fechaHoraFin)")
    Prueba findPruebaByVehiculoIdAndFechaNotificacion(
            @Param("vehiculoId") Integer vehiculoId,
            @Param("fechaNotificacion") LocalDateTime fechaNotificacion);
     */
    /*
        La siguiente query es similar a la anterior, pero busca las pruebas
        para un empleado en particular, buscado por su id

    @Query("SELECT p FROM Prueba p WHERE p.vehiculo.id = :vehiculoId " +
            "AND p.empleado.legajo = :idEmpleado " +
            "AND (p.fechaHoraFin IS NULL AND :fechaNotificacion BETWEEN p.fechaHoraInicio AND CURRENT_TIMESTAMP " +
            "OR :fechaNotificacion BETWEEN p.fechaHoraInicio AND p.fechaHoraFin)")
    Prueba findPruebaByVehiculoIdAndFechaNotificacionAndEmpleado(
            @Param("vehiculoId") Integer vehiculoId,
            @Param("fechaNotificacion") LocalDateTime fechaNotificacion,
            @Param("idEmpleado") Integer idEmpleado);
    */
}
