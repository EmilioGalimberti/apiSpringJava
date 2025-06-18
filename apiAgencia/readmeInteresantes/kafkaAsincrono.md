Hacer la Llamada a Kafka Asíncrona
Incluso si Kafka estuviera funcionando, enviar un mensaje puede ser una operación de red lenta. No deberías bloquear tu flujo principal por ello. La solución es tratar el envío a Kafka como lo que es: una operación de "dispara y olvida" (fire-and-forget) que no debería afectar la respuesta al cliente.

Puedes modificar tu VehiculoService para que el envío a Kafka se ejecute en un hilo separado.

Refactor del VehiculoService:

Java

// En VehiculoService.java

.flatMap(restricciones -> {
// ... (lógica para guardar en la BD y construir la respuesta) ...

    if (estaPosicionFueraRadioAdmitido(posicionRespuesta, restricciones)) {
        posicionRespuesta.setMensaje("...");
        
        // ¡CAMBIO IMPORTANTE! Hacemos que el envío a Kafka no bloquee.
        Mono.fromRunnable(() -> kafkaProducer.enviarMensajeRadioExcedido(posicionRespuesta))
            .subscribeOn(Schedulers.boundedElastic()) // Ejecuta esto en otro hilo
            .subscribe(); // Inicia la operación "dispara y olvida"

        return Mono.just(posicionRespuesta);
    }

    if (estaEnZonaRestringida(posicionRespuesta, restricciones)) {
        posicionRespuesta.setMensaje("...");
        
        // ¡MISMO CAMBIO AQUÍ!
        Mono.fromRunnable(() -> kafkaProducer.enviarMensajeZonaPeligrosa(posicionRespuesta))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();

        return Mono.just(posicionRespuesta);
    }

    // ...
    return Mono.just(posicionRespuesta);
});
Mono.fromRunnable(...): Envuelve tu llamada a Kafka en un Mono que no emite ningún resultado (solo la ejecuta).
.subscribeOn(Schedulers.boundedElastic()): Le dice a Reactor que ejecute esta tarea en un hilo separado, optimizado para operaciones de red como esta.
.subscribe(): "Enciende" el Mono para que se ejecute, pero como es en otro hilo, tu flujo principal en el .flatMap continúa inmediatamente sin esperar.