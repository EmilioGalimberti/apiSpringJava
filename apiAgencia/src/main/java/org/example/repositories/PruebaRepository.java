package org.example.repositories;

import org.example.models.Prueba;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PruebaRepository extends JpaRepository<Prueba, Integer> {
    /*
        La siguiente query hace una consulta sobre si existe alguna prueba
        perteneciente a un vehiculo(buscado segun su id) cuya fecha y hora de
        fin sean nulos, es decir, este en curso
*/
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Prueba p WHERE p.vehiculo.id = :idVehiculo AND p.fechaHoraFin IS NULL")
    boolean existePruebaActiva(@Param("idVehiculo") Integer idVehiculo);

    /*
       consulta derivada de Spring Data JPA. Basándose en el nombre del (findBy...IsNull),
       Spring Data JPA genera automáticamente la consulta JPQL correspondiente (SELECT p FROM Prueba p WHERE p.fechaHoraFin IS NULL).
    */
    List<Prueba> findByFechaHoraFinIsNull();

    long countByFechaHoraFinIsNull();

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

    List<Prueba> findByIncidenteTrue();

    List<Prueba> findByIncidenteTrueAndEmpleado_Legajo(Long legajo);

    List<Prueba> findAllByVehiculo_Id(Integer vehiculoId);

    @Query("SELECT p FROM Prueba p WHERE p.vehiculo.id = :idVehiculo AND p.fechaHoraFin IS NULL")
    Prueba findPruebaActivaByVehiculoId(@Param("idVehiculo") Integer idVehiculo);
}
