package org.example.service;

import org.example.dtos.PosicionDto;
import org.example.dtos.externos.RestriccionesDto;
import org.example.models.Posicion;
import org.example.models.Vehiculo;
import org.example.repositories.PosicionRepository;
import org.example.repositories.PruebaRepository;
import org.example.repositories.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono; // Importamos Mono

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class VehiculoService {
    private final ExternalApisService externalApisService;
    private final VehiculoRepository vehiculoRepository;
    private final PruebaRepository pruebaRepository;
    private final PosicionRepository posicionRepository;
    private final KafkaProducer kafkaProducer;

    @Autowired
    public VehiculoService(ExternalApisService externalApisService, VehiculoRepository vehiculoRepository, PruebaRepository pruebaRepository, PosicionRepository posicionRepository, KafkaProducer kafkaProducer) {
        this.externalApisService = externalApisService;
        this.vehiculoRepository = vehiculoRepository;
        this.pruebaRepository = pruebaRepository;
        this.posicionRepository = posicionRepository;
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * Endpoint de prueba para obtener las restricciones actuales directamente
     * desde el servicio externo.
     * @return Un Mono que emite el DTO de Restricciones.
     */
    public Mono<RestriccionesDto> getRestriccionesActuales() {
        // Simplemente llamamos al método del servicio y devolvemos el Mono.
        // Spring se encargará de suscribirse y convertir el resultado a JSON.
        return externalApisService.getRestricciones();
    }

//    // 1. Recibe un PosicionDto con los datos de entrada y devuelve un Mono<PosicionDto>. Esto significa: "Te prometo que en el futuro te daré un PosicionDto con el resultado del procesamiento".
//    public Mono<PosicionDto> procesarPosicion(PosicionDto posicionDto){
//
//        // 2. Se llama al servicio externo de forma no-bloqueante. La ejecución no se detiene a esperar. En su lugar, se obtiene un Mono que contendrá las restricciones cuando lleguen.
//        return externalApisService.getRestricciones()
//                .switchIfEmpty(Mono.error(new IllegalStateException("No se pudieron obtener las restricciones desde el servicio externo."))) // Manejo de error si el Mono viene vacío.
//                .flatMap(restricciones -> {
//                    // 3. TODA LA LÓGICA ANTERIOR AHORA VIVE DENTRO DEL FLATMAP.
//                    //    Este bloque solo se ejecuta cuando el objeto 'restricciones' está disponible.
//                    Posicion posicionGuardada = guardarPosicion(posicionDto);
//                    PosicionDto posicionRespuesta = construirPosicionRespuesta(posicionDto, posicionGuardada);
//
//                    if (estaPosicionFueraRadioAdmitido(posicionRespuesta, restricciones)){
//                        posicionRespuesta.setMensaje("La posicion actual del vehiculo se encuentra por fuera del radio permitido por la agencia.");
//                        kafkaProducer.enviarMensajeRadioExcedido(posicionRespuesta);
//                        return Mono.just(posicionRespuesta); // Devolvemos la respuesta envuelta en un Mono
//                    }
//
//                    if (estaEnZonaRestringida(posicionRespuesta, restricciones)){
//                        posicionRespuesta.setMensaje("La posicion actual del vehiculo se encuentra dentro de un area restringida.");
//                        kafkaProducer.enviarMensajeZonaPeligrosa(posicionRespuesta);
//                        return Mono.just(posicionRespuesta);
//                    }
//
//                    posicionRespuesta.setMensaje("La posicion actual del vehiculo fue registrada.");
//
//                    // 4. DEVOLUCIÓN REACTIVA: Devolvemos el resultado final envuelto en un Mono.
//                    return Mono.just(posicionRespuesta);
//                });
//    }
//
//    // El resto de métodos privados no necesitan cambios, ya que son lógica síncrona
//    // que ahora es llamada desde dentro de la cadena reactiva.
//
//    private PosicionDto construirPosicionRespuesta(PosicionDto posicionDto, Posicion posicionGuardada) {
//        // ... sin cambios
//        posicionDto.setId(posicionGuardada.getId());
//        posicionDto.getVehiculo().setPatente(posicionGuardada.getVehiculo().getPatente());
//        posicionDto.getVehiculo().setIdModelo(posicionGuardada.getVehiculo().getModelo().getId());
//        return posicionDto;
//    }
//
//    private Vehiculo validarVehhiculoEnPrueba(Integer idVehiculo){
//        // ... sin cambios
//        Vehiculo vehiculo = vehiculoRepository.findById(idVehiculo)
//                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
//        if (pruebaRepository.existePruebaActiva(idVehiculo)) {
//            return vehiculo;
//        }
//        throw new IllegalArgumentException("El vehículo no esta siendo probado en este momento. Inicia una prueba para registrar la posicion.");
//    }
//
//    private Posicion buildPosicionFromDto(PosicionDto posicionDto){
//        // ... sin cambios
//        Vehiculo vehiculo = validarVehhiculoEnPrueba(posicionDto.getVehiculo().getId());
//        Posicion posicion = new Posicion();
//        posicion.setVehiculo(vehiculo);
//        posicion.setLatitud(posicionDto.getCoordenadas().getLat());
//        posicion.setLongitud(posicionDto.getCoordenadas().getLon());
//        posicion.setFechaHora(new LocalDateTime());
//        return posicion;
//    }
//
//    private Posicion guardarPosicion(PosicionDto posicionDto) {
//        // ... sin cambios
//        Posicion nuevaPosicion = this.buildPosicionFromDto(posicionDto);
//        return posicionRepository.save(nuevaPosicion);
//    }
//
//    private boolean estaPosicionFueraRadioAdmitido(PosicionDto posicion, RestriccionesDto restricciones){
//        // ... sin cambios
//        double distance = Math.sqrt(Math.pow(posicion.getCoordenadas().getLat() - restricciones.getCoordenadasAgencia().getLat(), 2)
//                + Math.pow(posicion.getCoordenadas().getLon() - restricciones.getCoordenadasAgencia().getLon(), 2));
//        return distance > restricciones.getRadioAdmitidoKm();
//    }
//
//    private boolean estaEnZonaRestringida(PosicionDto posicion, RestriccionesDto restricciones) {
//        // ... sin cambios
//        return restricciones.getZonasRestringidas().stream().anyMatch(zona -> {
//            double latVehiculo = posicion.getCoordenadas().getLat();
//            double lonVehiculo = posicion.getCoordenadas().getLon();
//            double latNoroeste = zona.getNoroeste().getLat();
//            double lonNoroeste = zona.getNoroeste().getLon();
//            double latSureste = zona.getSureste().getLat();
//            double lonSureste = zona.getSureste().getLon();
//
//            return latVehiculo <= latNoroeste && latVehiculo >= latSureste
//                    && lonVehiculo >= lonNoroeste && lonVehiculo <= lonSureste;
//        });
//    }

}