# Changelog

Todos los cambios notables de este proyecto se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es/1.0.0/)
y el proyecto respeta [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added

#### Backend — Spring Boot 3.2.3 / Java 17

- **`GestionProductosApplication`** — clase de entrada con `@SpringBootApplication`, arranca el contexto de Spring Boot en el puerto 8080 con context path `/api`.

- **`Producto` (model)** — entidad JPA mapeada a la tabla `productos` con los campos `id`, `nombre`, `descripcion`, `precio` (BigDecimal), `stock`, `categoria`, `activo`, `fechaCreacion` y `fechaActualizacion`. Incluye callbacks `@PrePersist` y `@PreUpdate` para gestión automática de timestamps y valor por defecto de `activo`.

- **`ProductoDTO` (dto)** — objeto de transferencia con validaciones Jakarta Bean Validation (`@NotBlank`, `@NotNull`, `@Size`, `@DecimalMin`, `@Min`) usado como contrato de entrada en las operaciones de creación y actualización.

- **`ProductoRepository` (repository)** — interfaz que extiende `JpaRepository<Producto, Long>` con cinco métodos adicionales: búsqueda por categoría, por disponibilidad, por nombre (búsqueda parcial), por rango de precio (JPQL) y conteo por categoría (JPQL).

- **`ProductoService` (service)** — servicio con nueve métodos de negocio: `obtenerTodos`, `obtenerPorId`, `crear`, `actualizar`, `eliminar`, `aplicarDescuento`, `buscarPorCategoria`, `buscarDisponibles` y `buscarPorRangoPrecio`. Contiene 8 bugs intencionales para los ejercicios de práctica.

- **`ProductoController` (controller)** — controlador REST `@RequestMapping("/productos")` con nueve endpoints que cubren las operaciones CRUD y las consultas de negocio. Habilita CORS para `http://localhost:3000`.

- **`ProductoNotFoundException` (exception)** — excepción de dominio `RuntimeException` lanzada cuando no se encuentra un producto por ID.

- **`GlobalExceptionHandler` (exception)** — `@RestControllerAdvice` que centraliza el manejo de `ProductoNotFoundException` (404), `MethodArgumentNotValidException` (400 con mapa campo→error) y `Exception` genérica (500). Todos los errores incluyen `timestamp`, `status`, `error` y mensaje.

- **`application.properties`** — configuración para MySQL 8.0 (`jdbc:mysql://localhost:3306/gestion_productos`), Hibernate en modo `update`, SQL logging habilitado, servidor en puerto 8080 con context path `/api`.

- **`application-test.properties`** — perfil de tests con H2 en memoria (`ddl-auto=create-drop`) para ejecución sin dependencia de MySQL.

- **`ProductoServiceTest`** — suite de tests unitarios con JUnit 5, Mockito y AssertJ. Cubre: `obtenerPorId` (existente y no existente), `crear`, `aplicarDescuento` y `buscarDisponibles`. Incluye 4 tests marcados como TODO para completar en ejercicios.

- **`ProductoControllerTest`** — suite de tests de integración con MockMvc. Cubre: `GET /`, `GET /{id}` (existente y no existente), `POST /` con validación. Incluye 3 tests marcados como TODO para completar en ejercicios.

- **`Dockerfile` (backend)** — multi-stage build: etapa Maven 3.9/Java 17 para compilar, etapa JRE 17 Alpine para runtime. Ejecuta como usuario no-root `spring`, expone puerto 8080.

- **`pom.xml`** — dependencias: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `mysql-connector-java`, `lombok`, `h2` (scope test), `spring-boot-starter-test`.

---

#### Frontend — React 18 / Axios / Nginx

- **`productoService.js` (services)** — módulo Axios con instancia configurada en `/api/productos`. Exporta: `obtenerTodos`, `obtenerPorId`, `crear`, `actualizar`, `eliminar`, `buscarPorCategoria`, `obtenerDisponibles`, `aplicarDescuento`. Contiene 1 bug intencional (`eliminar` usa GET en lugar de DELETE).

- **`App.jsx`** — componente raíz con estado `productos`, `productoEditar`, `error` y `cargando`. Coordina `ProductoLista` y `ProductoFormulario` en layout de dos columnas. Contiene 2 bugs intencionales en el manejo de errores.

- **`ProductoLista.jsx` (components)** — componente de tabla que recibe `productos`, `onEditar` y `onEliminar`. Muestra columnas: ID, Nombre, Categoría, Precio, Stock, Activo y Acciones. Contiene 2 bugs intencionales (lista vacía sin mensaje y precio sin formato).

- **`ProductoFormulario.jsx` (components)** — formulario controlado con validación client-side de nombre, precio, stock y categoría. Soporta modo creación y modo edición (poblado desde `productoEditar`). Contiene 2 bugs intencionales en el manejo de errores de validación.

- **`Dockerfile` (frontend)** — multi-stage build: etapa Node 18 Alpine para `npm run build`, etapa Nginx Alpine para servir estáticos, expone puerto 3000.

- **`nginx.conf`** — SPA routing (`try_files $uri /index.html`) con proxy `/api → http://app:8080` para el backend.

- **`package.json`** — dependencias: `react@^18.2.0`, `react-dom@^18.2.0`, `react-scripts@5.0.1`, `axios@^1.6.7`. Proxy configurado a `http://localhost:8080` para desarrollo local.

---

#### Infraestructura

- **`docker-compose.yml`** — orquesta tres servicios:
  - `db`: MySQL 8.0 con healthcheck, variables de entorno `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`.
  - `app`: backend Spring Boot, depende de `db` (condición `service_healthy`), variables de entorno para datasource.
  - `frontend`: React/Nginx, depende de `app`, expone puerto 3000.

---

#### Documentación

- **`README.md`** — descripción del proyecto, diagrama de arquitectura Mermaid, descripción de capas, tabla completa de endpoints REST con request/response, instrucciones de instalación local y Docker, ejercicios de práctica con Cursor.

- **`ARCHITECTURE.md`** — diagramas Mermaid del flujo HTTP completo (secuencia), flujo de excepción, relaciones entre clases (diagrama de clases UML) y estructura de paquetes. Documenta 8 decisiones de diseño encontradas en el código.

- **`CHANGELOG.md`** — este archivo.

- **`.cursorrules`** — convenciones del proyecto para backend (inyección por constructor, BigDecimal para dinero, borrado lógico, validación de parámetros) y frontend (componentes funcionales, manejo de estados loading/error/data, formato de moneda).

- **`REPORTE-BUGS.md`** — reporte generado automáticamente con los 14 bugs intencionales del proyecto, su localización, descripción y propuesta de corrección.

---

[Unreleased]: https://github.com/tu-usuario/gestion-productos/compare/HEAD
