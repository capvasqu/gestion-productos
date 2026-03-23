# Arquitectura — Gestión de Productos

Este documento describe el diseño interno del proyecto: flujo de una request HTTP, relaciones entre clases y decisiones de diseño encontradas en el código.

---

## Flujo completo de una request HTTP

El siguiente diagrama muestra el ciclo de vida de una petición desde el navegador hasta la base de datos y de vuelta, tomando como ejemplo `POST /api/productos`.

```mermaid
sequenceDiagram
    actor Usuario
    participant React as React (App.jsx)
    participant AxiosSvc as productoService.js<br/>(Axios)
    participant Nginx as Nginx<br/>(proxy /api)
    participant Ctrl as ProductoController
    participant Valid as Bean Validation<br/>(@Valid)
    participant EH as GlobalExceptionHandler
    participant Svc as ProductoService
    participant Repo as ProductoRepository
    participant DB as MySQL

    Usuario->>React: Completa formulario y hace clic "Crear"
    React->>AxiosSvc: crear(payload)
    AxiosSvc->>Nginx: POST /api/productos {body: ProductoDTO}
    Nginx->>Ctrl: proxy_pass → POST /api/productos

    Ctrl->>Valid: @Valid ProductoDTO
    alt Validación fallida
        Valid-->>EH: MethodArgumentNotValidException
        EH-->>Nginx: 400 {campos: {campo: "mensaje"}}
        Nginx-->>AxiosSvc: 400
        AxiosSvc-->>React: error (catch)
        React-->>Usuario: Muestra error de validación
    else Validación exitosa
        Valid-->>Ctrl: DTO válido
        Ctrl->>Svc: crear(dto)
        Svc->>Repo: save(producto)
        Repo->>DB: INSERT INTO productos (...)
        DB-->>Repo: producto con id generado
        Repo-->>Svc: Producto persistido
        Svc-->>Ctrl: Producto
        Ctrl-->>Nginx: 201 Created {body: Producto}
        Nginx-->>AxiosSvc: 201
        AxiosSvc-->>React: response.data
        React-->>Usuario: Lista actualizada
    end
```

---

## Flujo de excepción — Producto no encontrado

```mermaid
sequenceDiagram
    participant Ctrl as ProductoController
    participant Svc as ProductoService
    participant Repo as ProductoRepository
    participant EH as GlobalExceptionHandler

    Ctrl->>Svc: obtenerPorId(99)
    Svc->>Repo: findById(99)
    Repo-->>Svc: Optional.empty()
    Svc->>EH: throw ProductoNotFoundException("Producto no encontrado con id: 99")
    EH-->>Ctrl: ResponseEntity 404
    Note over EH: {timestamp, status: 404,<br/>error: "No encontrado",<br/>message: "Producto no encontrado con id: 99"}
```

---

## Relaciones entre clases

```mermaid
classDiagram
    class Producto {
        +Long id
        +String nombre
        +String descripcion
        +BigDecimal precio
        +Integer stock
        +String categoria
        +Boolean activo
        +LocalDateTime fechaCreacion
        +LocalDateTime fechaActualizacion
        #onCreate()
        #onUpdate()
    }

    class ProductoDTO {
        +String nombre
        +String descripcion
        +BigDecimal precio
        +Integer stock
        +String categoria
    }

    class ProductoRepository {
        <<interface>>
        +findByCategoriaIgnoreCase(String) List~Producto~
        +findByActivoTrueAndStockGreaterThan(Integer) List~Producto~
        +findByNombreContainingIgnoreCase(String) List~Producto~
        +findByPrecioEntre(BigDecimal, BigDecimal) List~Producto~
        +contarPorCategoria(String) Long
    }

    class ProductoService {
        -ProductoRepository productoRepository
        +obtenerTodos() List~Producto~
        +obtenerPorId(Long) Producto
        +crear(ProductoDTO) Producto
        +actualizar(Long, ProductoDTO) Producto
        +eliminar(Long) void
        +aplicarDescuento(Long, double) Producto
        +buscarPorCategoria(String) List~Producto~
        +buscarDisponibles() List~Producto~
        +buscarPorRangoPrecio(BigDecimal, BigDecimal) List~Producto~
    }

    class ProductoController {
        -ProductoService productoService
        +obtenerTodos() ResponseEntity
        +obtenerPorId(Long) ResponseEntity
        +crear(ProductoDTO) ResponseEntity
        +actualizar(Long, ProductoDTO) ResponseEntity
        +eliminar(Long) ResponseEntity
        +porCategoria(String) ResponseEntity
        +disponibles() ResponseEntity
        +porRangoPrecio(BigDecimal, BigDecimal) ResponseEntity
        +aplicarDescuento(Long, double) ResponseEntity
    }

    class ProductoNotFoundException {
        +ProductoNotFoundException(String message)
    }

    class GlobalExceptionHandler {
        +handleNotFound(ProductoNotFoundException) ResponseEntity
        +handleValidation(MethodArgumentNotValidException) ResponseEntity
        +handleGeneral(Exception) ResponseEntity
    }

    ProductoController --> ProductoService : usa
    ProductoController ..> ProductoDTO : recibe (@Valid)
    ProductoController ..> ProductoNotFoundException : interceptada por
    ProductoService --> ProductoRepository : usa
    ProductoService ..> ProductoDTO : lee datos de
    ProductoService ..> Producto : crea/devuelve
    ProductoService ..> ProductoNotFoundException : lanza
    ProductoRepository ..> Producto : persiste/consulta
    GlobalExceptionHandler ..> ProductoNotFoundException : maneja
    Producto --|> Object : extends (JPA @Entity)
    ProductoRepository --|> JpaRepository : extends
```

