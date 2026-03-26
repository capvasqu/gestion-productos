# Product Management — Practice project for Cursor

Spring Boot microservice + React frontend designed to learn how to use Cursor
as an AI-assisted development tool.

---

## About this project

Project developed as a practice exercise for AI-driven automation.
Includes a bug report generated automatically by Claude Code in agent mode,
analyzing the full repository without manual intervention.

**Tools used:** Claude Code · Cursor · Spring Boot · React · Docker

---

## Project structure

```
gestion-productos/
├── src/
│   ├── main/java/com/demo/productos/
│   │   ├── controller/    ProductoController.java
│   │   ├── service/       ProductoService.java   ← 8 intentional bugs
│   │   ├── repository/    ProductoRepository.java
│   │   ├── model/         Producto.java
│   │   ├── dto/           ProductoDTO.java
│   │   └── exception/     GlobalExceptionHandler.java
│   └── test/              JUnit 5 tests + TODO cases
├── frontend/
│   └── src/
│       ├── App.jsx                         ← 2 intentional bugs
│       ├── components/
│       │   ├── ProductoLista.jsx           ← 2 intentional bugs
│       │   └── ProductoFormulario.jsx      ← 2 intentional bugs
│       └── services/productoService.js     ← 1 intentional bug
├── Dockerfile
├── docker-compose.yml
├── .cursorrules
└── README.md
```

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+
- Docker and Docker Compose (optional)
- MySQL 8.0 (or use Docker)

---

## Option A: Run with Docker Compose (recommended)

```bash
docker-compose up --build
```

- Frontend: http://localhost:3000
- REST API: http://localhost:8080/api/productos

---

## Option B: Run manually

### 1. MySQL database

```sql
CREATE DATABASE gestion_productos;
```

Update credentials in:
`src/main/resources/application.properties`

### 2. Backend

```bash
mvn clean install
mvn spring-boot:run
```

### 3. Frontend

```bash
cd frontend
npm install
npm start
```

---

## Available endpoints

| Method | URL                              | Description                     |
|--------|----------------------------------|---------------------------------|
| GET    | /api/productos                   | List all products                |
| GET    | /api/productos/{id}              | Find by ID                      |
| POST   | /api/productos                   | Create product                  |
| PUT    | /api/productos/{id}              | Update product                  |
| DELETE | /api/productos/{id}              | Delete product                  |
| GET    | /api/productos/categoria/{cat}   | Filter by category              |
| GET    | /api/productos/disponibles       | Products with stock > 0         |
| GET    | /api/productos/precio?min=&max=  | Filter by price range           |
| PATCH  | /api/productos/{id}/descuento    | Apply discount percentage       |

---

## Practice exercises with Cursor

### Exercise 1 — Code review (identify bugs)
Open `ProductoService.java` in Cursor and type in the chat:
> "Review this service and identify all quality issues, bugs and bad practices you find"

Documented bugs in the code: BUG #1 to #8 (backend) and BUG #9 to #14 (frontend)

### Exercise 2 — Complete Javadoc
Open any file with `// TODO: add Javadoc` comments and type:
> "Generate complete Javadoc for all methods and fields marked with TODO"

### Exercise 3 — Complete tests
Open `ProductoServiceTest.java` and type:
> "Implement the tests marked as TODO following the same style as the existing tests"

### Exercise 4 — Guided refactoring
Open `ProductoService.java` and type:
> "Refactor the aplicarDescuento() method to fix bugs #5 and #6 using BigDecimal correctly"

### Exercise 5 — Generate new code
Type in the Cursor chat:
> "@ProductoRepository.java @ProductoService.java Add a buscarPorNombre() method that searches products whose name contains the given text, following the same pattern as the existing methods"
