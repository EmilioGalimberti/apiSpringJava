### ¿Qué son las Fórmulas Geodésicas y por qué la de Haversine?

En pocas palabras, una fórmula geodésica calcula la distancia más corta entre dos puntos sobre una superficie curva, como la Tierra. No traza una línea recta a través de la Tierra, sino sobre su superficie (esto se conoce como "distancia del gran círculo", que es como se ven las rutas de los aviones).

La **fórmula de Haversine** es la más famosa y utilizada para este propósito porque ofrece un excelente equilibrio entre simplicidad y precisión para la gran mayoría de aplicaciones, incluyendo la tuya.

### ¿Por Qué No Sirve la Fórmula de Pitágoras (Euclídea)?

Como vimos, tu fórmula `Math.sqrt(dx² + dy²)` asume que el mundo es un plano cuadriculado (como una hoja de papel). Esto funciona para distancias muy cortas o en un mapa plano, pero falla en un globo porque, como ya sabes, la distancia real de un "grado de longitud" se encoge a medida que te acercas a los polos.

La fórmula de Haversine **resuelve este problema** porque usa trigonometría esférica, teniendo en cuenta la curvatura de la Tierra en sus cálculos.

### ¿Cómo Funciona Haversine? (La Idea, sin Matemáticas Complicadas)

No necesitas entender cada detalle matemático, pero conceptualmente, la fórmula hace lo siguiente:

1.  **Convierte Grados a Radianes:** Las funciones trigonométricas en Java (`sin`, `cos`) trabajan con radianes, no con grados. Así que lo primero es convertir todas tus coordenadas a esta unidad.
2.  **Calcula las Diferencias:** Obtiene las diferencias de latitud y longitud entre los dos puntos.
3.  **Trigonometría Esférica:** Aplica una serie de operaciones con senos y cosenos sobre estas diferencias. Esta es la "magia" de la fórmula: calcula el ángulo que se formaría entre los dos puntos si trazáramos líneas desde cada uno hasta el centro de la Tierra.
4.  **Calcula la Distancia Real:** Finalmente, multiplica ese ángulo (en radianes) por el radio de la Tierra. El resultado es la distancia real sobre la superficie en la misma unidad que el radio (generalmente kilómetros o metros).

### Implementación en Java y Cómo Usarla

La mejor forma de aplicarlo es crear un método de ayuda estático que puedas llamar desde cualquier parte. Puedes crear una nueva clase `GeoUtils.java` o simplemente añadirlo como un método privado `static` en tu `VehiculoService`.

Aquí está el código:

```java
// Puedes añadir este método dentro de tu clase VehiculoService

private static final double RADIO_TERRESTRE_METROS = 6371000; // Radio promedio de la Tierra en metros

/**
 * Calcula la distancia en metros entre dos puntos geográficos usando la fórmula de Haversine.
 */
private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double lat1Rad = Math.toRadians(lat1);
    double lat2Rad = Math.toRadians(lat2);

    double a = Math.pow(Math.sin(dLat / 2), 2) +
               Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1Rad) * Math.cos(lat2Rad);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return RADIO_TERRESTRE_METROS * c;
}
```

### Cómo Integrarlo en Tus Métodos (La Solución Final)

Ahora, reemplaza tus cálculos de distancia antiguos con llamadas a este nuevo método. ¡Fíjate cómo ahora comparas "metros con metros"!

```java
// En VehiculoService.java

private boolean estaPosicionFueraRadioAdmitido(PosicionDto posicion, RestriccionesDto restricciones) {
    double latAgencia = restricciones.getUbicacionAgencia().getLatitud();
    double lonAgencia = restricciones.getUbicacionAgencia().getLongitud();
    double latVehiculo = posicion.getCoordenadas().getLat();
    double lonVehiculo = posicion.getCoordenadas().getLon();

    // ¡USAMOS EL NUEVO MÉTODO!
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

        // ¡USAMOS EL NUEVO MÉTODO OTRA VEZ!
        double distanciaAlCentroEnMetros = calcularDistanciaHaversine(latVehiculo, lonVehiculo, latCentroZona, lonCentroZona);

        // La comparación vuelve a ser correcta: metros vs metros.
        return distanciaAlCentroEnMetros <= zona.getRadioMetros();
    });
}
```

Con este cambio, tu lógica para detectar si un vehículo se ha alejado demasiado o ha entrado en una zona peligrosa será **geográficamente correcta y precisa**.