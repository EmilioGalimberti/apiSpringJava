# Backend de Aplicaciones para Agencia de Vehículos Usados

Este es el backend para una agencia de venta de vehículos usados que permite la gestión de pruebas de manejo de sus productos. El sistema integra la geolocalización de vehículos para monitorear pruebas, detectar incidentes y generar notificaciones.

---

## 🚀 Estructura del Proyecto

El proyecto está organizado en las siguientes capas principales para una clara separación de responsabilidades:

* **`models/`**: Contiene las entidades JPA que mapean la base de datos relacional.
* **`repositories/`**: Interfaces de Spring Data JPA para la interacción con la base de datos.
* **`dtos/`**: Objetos de transferencia de datos para la comunicación entre capas y la exposición de la API REST.
* **`services/`**: Implementa la lógica de negocio de la aplicación.
* **`controllers/`**: Expone la funcionalidad como una API REST.

---

## 📦 Capa de Modelos (`models/`)

En esta carpeta se definen las entidades Java que representan las tablas de la base de datos. Utilizan anotaciones JPA para el mapeo objeto-relacional y Lombok para reducir el código repetitivo (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`).

**Anotaciones importantes en las entidades:**

* `@Entity`: Marca una clase como una entidad JPA.
* `@Table(name = "...")`: Especifica el nombre de la tabla en la base de datos.
* `@Id`: Marca el campo como la clave primaria.
* `@GeneratedValue(strategy = GenerationType.IDENTITY)`: Configura la generación automática de ID (autoincremental).
* `@Column(name = "...")`: Mapea un campo a una columna específica de la tabla.
* `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany`: Definen las relaciones entre entidades.
    * `mappedBy`: En el lado "no-propietario" de una relación bidireccional (`@OneToMany`), indica el campo de la entidad "propietaria" que gestiona la relación. Esto evita la creación de tablas de unión innecesarias.
* `@EqualsAndHashCode.Exclude` y `@ToString.Exclude`: Anotaciones de Lombok que son cruciales en entidades con relaciones bidireccionales. **Excluyen** las colecciones relacionadas de la generación automática de los métodos `equals()`, `hashCode()` y `toString()`. Esto es fundamental para:
    * **Evitar bucles infinitos:** Al serializar objetos a JSON o al imprimir (`toString()`), una relación bidireccional podría causar un `StackOverflowError`.
    * **Mejorar el rendimiento:** Evita cargar colecciones grandes innecesariamente solo para comparaciones o representaciones.
    * **Identidad basada en ID:** Asegura que la identidad de una entidad para `equals()` y `hashCode()` se base principalmente en su clave primaria.

---

## 🗄️ Capa de Repositorios (`repositories/`)

Los repositorios son interfaces clave en Spring Data JPA que te permiten interactuar con tu base de datos utilizando tus objetos Java (entidades) sin tener que escribir SQL manual ni gestionar las complejidades de JDBC/JPA directamente. Simplifican enormemente la capa de persistencia, permitiéndote centrarte en la lógica de negocio de tu aplicación.

**¿Por qué son esenciales los Repositorios?**

**Sin Spring Data JPA (y sus Repositorios):**
Si no tuvieras Spring Data JPA, para interactuar con la base de datos tendrías que:
* Abrir y cerrar conexiones a la base de datos.
* Escribir sentencias SQL manualmente (ej. `INSERT INTO Empleados (NOMBRE, APELLIDO) VALUES (?, ?);`).
* Preparar las sentencias con los parámetros.
* Ejecutar las consultas.
* Mapear manualmente los resultados de las consultas (`ResultSet`) a tus objetos Java.
* Manejar las transacciones (commit/rollback).
  Esto es extremadamente tedioso, repetitivo y propenso a errores, y tendrías que repetir este proceso para cada entidad y cada tipo de operación.

**Con Spring Data JPA (y sus Repositorios):**
Aquí es donde los repositorios se vuelven súper poderosos. Cuando extiendes de `JpaRepository` (como `EmpleadoRepository extends JpaRepository<Empleado, Long>`), Spring Data JPA hace todo el trabajo pesado por ti.

**Funcionalidades Clave que te Proporcionan los Repositorios:**

* **Operaciones CRUD Estándar:**
    * `save(entity)`: Inserta una nueva entidad en la base de datos o actualiza una existente si ya tiene un ID.
    * `findById(id)`: Busca una entidad por su ID. Devuelve un `Optional` para manejar el caso de que la entidad no exista.
    * `findAll()`: Recupera todas las entidades de ese tipo.
    * `delete(entity)` o `deleteById(id)`: Elimina una entidad de la base de datos.
    * `count()`: Devuelve el número total de entidades.
    * `existsById(id)`: Verifica si una entidad con un ID dado existe.

* **Consultas Derivadas de Métodos (Query Methods):**
  Puedes definir tus propios métodos en la interfaz del repositorio, y Spring Data JPA generará la consulta SQL automáticamente basándose en el nombre del método.
    * **Ejemplo:** En `InteresadoRepository`, si defines `Optional<Interesado> findByDocumento(String documento);`, Spring Data JPA sabe que debe generar una consulta como `SELECT * FROM Interesados WHERE DOCUMENTO = ?` y mapear el resultado a un objeto `Interesado`.
    * **Otros ejemplos:** `findByNombreAndApellido(String nombre, String apellido)`, `findByFechaVencimientoLicenciaBefore(LocalDate date)`, `findByRestringidoTrue()`.

* **Consultas Personalizadas con `@Query`:**
  Si la consulta que necesitas es demasiado compleja para ser generada por el nombre del método, puedes escribirla tú mismo usando la anotación `@Query` directamente en la interfaz del repositorio (puedes usar JPQL/HQL o SQL nativo).

* **Paginación y Ordenación:**
  Los repositorios también te permiten obtener datos de forma paginada (`findAll(Pageable pageable)`) y ordenada (`findAll(Sort sort)`), lo cual es crucial para aplicaciones grandes donde no quieres cargar todos los datos a la vez.

---

## 📊 DTOs (Data Transfer Objects) (`dtos/`)

Los DTOs son objetos planos (POJOs - Plain Old Java Objects) que representan los datos que se van a **transferir** a través de la red (entre el backend y el frontend, o entre microservicios). Su creación es un paso importante y necesario antes de construir los controladores REST, e incluso influyen en el diseño de los servicios.

### ¿Por qué los DTOs son importantes?

1.  **Separación de Conceptos:**
    * **Entidades JPA:** Son el mapeo directo de tu base de datos. Contienen anotaciones JPA, relaciones bidireccionales, getters/setters, etc. Su objetivo principal es la persistencia y la interacción con la base de datos.
    * **DTOs:** Son objetos diseñados para la transferencia de datos. No tienen anotaciones JPA ni relaciones bidireccionales, y su estructura puede ser diferente a la de la entidad, adaptándose mejor a las necesidades de la API.

2.  **Control de la Exposición de Datos:**
    No siempre querrás exponer todos los campos de una entidad JPA a través de tu API REST (ej. campos internos, hashes de contraseñas). Los DTOs te permiten seleccionar exactamente qué datos se envían (`Response DTOs`) o qué datos se reciben (`Request DTOs`), controlando así la superficie de tu API.

3.  **Prevención de Bucles Infinitos en JSON/XML:**
    Cuando Spring serializa entidades JPA con relaciones bidireccionales directamente a JSON, puede caer en un bucle infinito (Ej: `Empleado` tiene una lista de `Pruebas`, y cada `Prueba` tiene una referencia a su `Empleado`). Los DTOs rompen este ciclo al solo incluir las referencias necesarias (ej. solo el ID de la entidad relacionada) o una representación simplificada de la relación.

4.  **Optimización del Payload:**
    Puedes crear DTOs con menos campos para operaciones específicas, reduciendo el tamaño de la respuesta HTTP y mejorando el rendimiento de la red. Por ejemplo, un DTO para una lista de ítems podría tener solo el ID y el nombre, mientras que un DTO de detalle tendría todos los campos.

5.  **Manejo de Entrada de Datos:**
    Puedes tener DTOs específicos para la entrada de datos (`Request DTOs`) que pueden incluir campos de validación, combinar datos de varias entidades para una operación de creación/actualización, o reflejar exactamente el cuerpo de la solicitud HTTP esperada.

---

## ⚙️ Configuración del Proyecto

### `pom.xml`

El archivo `pom.xml` define las dependencias y la configuración de construcción de tu proyecto Maven.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>

    <groupId>org.example</groupId>
    <artifactId>tpJavaSpringBackEnd</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!--● Spring Data JPA: Para Java Persistence API.-->

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
            <version>3.4.4</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>3.4.4</version>
        </dependency>
        <!-- SQLite Dependency -->
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.42.0.0</version>
        </dependency>
        <!-- Hibernate Dependency-->
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-community-dialects</artifactId>
            <version>6.2.7.Final</version>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.2.0</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.30</version>
            <scope>provided</scope>
        </dependency>


        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>

```

application.properties
Este archivo (ubicado en src/main/resources/) se utiliza para la configuración de la aplicación Spring Boot, incluyendo la conexión a la base de datos.

```properties
# Configuración de la base de datos SQLite
spring.datasource.url=jdbc:sqlite:agencia.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect

# Configuración de Hibernate (para Spring Data JPA)
# ddl-auto:
#   - none: No hace nada en el esquema de la BD.
#   - validate: Valida el esquema, pero no lo modifica.
#   - update: Actualiza el esquema de la BD si es necesario (añade tablas/columnas, NO las borra).
#   - create: Crea el esquema de la BD al iniciar (borra las tablas existentes).
#   - create-drop: Crea el esquema al iniciar y lo borra al cerrar la aplicación.
spring.jpa.hibernate.ddl-auto=update

# Mostrar las sentencias SQL generadas por Hibernate en la consola
spring.jpa.show-sql=true
# Formatear el SQL para que sea legible
spring.jpa.properties.hibernate.format_sql=true

# Configuración del servidor (si no se especifica, por defecto es 8080)
server.port=8080

# Otras configuraciones que podrías necesitar:
# spring.main.web-application-type=servlet # O REACTIVE
```

# DTO vs entidad JPA
* Las entidades son la representación de tu dominio de datos y la interfaz principal entre tu aplicación y la base de datos.
*  Los DTOs son la representación de tus mensajes de API y la interfaz principal entre tu backend y sus consumidores.

¿Por qué no usar solo uno?
Si solo usas Entidades para la API:

Expones la estructura interna de tu base de datos, lo que puede ser un riesgo de seguridad o hacer que tu API sea rígida a los cambios de la BD.
Corres un alto riesgo de bucles infinitos de serialización con relaciones bidireccionales, causando StackOverflowError.
No puedes controlar qué datos se envían/reciben de forma granular.
El "payload" de tu API puede ser más grande de lo necesario.
Si solo usas DTOs y no Entidades JPA (en el contexto de una BD relacional):

Perderías todos los beneficios de JPA/Hibernate para el mapeo objeto-relacional y la gestión automática de la persistencia. Tendrías que mapear manualmente entre DTOs y la base de datos, escribiendo mucho más código JDBC. Esto anularía el propósito de usar JPA.
Conclusión
Entidades y DTOs cumplen roles distintos y complementarios:

Entidades: Viven en la capa de persistencia y son el modelo de tu base de datos.
DTOs: Viven en la capa de la API (o a veces en la de servicio) y son el modelo de los datos que viajan por la red.
La capa de Servicios actúa como el puente, mapeando datos entre las Entidades (cuando interactúa con los repositorios) y los DTOs (cuando prepara datos para el controlador o recibe datos del controlador). Este patrón es una práctica recomendada que conduce a un código más limpio, modular, mantenible y robusto.