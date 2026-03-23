# Gestión de Productos

## Sobre este proyecto

Proyecto desarrollado como práctica de automatización con IA.
Incluye un reporte de bugs generado automáticamente por Claude Code
en modo agente, analizando el repositorio completo sin intervención manual.

**Herramientas utilizadas:** Claude Code · Cursor · Spring Boot · React · Docker

# Gestión de Productos — Proyecto de práctica para Cursor

API REST con Spring Boot 3 y frontend React para la gestión de un catálogo de productos.
Diseñado como proyecto de práctica con Cursor IDE: incluye bugs intencionales, TODOs y tests incompletos para trabajar con IA asistida.

---

## Tabla de contenidos

- [Descripción](#descripción)
- [Arquitectura](#arquitectura)
- [Capas de la aplicación](#capas-de-la-aplicación)
- [Endpoints REST](#endpoints-rest)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Ejercicios de práctica con Cursor](#ejercicios-de-práctica-con-cursor)

---

## Descripción

`gestion-productos` es un CRUD completo que permite crear, consultar, actualizar y eliminar productos desde una interfaz web. El backend expone una API REST documentada y el frontend React se comunica con ella a través de Axios, con el proxy de Nginx en producción Docker.

**Stack tecnológico**

| Capa | Tecnología |
|------|-----------|
| Backend | Spring Boot 3.2.3, Java 17 |
| Persistencia | Spring Data JPA + Hibernate + MySQL 8.0 |
| Validación | Jakarta Bean Validation |
| Frontend | React 18, Axios |
| Servidor web | Nginx (producción) |
| Contenerización | Docker + Docker Compose |
| Tests | JUnit 5, Mockito, AssertJ, H2 (in-memory) |

---

## Arquitectura

```mermaid
graph TB
    subgraph Frontend["Frontend (React 18 · puerto 3000)"]
        UI[App.jsx]
        Lista[ProductoLista.jsx]
        Form[ProductoFormulario.jsx]
        Svc[productoService.js · Axios]
        UI --> Lista
        UI --> Form
        UI --> Svc
    end

    subgraph Nginx["Nginx (proxy /api → backend)"]
        NX[nginx.conf]
    end

    subgraph Backend["Backend (Spring Boot · puerto 8080)"]
        Ctrl[ProductoController\n@RestController /api/productos]
        Serv[ProductoService\n@Service]
        Repo[ProductoRepository\n@Repository · JPA]
        Ent[Producto\n@Entity]
        DTO[ProductoDTO\n@Valid]
        EX[GlobalExceptionHandler\n@RestControllerAdvice]
        Ctrl --> Serv
        Ctrl --> DTO
        Serv --> Repo
        Repo --> Ent
        EX -.maneja excepciones.-> Ctrl
    end

    subgraph DB["Base de datos"]
        MySQL[(MySQL 8.0\ngestion_productos)]
        H2[(H2 in-memory\nperfil test)]
    end

    Svc -- HTTP/JSON --> Nginx
    Nginx -- proxy_pass --> Ctrl
    Repo -- JPA/Hibernate --> MySQL
    Repo -. test profile .-> H2
```

---

## Capas de la aplicación

### `model/` — Entidad JPA

**`Producto.java`** mapea la tabla `productos` con los campos:

| Campo | Tipo | Restricciones |
|-------|------|---------------|
| `id` | Long | PK, auto-generado |
| `nombre` | String | NotBlank, 2–100 caracteres |
| `descripcion` | String | Máx. 500 caracteres |
| `precio` | BigDecimal | NotNull, > 0, precision(10,2) |
| `stock` | Integer | Min 0 |
| `categoria` | String | NotBlank |
| `activo` | Boolean | Default true |
| `fechaCreacion` | LocalDateTime | Auto-set en @PrePersist |
| `fechaActualizacion` | LocalDateTime | Auto-set en @PrePersist/@PreUpdate |

---

### `dto/` — Objeto de transferencia

**`ProductoDTO.java`** es el contrato de entrada para crear y actualizar productos. Contiene las mismas validaciones de negocio que la entidad pero sin campos generados por el sistema (`id`, `activo`, timestamps). El controlador recibe siempre un DTO con `@Valid` y la capa de servicio construye la entidad a partir de él.

---

### `repository/` — Acceso a datos

**`ProductoRepository`** extiende `JpaRepository<Producto, Long>` y agrega:

| Método | Tipo | Descripción |
|--------|------|-------------|
| `findByCategoriaIgnoreCase(String)` | Derived Query | Busca por categoría (case-insensitive) |
| `findByActivoTrueAndStockGreaterThan(Integer)` | Derived Query | Productos activos con stock > N |
| `findByNombreContainingIgnoreCase(String)` | Derived Query | Búsqueda parcial por nombre |
| `findByPrecioEntre(BigDecimal, BigDecimal)` | JPQL | Rango de precio en productos activos |
| `contarPorCategoria(String)` | JPQL | Conteo de activos por categoría |

---

### `service/` — Lógica de negocio

**`ProductoService`** orquesta las operaciones CRUD y las consultas de negocio:

| Método | Descripción |
|--------|-------------|
| `obtenerTodos()` | Devuelve todos los productos |
| `obtenerPorId(Long)` | Busca por ID o lanza `ProductoNotFoundException` |
| `crear(ProductoDTO)` | Construye y persiste un nuevo producto |
| `actualizar(Long, ProductoDTO)` | Actualiza campos de un producto existente |
| `eliminar(Long)` | Elimina por ID |
| `aplicarDescuento(Long, double)` | Aplica descuento porcentual al precio |
| `buscarPorCategoria(String)` | Delega a repositorio por categoría |
| `buscarDisponibles()` | Activos con stock > 0 |
| `buscarPorRangoPrecio(BigDecimal, BigDecimal)` | Filtra por rango de precio |

> El archivo contiene **8 bugs intencionales** comentados como `// BUG #N` para los ejercicios de práctica.

---

### `controller/` — Capa REST

**`ProductoController`** expone todos los endpoints bajo `/api/productos` con CORS habilitado para `http://localhost:3000`. Usa inyección por constructor, valida entradas con `@Valid` y devuelve los códigos HTTP correctos (201 al crear, 204 al eliminar).

---

### `exception/` — Manejo de errores

| Clase | Rol |
|-------|-----|
| `ProductoNotFoundException` | RuntimeException lanzada cuando un producto no existe |
| `GlobalExceptionHandler` | `@RestControllerAdvice` que intercepta excepciones y formatea respuestas JSON con `timestamp`, `status`, `error` y `message` |

Respuestas de error:
- **404** — `ProductoNotFoundException`
- **400** — Fallos de validación Jakarta Bean (incluye mapa campo→mensaje)
- **500** — Cualquier excepción no controlada

---

## Endpoints REST

Base URL: `http://localhost:8080/api/productos`

### GET `/`
Retorna todos los productos.

**Respuesta 200**
```json
[
  {
    "id": 1,
    "nombre": "Laptop Pro",
    "descripcion": "Laptop de alto rendimiento",
    "precio": 1299.99,
    "stock": 15,
    "categoria": "Electrónica",
    "activo": true,
    "fechaCreacion": "2026-03-01T10:00:00",
    "fechaActualizacion": "2026-03-01T10:00:00"
  }
]
```

---

### GET `/{id}`
Busca un producto por su ID.

**Path param**: `id` (Long)

| Código | Situación |
|--------|-----------|
| 200 | Producto encontrado |
| 404 | ID no existe |

---

### POST `/`
Crea un nuevo producto.

**Body** (`application/json`)
```json
{
  "nombre": "Laptop Pro",
  "descripcion": "Laptop de alto rendimiento",
  "precio": 1299.99,
  "stock": 15,
  "categoria": "Electrónica"
}
```

| Campo | Requerido | Restricciones |
|-------|-----------|---------------|
| `nombre` | Sí | 2–100 caracteres |
| `descripcion` | No | Máx. 500 caracteres |
| `precio` | Sí | Número > 0 |
| `stock` | Sí | Entero ≥ 0 |
| `categoria` | Sí | No vacío |

| Código | Situación |
|--------|-----------|
| 201 | Creado correctamente |
| 400 | Validación fallida (mapa campo→error) |

---

### PUT `/{id}`
Actualiza todos los campos de un producto existente.

**Path param**: `id` (Long)
**Body**: mismo esquema que POST

| Código | Situación |
|--------|-----------|
| 200 | Actualizado correctamente |
| 400 | Validación fallida |
| 404 | ID no existe |

---

### DELETE `/{id}`
Elimina un producto por ID.

**Path param**: `id` (Long)

| Código | Situación |
|--------|-----------|
| 204 | Eliminado correctamente |
| 404 | ID no existe |

---

### GET `/categoria/{categoria}`
Filtra productos por categoría (case-insensitive).

**Path param**: `categoria` (String)
**Respuesta 200**: array de productos

---

### GET `/disponibles`
Retorna productos activos con stock > 0.

**Respuesta 200**: array de productos

---

### GET `/precio?min={min}&max={max}`
Filtra productos activos cuyo precio está entre `min` y `max`.

**Query params**:
- `min` — BigDecimal
- `max` — BigDecimal

**Respuesta 200**: array de productos

---

### PATCH `/{id}/descuento?porcentaje={porcentaje}`
Aplica un descuento porcentual al precio del producto.

**Path param**: `id` (Long)
**Query param**: `porcentaje` (double, ej. `10` = 10%)

| Código | Situación |
|--------|-----------|
| 200 | Descuento aplicado |
| 404 | ID no existe |

---

## Instalación y ejecución

### Requisitos previos

- Java 17+
- Maven 3.8+
- Node.js 18+
- Docker y Docker Compose (opcional)
- MySQL 8.0 (si no se usa Docker)

---

### Opción A — Docker Compose (recomendado)

Levanta MySQL, el backend y el frontend con un solo comando:

```bash
docker-compose up --build
```

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:3000 |
| API REST | http://localhost:8080/api/productos |
| MySQL | localhost:3306 |

Para detener y limpiar:

```bash
docker-compose down -v
```

---

### Opción B — Ejecución local

#### 1. Base de datos MySQL

```sql
CREATE DATABASE gestion_productos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Editar credenciales en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gestion_productos?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=tu_password
```

#### 2. Backend

```bash
# Compilar y ejecutar tests
mvn clean install

# Iniciar el servidor (puerto 8080)
mvn spring-boot:run
```

#### 3. Frontend

```bash
cd frontend
npm install
npm start   # Abre http://localhost:3000
```

#### 4. Ejecutar solo los tests

```bash
# Backend (usa H2 en memoria, no requiere MySQL)
mvn test

# Frontend
cd frontend && npm test
```

---

## Estructura del proyecto

```
gestion-productos/
├── src/
│   ├── main/
│   │   ├── java/com/demo/productos/
│   │   │   ├── GestionProductosApplication.java
│   │   │   ├── controller/
│   │   │   │   └── ProductoController.java
│   │   │   ├── service/
│   │   │   │   └── ProductoService.java        ← 8 bugs intencionales
│   │   │   ├── repository/
│   │   │   │   └── ProductoRepository.java
│   │   │   ├── model/
│   │   │   │   └── Producto.java
│   │   │   ├── dto/
│   │   │   │   └── ProductoDTO.java
│   │   │   └── exception/
│   │   │       ├── ProductoNotFoundException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-test.properties
│   └── test/java/com/demo/productos/
│       ├── service/
│       │   └── ProductoServiceTest.java
│       └── controller/
│           └── ProductoControllerTest.java
├── frontend/
│   ├── src/
│   │   ├── App.jsx                             ← 2 bugs intencionales
│   │   ├── index.js
│   │   ├── components/
│   │   │   ├── ProductoLista.jsx               ← 2 bugs intencionales
│   │   │   └── ProductoFormulario.jsx          ← 2 bugs intencionales
│   │   └── services/
│   │       └── productoService.js              ← 1 bug intencional
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── .cursorrules
├── ARCHITECTURE.md
├── CHANGELOG.md
└── README.md
```

---

## Ejercicios de práctica con Cursor

### Ejercicio 1 — Identificar bugs (modo Agent)
```
Analiza el proyecto completo y genera un archivo REPORTE-BUGS.md
listando todos los comentarios BUG # que encuentres, con archivo,
línea, descripción del problema y cómo corregirlo.
```

### Ejercicio 2 — Completar Javadoc
```
@ProductoService.java Genera el Javadoc completo para todos los
métodos y campos que tienen el comentario TODO.
```

### Ejercicio 3 — Completar tests faltantes
```
@ProductoServiceTest.java @ProductoService.java Implementa los
tests marcados como TODO siguiendo el mismo estilo existente.
```

### Ejercicio 4 — Corregir bug específico
```
@ProductoService.java Corrige el BUG #4 y BUG #6.
Para el #4 implementa borrado lógico con activo=false.
Para el #6 usa BigDecimal para el cálculo del descuento.
```

### Ejercicio 5 — Generar código nuevo
```
@ProductoRepository.java @ProductoService.java Agrega un método
buscarPorNombre() siguiendo el mismo patrón de los métodos existentes.
```