---

## Estructura de paquetes y responsabilidades

```mermaid
graph LR
    subgraph com.demo.productos
        direction TB
        A["GestionProductosApplication - Punto de entrada @SpringBootApplication"]

        subgraph controller
            B["ProductoController @RestController /api/productos"]
        end

        subgraph service
            C["ProductoService @Service Lógica de negocio"]
        end

        subgraph repository
            D["ProductoRepository @Repository JpaRepository + queries"]
        end

        subgraph model
            E["Producto @Entity Tabla: productos"]
        end

        subgraph dto
            F["ProductoDTO Contrato de entrada @Valid"]
        end

        subgraph exception
            G["ProductoNotFoundException RuntimeException"]
            H["GlobalExceptionHandler @RestControllerAdvice"]
        end
    end

    A --> B
    B --> C
    B --> F
    C --> D
    C --> G
    D --> E
    H --> G
```

---

## Decisiones de diseño

### 1. Separación DTO / Entidad
El controlador nunca expone la entidad `Producto` directamente en los cuerpos de entrada. En cambio, recibe un `ProductoDTO` validado con `@Valid`. La entidad sí se devuelve como respuesta (sin un DTO de salida separado), lo que simplifica el código pero mezcla levemente las capas de persistencia y transporte.

**Implicación**: un cambio en el modelo (agregar un campo interno) podría filtrar datos hacia el cliente si no se agrega un DTO de respuesta.

---

### 2. Manejo de errores centralizado
`GlobalExceptionHandler` con `@RestControllerAdvice` centraliza toda la lógica de formateo de errores. Todos los errores devuelven un objeto JSON consistente con `timestamp`, `status`, `error` y `message`/`campos`, lo que facilita el consumo desde el frontend.

---

### 3. Perfil de tests con H2
`application-test.properties` configura H2 en memoria para los tests, sin necesidad de MySQL. El esquema se crea y destruye automáticamente (`ddl-auto=create-drop`), garantizando aislamiento entre suites.

---

### 4. CORS explícito en el controlador
`@CrossOrigin(origins = "http://localhost:3000")` está definido directamente en `ProductoController`. En un proyecto real con múltiples controladores, esta configuración debería centralizarse en una clase `WebMvcConfigurer`.

---

### 5. Multi-stage Docker builds
Tanto el backend como el frontend usan multi-stage builds para minimizar el tamaño de las imágenes finales. El backend compila con Maven y copia solo el JAR a una imagen JRE Alpine. El frontend compila con Node y sirve los estáticos desde Nginx Alpine.

---

### 6. Proxy Nginx como punto de entrada único
En producción Docker, Nginx actúa como reverse proxy: sirve el frontend en `/` y redirige `/api` al backend. El frontend usa rutas relativas (`/api/productos`) por lo que funciona tanto en desarrollo (proxy de `react-scripts`) como en producción (proxy de Nginx) sin cambiar código.

---

### 7. Bugs intencionales como ejercicio pedagógico
El servicio y el frontend contienen 14 bugs documentados (`// BUG #N`) que cubren anti-patrones reales:

| Categoría | Bugs |
|-----------|------|
| Inyección de dependencias | #1 — field injection en lugar de constructor |
| Validación de parámetros | #2, #5, #7, #8 |
| Lógica de negocio | #3, #4, #6 |
| HTTP incorrecto en frontend | #9 |
| UX / manejo de errores | #10, #11, #12, #13, #14 |

Los `.cursorrules` del proyecto definen las convenciones correctas para contrastar contra los bugs.

---

### 8. Convención de nombres en español
Todos los métodos de negocio, variables y entidades usan nombres en español (`obtenerTodos`, `buscarPorCategoria`, `fechaCreacion`). Los nombres de anotaciones, interfaces de Spring y métodos de test siguen el inglés técnico estándar de Java.
