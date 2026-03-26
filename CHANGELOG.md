# Changelog

All notable changes to this project are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and the project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added

#### Backend — Spring Boot 3.2.3 / Java 17

- **`GestionProductosApplication`** — entry point class with `@SpringBootApplication`, starts the Spring Boot context on port 8080 with context path `/api`.

- **`Producto` (model)** — JPA entity mapped to the `productos` table with fields `id`, `nombre`, `descripcion`, `precio` (BigDecimal), `stock`, `categoria`, `activo`, `fechaCreacion` and `fechaActualizacion`. Includes `@PrePersist` and `@PreUpdate` callbacks for automatic timestamp management and default value for `activo`.

- **`ProductoDTO` (dto)** — transfer object with Jakarta Bean Validation constraints (`@NotBlank`, `@NotNull`, `@Size`, `@DecimalMin`, `@Min`) used as the input contract for create and update operations.

- **`ProductoRepository` (repository)** — interface extending `JpaRepository<Producto, Long>` with five additional methods: search by category, by availability, by name (partial match), by price range (JPQL) and count by category (JPQL).

- **`ProductoService` (service)** — service with nine business methods: `obtenerTodos`, `obtenerPorId`, `crear`, `actualizar`, `eliminar`, `aplicarDescuento`, `buscarPorCategoria`, `buscarDisponibles` and `buscarPorRangoPrecio`. Contains 8 intentional bugs for practice exercises.

- **`ProductoController` (controller)** — REST controller `@RequestMapping("/productos")` with nine endpoints covering CRUD operations and business queries. Enables CORS for `http://localhost:3000`.

- **`ProductoNotFoundException` (exception)** — domain exception `RuntimeException` thrown when a product is not found by ID.

- **`GlobalExceptionHandler` (exception)** — `@RestControllerAdvice` that centralizes handling of `ProductoNotFoundException` (404), `MethodArgumentNotValidException` (400 with field→error map) and generic `Exception` (500). All errors include `timestamp`, `status`, `error` and message.

- **`application.properties`** — configuration for MySQL 8.0 (`jdbc:mysql://localhost:3306/gestion_productos`), Hibernate in `update` mode, SQL logging enabled, server on port 8080 with context path `/api`.

- **`application-test.properties`** — test profile with in-memory H2 (`ddl-auto=create-drop`) for execution without MySQL dependency.

- **`ProductoServiceTest`** — unit test suite with JUnit 5, Mockito and AssertJ. Covers: `obtenerPorId` (existing and non-existing), `crear`, `aplicarDescuento` and `buscarDisponibles`. Includes 4 tests marked as TODO to complete in exercises.

- **`ProductoControllerTest`** — integration test suite with MockMvc. Covers: `GET /`, `GET /{id}` (existing and non-existing), `POST /` with validation. Includes 3 tests marked as TODO to complete in exercises.

- **`Dockerfile` (backend)** — multi-stage build: Maven 3.9/Java 17 stage for compilation, JRE 17 Alpine stage for runtime. Runs as non-root user `spring`, exposes port 8080.

- **`pom.xml`** — dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `mysql-connector-java`, `lombok`, `h2` (scope test), `spring-boot-starter-test`.

---

#### Frontend — React 18 / Axios / Nginx

- **`productoService.js` (services)** — Axios module with instance configured at `/api/productos`. Exports: `obtenerTodos`, `obtenerPorId`, `crear`, `actualizar`, `eliminar`, `buscarPorCategoria`, `obtenerDisponibles`, `aplicarDescuento`. Contains 1 intentional bug (`eliminar` uses GET instead of DELETE).

- **`App.jsx`** — root component with state `productos`, `productoEditar`, `error` and `cargando`. Coordinates `ProductoLista` and `ProductoFormulario` in a two-column layout. Contains 2 intentional bugs in error handling.

- **`ProductoLista.jsx` (components)** — table component that receives `productos`, `onEditar` and `onEliminar`. Displays columns: ID, Name, Category, Price, Stock, Active and Actions. Contains 2 intentional bugs (empty list without message and unformatted price).

- **`ProductoFormulario.jsx` (components)** — controlled form with client-side validation for name, price, stock and category. Supports create mode and edit mode (populated from `productoEditar`). Contains 2 intentional bugs in validation error handling.

- **`Dockerfile` (frontend)** — multi-stage build: Node 18 Alpine stage for `npm run build`, Nginx Alpine stage for serving static files, exposes port 3000.

- **`nginx.conf`** — SPA routing (`try_files $uri /index.html`) with proxy `/api → http://app:8080` for the backend.

- **`package.json`** — dependencies: `react@^18.2.0`, `react-dom@^18.2.0`, `react-scripts@5.0.1`, `axios@^1.6.7`. Proxy configured to `http://localhost:8080` for local development.

---

#### Infrastructure

- **`docker-compose.yml`** — orchestrates three services:
  - `db`: MySQL 8.0 with healthcheck, environment variables `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`.
  - `app`: Spring Boot backend, depends on `db` (condition `service_healthy`), environment variables for datasource.
  - `frontend`: React/Nginx, depends on `app`, exposes port 3000.

---

#### Documentation

- **`README.md`** — project description, Mermaid architecture diagram, layer descriptions, full REST endpoint table with request/response, local and Docker installation instructions, practice exercises with Cursor.

- **`ARCHITECTURE.md`** — Mermaid diagrams of the full HTTP flow (sequence), exception flow, class relationships (UML class diagram) and package structure. Documents 8 design decisions found in the code.

- **`CHANGELOG.md`** — this file.

- **`.cursorrules`** — project conventions for backend (constructor injection, BigDecimal for money, soft delete, parameter validation) and frontend (functional components, loading/error/data state handling, currency formatting).

- **`REPORTE-BUGS.md`** — automatically generated report with the 14 intentional bugs in the project, their location, description and proposed fix.

---

[Unreleased]: https://github.com/tu-usuario/gestion-productos/compare/HEAD
