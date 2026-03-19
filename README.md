# Gestión de Productos — Proyecto de práctica para Cursor

Microservicio Spring Boot + Frontend React diseñado para aprender a usar Cursor
como asistente de desarrollo con IA.

---

## Estructura del proyecto

```
gestion-productos/
├── src/
│   ├── main/java/com/demo/productos/
│   │   ├── controller/    ProductoController.java
│   │   ├── service/       ProductoService.java   ← 8 bugs intencionales
│   │   ├── repository/    ProductoRepository.java
│   │   ├── model/         Producto.java
│   │   ├── dto/           ProductoDTO.java
│   │   └── exception/     GlobalExceptionHandler.java
│   └── test/              Tests con JUnit 5 + casos TODO
├── frontend/
│   └── src/
│       ├── App.jsx                         ← 2 bugs intencionales
│       ├── components/
│       │   ├── ProductoLista.jsx           ← 2 bugs intencionales
│       │   └── ProductoFormulario.jsx      ← 2 bugs intencionales
│       └── services/productoService.js     ← 1 bug intencional
├── Dockerfile
├── docker-compose.yml
├── .cursorrules
└── README.md
```

---

## Requisitos previos

- Java 17+
- Maven 3.8+
- Node.js 18+
- Docker y Docker Compose (opcional)
- MySQL 8.0 (o usar Docker)

---

## Opción A: Levantar con Docker Compose (recomendado)

```bash
docker-compose up --build
```

- Frontend: http://localhost:3000
- API REST: http://localhost:8080/api/productos

---

## Opción B: Levantar manualmente

### 1. Base de datos MySQL

```sql
CREATE DATABASE gestion_productos;
```

Actualizar credenciales en:
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

## Endpoints disponibles

| Método | URL                              | Descripción                     |
|--------|----------------------------------|---------------------------------|
| GET    | /api/productos                   | Listar todos                    |
| GET    | /api/productos/{id}              | Buscar por ID                   |
| POST   | /api/productos                   | Crear producto                  |
| PUT    | /api/productos/{id}              | Actualizar producto             |
| DELETE | /api/productos/{id}              | Eliminar producto               |
| GET    | /api/productos/categoria/{cat}   | Filtrar por categoría           |
| GET    | /api/productos/disponibles       | Productos con stock > 0         |
| GET    | /api/productos/precio?min=&max=  | Filtrar por rango de precio     |
| PATCH  | /api/productos/{id}/descuento    | Aplicar % de descuento          |

---

## Ejercicios para practicar con Cursor

### Ejercicio 1 — Identificar bugs (modo Agent)
Escribe en el chat de Cursor con modo Agent:
> "Analiza el proyecto completo y genera un archivo REPORTE-BUGS.md
> listando todos los comentarios BUG # que encuentres, con archivo,
> línea, descripción del problema y cómo corregirlo."

### Ejercicio 2 — Completar Javadoc
Abre cualquier archivo con `// TODO: agregar Javadoc` y escribe:
> "@ProductoService.java Genera el Javadoc completo para todos los
> métodos y campos que tienen el comentario TODO."

### Ejercicio 3 — Completar tests faltantes
> "@ProductoServiceTest.java @ProductoService.java Implementa los
> tests marcados como TODO siguiendo el mismo estilo existente."

### Ejercicio 4 — Corregir bug específico
> "@ProductoService.java Corrige el BUG #4 y BUG #6. Para el #4
> implementa borrado lógico con activo=false. Para el #6 usa
> BigDecimal para el cálculo del descuento."

### Ejercicio 5 — Generar código nuevo
> "@ProductoRepository.java @ProductoService.java Agrega un método
> buscarPorNombre() siguiendo el mismo patrón de los métodos existentes."
