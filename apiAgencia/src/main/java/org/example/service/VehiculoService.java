package org.example.service;

import org.example.dtos.PosicionDto;
import org.example.dtos.externos.RestriccionesDto;
import org.example.models.Posicion;
import org.example.models.Prueba;
import org.example.models.Vehiculo;
import org.example.repositories.PosicionRepository;
import org.example.repositories.PruebaRepository;
import org.example.repositories.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono; // Importamos Mono
import reactor.core.scheduler.Schedulers;
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

    // PARA EL CALCULO DE DISTANCIA
    private static final double RADIO_TERRESTRE_METROS = 6371000; // Radio promedio de la Tierra en metros


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

    // 1. Recibe un PosicionDto con los datos de entrada y devuelve un Mono<PosicionDto>. Esto significa: "Te prometo que en el futuro te daré un PosicionDto con el resultado del procesamiento".
    public Mono<PosicionDto> procesarPosicion(PosicionDto posicionDto){

        // 2. Se llama al servicio externo de forma no-bloqueante. La ejecución no se detiene a esperar. En su lugar, se obtiene un Mono que contendrá las restricciones cuando lleguen.
        return externalApisService.getRestricciones()
                .switchIfEmpty(Mono.error(new IllegalStateException("No se pudieron obtener las restricciones desde el servicio externo."))) // Manejo de error si el Mono viene vacío.
                .flatMap(restricciones -> {
                    //    Este bloque solo se ejecuta cuando el objeto 'restricciones' está disponible.

                    // 3. Aquí se guarda la posición en la base de datos de forma reactiva.
                    Posicion posicionGuardada = guardarPosicion(posicionDto);

                    // 4. Se construye la respuesta a partir del PosicionDto original y el objeto guardado.
                    // Toma los datos de la posición recién guardada (como su nuevo ID) y enriquece el DTO para repuesta.
                    PosicionDto posicionRespuesta = construirPosicionRespuesta(posicionDto, posicionGuardada);


                    /* Se verifica el radio antes que las zonas restringidas ya que si se encuentra alejado no seria necesario
                    verificar las zonas restringidas y verificar el radio es un calculo mas sencillo y eficiente que verificar
                    todas las zonas restringidas que se deben reccorer con un loop.
                        */


                    if (estaPosicionFueraRadioAdmitido(posicionRespuesta, restricciones)){
                        posicionRespuesta.setMensaje("La posicion actual del vehiculo se encuentra por fuera del radio permitido por la agencia.");

                        marcarIncidenteEnPruebaActiva(posicionRespuesta.getVehiculo().getId());

                        //KAFKA ASINCRONO,
                        //permite que siga funcionando sin depedner de que reciba la notificacion el cliente
                        // en especial agregue esto porque no esta el kafka cliente
                        //Hacemos que el envío a Kafka no bloquee.
                        Mono.fromRunnable(() -> kafkaProducer.enviarMensajeRadioExcedido(posicionRespuesta))
                                .subscribeOn(Schedulers.boundedElastic()) // Ejecuta esto en otro hilo
                                .subscribe(); // Inicia la operación "dispara y olvida"
                        return Mono.just(posicionRespuesta); // Devolvemos la respuesta envuelta en un Mono
                    }

                    if (estaEnZonaRestringida(posicionRespuesta, restricciones)){
                        posicionRespuesta.setMensaje("La posicion actual del vehiculo se encuentra dentro de un area restringida.");
                        marcarIncidenteEnPruebaActiva(posicionRespuesta.getVehiculo().getId());
                        Mono.fromRunnable(() -> kafkaProducer.enviarMensajeZonaPeligrosa(posicionRespuesta))
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe();
                        return Mono.just(posicionRespuesta);
                    }

                    posicionRespuesta.setMensaje("La posicion actual del vehiculo fue registrada.");

                    // 4. DEVOLUCIÓN REACTIVA: Devolvemos el resultado final envuelto en un Mono.
                    return Mono.just(posicionRespuesta);
                });
    }

    // 3. Este metod guarda la posición en la base de datos y devuelve el objeto guardado.
    //Orquesta la validación y el guardado.
    private Posicion guardarPosicion(PosicionDto posicionDto) {
        Posicion nuevaPosicion = this.buildPosicionFromDto(posicionDto);
        return posicionRepository.save(nuevaPosicion);
    }

    // Convierte el objeto de transferencia de datos (PosicionDto) en una entidad de base de datos (Posicion), realizando la validación del vehículo en el proceso.
    private Posicion buildPosicionFromDto(PosicionDto posicionDto){
        Vehiculo vehiculo = validarVehhiculoEnPrueba(posicionDto.getVehiculo().getId());
        Posicion posicion = new Posicion();
        posicion.setVehiculo(vehiculo);
        posicion.setLatitud(posicionDto.getCoordenadas().getLat());
        posicion.setLongitud(posicionDto.getCoordenadas().getLon());
        posicion.setFechaHora(System.currentTimeMillis());
        return posicion;
    }
    //Una regla de negocio crucial. Usa los repositorios para asegurar que la posición que se está registrando pertenece a un vehículo real que está actualmente en una prueba de manejo.
    private Vehiculo validarVehhiculoEnPrueba(Integer idVehiculo){
        Vehiculo vehiculo = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        if (pruebaRepository.existePruebaActiva(idVehiculo)) {
            return vehiculo;
        }
        throw new IllegalArgumentException("El vehículo no esta siendo probado en este momento. Inicia una prueba para registrar la posicion.");
    }

    // Este métod construye el DTO de respuesta a partir del DTO original y la entidad guardada.
    // Un simple formateador para preparar el DTO de salida.
    private PosicionDto construirPosicionRespuesta(PosicionDto posicionDto, Posicion posicionGuardada) {
        posicionDto.setId(posicionGuardada.getId());
        posicionDto.getVehiculo().setPatente(posicionGuardada.getVehiculo().getPatente());
        posicionDto.getVehiculo().setIdModelo(posicionGuardada.getVehiculo().getModelo().getId());
        return posicionDto;
    }

    /**
     * Verifica si la posición del vehículo está fuera del radio admitido por la agencia.
     * Utiliza el nuevo metodo de cálculo de distancia basado en Haversine.
     *
     * @param posicion La posición del vehículo a verificar.
     * @param restricciones Las restricciones de la agencia que incluyen ubicación y radio máximo.
     * @return true si la posición está fuera del radio admitido, false en caso contrario.
     */
    private boolean estaPosicionFueraRadioAdmitido(PosicionDto posicion, RestriccionesDto restricciones) {
        double latAgencia = restricciones.getUbicacionAgencia().getLatitud();
        double lonAgencia = restricciones.getUbicacionAgencia().getLongitud();
        double latVehiculo = posicion.getCoordenadas().getLat();
        double lonVehiculo = posicion.getCoordenadas().getLon();

        double distanciaEnMetros = calcularDistanciaHaversine(latVehiculo, lonVehiculo, latAgencia, lonAgencia);

        // Ahora la comparación es correcta: metros vs metros.
        return distanciaEnMetros > restricciones.getRadioMaximoMetros();
    }

    private boolean estaEnZonaRestringida(PosicionDto posicion, RestriccionesDto restricciones) {
        return restricciones.getZonasPeligrosas().stream().anyMatch(zona -> {
            double latVehiculo = posicion.getCoordenadas().getLat();
            double lonVehiculo = posicion.getCoordenadas().getLon();

            double latCentroZona = zona.getCoordenadas().getLatitud();
            double lonCentroZona = zona.getCoordenadas().getLongitud();

            double distanciaAlCentroEnMetros = calcularDistanciaHaversine(latVehiculo, lonVehiculo, latCentroZona, lonCentroZona);

            // La comparación vuelve a ser correcta: metros vs metros.
            return distanciaAlCentroEnMetros <= zona.getRadioMetros();
        });
    }


    /**
     * Calcula la distancia en metros entre dos puntos geográficos usando la fórmula de Haversine.
     */
    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        //Calcula las Diferencias: Obtiene las diferencias de latitud y longitud entre los dos puntos.
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        //Convierte Grados a Radianes: Las funciones trigonométricas en Java (sin, cos) trabajan con radianes, no con grados.
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        //Trigonometría Esférica:
        double a = Math.pow(Math.sin(dLat / 2), 2) +  Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1Rad) * Math.cos(lat2Rad);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        //Finalmente, multiplica ese ángulo (en radianes) por el radio de la Tierra. El resultado es la distancia real sobre la superficie en la misma unidad que el radio
        return RADIO_TERRESTRE_METROS * c;
    }

    private void marcarIncidenteEnPruebaActiva(Integer idVehiculo) {
        Prueba prueba = pruebaRepository.findPruebaActivaByVehiculoId(idVehiculo);
        if (prueba != null && (prueba.getIncidente() == null || !prueba.getIncidente())) {
            prueba.setIncidente(true);
            pruebaRepository.save(prueba);
        }
    }

}