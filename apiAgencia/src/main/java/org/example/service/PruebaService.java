package org.example.service;

import org.example.dtos.PruebaDto;
import org.example.models.Empleado;
import org.example.models.Interesado;
import org.example.models.Prueba;
import org.example.models.Vehiculo;
import org.example.repositories.EmpleadoRepository;
import org.example.repositories.InteresadoRepository;
import org.example.repositories.PruebaRepository;
import org.example.repositories.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class PruebaService {
    private final PruebaRepository pruebaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final InteresadoRepository interesadoRepository;

    @Autowired
    public PruebaService(PruebaRepository pruebaRepository, EmpleadoRepository empleadoRepository, VehiculoRepository vehiculoRepository, InteresadoRepository interesadoRepository) {
        this.pruebaRepository = pruebaRepository;
        this.empleadoRepository = empleadoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.interesadoRepository = interesadoRepository;
    }

    /*
     a. Crear una nueva prueba,
     */
    public PruebaDto crearPrueba(PruebaDto pruebaDto) {
        // 1. Validar que el vehículo no esté en otra prueba
        Vehiculo vehiculo = validarVehiculoDisponible(pruebaDto.getVehiculo().getId());
        // 2. Validar que el interesado no esté restringido y tenga licencia vigente
        Interesado interesado = validarInteresado(pruebaDto.getInteresado().getId());
        // 3. Validar que el empleado exista
        Empleado empleado = empleadoRepository.findById(pruebaDto.getEmpleado().getLegajo())
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        // 4. Construir la entidad Prueba y guardar la nueva prueba
        Prueba nuevaPrueba = new Prueba(vehiculo, interesado, empleado, new Date());
        Prueba savedPrueba = pruebaRepository.save(nuevaPrueba);

        // 5. Devolver el DTO de la prueba creada
        return new PruebaDto(savedPrueba);
    }

    /*
    public PruebaDto findById(Integer id) throws ServiceException {
        return pruebaRepository.findById(id).map(PruebaDto::new).orElseThrow(() ->
            new ServiceException("Prueba no encontrada")
        );
    }
     */


    public Iterable<PruebaDto> findAll() {
        Iterable<Prueba> pruebas = pruebaRepository.findAll();
        return StreamSupport.stream(pruebas.spliterator(), false).map(PruebaDto::new).toList();
    }




    /*
    b. Listar todas las pruebas en curso en un momento dado
     */
    public List<PruebaDto> getPruebasEnCurso() {
        // Mapea cada entidad Prueba a un PruebaDTO
        return pruebaRepository.findByFechaHoraFinIsNull().stream().map(PruebaDto::new).toList();
    }

    /*
    public PruebaDto updatePrueba(Integer id, PruebaDto pruebaDto) {
        Prueba existingPrueba = pruebaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prueba no encontrada"));

        existingPrueba.setId(id);
        existingPrueba.setFechaHoraInicio(pruebaDto.getFechaHoraInicio());

        // actualizar relaciones
        Vehiculo vehiculo = vehiculoRepository.findById(pruebaDto.getVehiculo().getId())
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        existingPrueba.setVehiculo(vehiculo);

        Empleado empleado = empleadoRepository.findById(pruebaDto.getEmpleado().getLegajo())
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        existingPrueba.setEmpleado(empleado);

        Interesado interesado = interesadoRepository.findById(pruebaDto.getInteresado().getId())
                .orElseThrow(() -> new IllegalArgumentException("Interesado no encontrado"));
        existingPrueba.setInteresado(interesado);

        Prueba updatedPrueba = pruebaRepository.save(existingPrueba);

        return new PruebaDto(updatedPrueba);
    }
     */

    /*
    c. Finalizar una prueba, permitiéndole al empleado agregar un comentario sobre la misma.
     */
    public PruebaDto finalizarPrueba(Integer id, String comentario) {
        Prueba pruebaEnCurso = pruebaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prueba no encontrada"));

        if (pruebaEnCurso.getFechaHoraFin() != null) {
            throw new IllegalArgumentException("La prueba ya ha sido finalizada.");
        }

        pruebaEnCurso.setFechaHoraFin(new Date());
        pruebaEnCurso.setComentarios(comentario);

        return new PruebaDto(pruebaRepository.save(pruebaEnCurso));
    }


    public void deletePrueba(Integer id) {
        Prueba existingPrueba = pruebaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prueba no encontrada"));
        pruebaRepository.delete(existingPrueba);
    }


    private Vehiculo validarVehiculoDisponible(Integer idVehiculo) {
        Vehiculo vehiculo = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        // todos los vehiculos se asumen patentados por lo que no es necesario validar la patente
        if (pruebaRepository.existePruebaActiva(idVehiculo)) {
            throw new IllegalArgumentException("El vehículo está siendo probado.");
        }
        return vehiculo;
    }

    private Interesado validarInteresado(Long idInteresado) {
        Interesado interesado = interesadoRepository.findById(idInteresado)
                .orElseThrow(() -> new IllegalArgumentException("Interesado no encontrado"));
        if (interesado.getFechaVencimientoLicencia().before(new Date())) {
            throw new IllegalArgumentException("La licencia del interesado está vencida.");
        }
        if (interesado.getRestringido()) {
            throw new IllegalArgumentException("El interesado está restringido para probar vehículos.");
        }
        return interesado;
    }

}
