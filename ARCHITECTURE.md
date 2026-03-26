# Architecture — Product Management

This document describes the internal design of the project: HTTP request flow, class relationships, and design decisions found in the code.

---

## Full HTTP request flow

The following diagram shows the lifecycle of a request from the browser to the database and back, using `POST /api/productos` as an example.

```mermaid
sequenceDiagram
    actor User
    participant React as React (App.jsx)
    participant AxiosSvc as productoService.js<br/>(Axios)
    participant Nginx as Nginx<br/>(proxy /api)
    participant Ctrl as ProductoController
    participant Valid as Bean Validation<br/>(@Valid)
    participant EH as GlobalExceptionHandler
    participant Svc as ProductoService
    participant Repo as ProductoRepository
    participant DB as MySQL

    User->>React: Fills in form and clicks "Create"
    React->>AxiosSvc: crear(payload)
    AxiosSvc->>Nginx: POST /api/productos {body: ProductoDTO}
    Nginx->>Ctrl: proxy_pass → POST /api/productos

    Ctrl->>Valid: @Valid ProductoDTO
    alt Validation failed
        Valid-->>EH: MethodArgumentNotValidException
        EH-->>Nginx: 400 {fields: {field: "message"}}
        Nginx-->>AxiosSvc: 400
        AxiosSvc-->>React: error (catch)
        React-->>User: Shows validation error
    else Validation succeeded
        Valid-->>Ctrl: Valid DTO
        Ctrl->>Svc: crear(dto)
        Svc->>Repo: save(producto)
        Repo->>DB: INSERT INTO productos (...)
        DB-->>Repo: producto with generated id
        Repo-->>Svc: Persisted Producto
        Svc-->>Ctrl: Producto
        Ctrl-->>Nginx: 201 Created {body: Producto}
        Nginx-->>AxiosSvc: 201
        AxiosSvc-->>React: response.data
        React-->>User: Updated list
    end
```

---

## Exception flow — Product not found

```mermaid
sequenceDiagram
    participant Ctrl as ProductoController
    participant Svc as ProductoService
    participant Repo as ProductoRepository
    participant EH as GlobalExceptionHandler

    Ctrl->>Svc: obtenerPorId(99)
    Svc->>Repo: findById(99)
    Repo-->>Svc: Optional.empty()
    Svc->>EH: throw ProductoNotFoundException("Product not found with id: 99")
    EH-->>Ctrl: ResponseEntity 404
    Note over EH: {timestamp, status: 404,<br/>error: "Not Found",<br/>message: "Product not found with id: 99"}
```

---

## Class relationships

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

    ProductoController --> ProductoService : uses
    ProductoController ..> ProductoDTO : receives (@Valid)
    ProductoController ..> ProductoNotFoundException : intercepted by
    ProductoService --> ProductoRepository : uses
    ProductoService ..> ProductoDTO : reads data from
    ProductoService ..> Producto : creates/returns
    ProductoService ..> ProductoNotFoundException : throws
    ProductoRepository ..> Producto : persists/queries
    GlobalExceptionHandler ..> ProductoNotFoundException : handles
    Producto --|> Object : extends (JPA @Entity)
    ProductoRepository --|> JpaRepository : extends
```

---

## Package structure and responsibilities

```mermaid
graph LR
    subgraph com.demo.productos
        direction TB
        A["GestionProductosApplication - Entry point @SpringBootApplication"]

        subgraph controller
            B["ProductoController @RestController /api/productos"]
        end

        subgraph service
            C["ProductoService @Service Business logic"]
        end

        subgraph repository
            D["ProductoRepository @Repository JpaRepository + queries"]
        end

        subgraph model
            E["Producto @Entity Table: productos"]
        end

        subgraph dto
            F["ProductoDTO Input contract @Valid"]
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

## Design decisions

### 1. DTO / Entity separation
The controller never exposes the `Producto` entity directly in request bodies. Instead, it receives a `ProductoDTO` validated with `@Valid`. The entity is returned as the response (without a separate output DTO), which simplifies the code but slightly blurs the persistence and transport layers.

**Implication**: a model change (adding an internal field) could leak data to the client if a response DTO is not added.

---

### 2. Centralized error handling
`GlobalExceptionHandler` with `@RestControllerAdvice` centralizes all error formatting logic. All errors return a consistent JSON object with `timestamp`, `status`, `error` and `message`/`fields`, making frontend consumption straightforward.

---

### 3. H2 test profile
`application-test.properties` configures an in-memory H2 database for tests, with no MySQL dependency. The schema is created and destroyed automatically (`ddl-auto=create-drop`), guaranteeing isolation between test suites.

---

### 4. Explicit CORS on the controller
`@CrossOrigin(origins = "http://localhost:3000")` is defined directly on `ProductoController`. In a real project with multiple controllers, this configuration should be centralized in a `WebMvcConfigurer` class.

---

### 5. Multi-stage Docker builds
Both the backend and frontend use multi-stage builds to minimize final image sizes. The backend compiles with Maven and copies only the JAR to a JRE Alpine image. The frontend compiles with Node and serves static files from Nginx Alpine.

---

### 6. Nginx proxy as single entry point
In the Docker production setup, Nginx acts as a reverse proxy: it serves the frontend at `/` and forwards `/api` to the backend. The frontend uses relative paths (`/api/productos`) so it works in both development (proxy via `react-scripts`) and production (proxy via Nginx) without any code changes.

---

### 7. Intentional bugs as a teaching exercise
The service and frontend contain 14 documented bugs (`// BUG #N`) covering real anti-patterns:

| Category | Bugs |
|----------|------|
| Dependency injection | #1 — field injection instead of constructor |
| Parameter validation | #2, #5, #7, #8 |
| Business logic | #3, #4, #6 |
| Incorrect HTTP in frontend | #9 |
| UX / error handling | #10, #11, #12, #13, #14 |

The project's `.cursorrules` define the correct conventions to contrast against the bugs.

---

### 8. Spanish naming convention
All business methods, variables, and entities use Spanish names (`obtenerTodos`, `buscarPorCategoria`, `fechaCreacion`). Annotation names, Spring interfaces, and test methods follow standard Java technical English.
