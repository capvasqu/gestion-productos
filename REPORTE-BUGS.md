# REPORTE DE BUGS

Fecha: 2026-03-19
Total de bugs encontrados: **14**

---

## Backend — `src/main/java/com/demo/productos/service/ProductoService.java`

---

### BUG #1 — Inyección por campo en lugar de constructor

**Archivo:** `src/main/java/com/demo/productos/service/ProductoService.java` · línea 23
**Descripción:** Se usa `@Autowired` sobre el campo directamente. Esto dificulta las pruebas unitarias, oculta las dependencias reales de la clase y puede provocar objetos en estado inválido.

**Corrección:**
```java
// Eliminar el @Autowired sobre el campo:
// @Autowired
// private ProductoRepository productoRepository;

// Agregar campo final y constructor:
private final ProductoRepository productoRepository;

public ProductoService(ProductoRepository productoRepository) {
    this.productoRepository = productoRepository;
}
```

---

### BUG #2 — No se valida que `id` no sea `null` ni negativo

**Archivo:** `src/main/java/com/demo/productos/service/ProductoService.java` · línea 41
**Descripción:** El método `obtenerPorId` acepta cualquier valor sin verificación previa. Un `id` nulo lanzará una `NullPointerException` no controlada; un `id` negativo generará una búsqueda sin sentido.

**Corrección:**
```java
public Producto obtenerPorId(Long id) {
    if (id == null || id <= 0) {
        throw new IllegalArgumentException("El id debe ser un número positivo");
    }
    return productoRepository.findById(id)
            .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con id: " + id));
}
```

---

### BUG #3 — No se verifica si ya existe un producto con el mismo nombre

**Archivo:** `src/main/java/com/demo/productos/service/ProductoService.java` · línea 54
**Descripción:** Al crear un producto no se comprueba si ya existe otro con el mismo nombre, lo que permite duplicados en la base de datos.

**Corrección:**
```java
public Producto crear(ProductoDTO dto) {
    if (productoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
        throw new IllegalArgumentException("Ya existe un producto con el nombre: " + dto.getNombre());
    }
    Producto producto = new Producto();
    producto.setNombre(dto.getNombre());
    producto.setDescripcion(dto.getDescripcion());
    producto.setPrecio(dto.getPrecio());
    producto.setStock(dto.getStock());
    producto.setCategoria(dto.getCategoria());
    return productoRepository.save(producto);
}
```
> Agregar en `ProductoRepository`: `boolean existsByNombreIgnoreCase(String nombre);`

---

### BUG #4 — Eliminación física en lugar de borrado lógico

**Archivo:** `src/main/java/com/demo/productos/service/ProductoService.java` · línea 71
**Descripción:** `deleteById` borra el registro de forma permanente. Si el producto tiene referencias en otras tablas (pedidos, historial), esto puede romper la integridad referencial o eliminar información importante.

**Corrección:**
```java
public void eliminar(Long id) {
    Producto producto = obtenerPorId(id);
    producto.setActivo(false);
    productoRepository.save(producto);
}
```

---

### BUG #5 — No se valida que el porcentaje de descuento esté entre 0 y 100

**Archivo:** `src/main/java/com/demo/productos/service/ProductoService.java` · línea 84
**Descripción:** Un porcentaje negativo incrementaría el precio; uno mayor a 100 lo pondría en negativo. Ambos corrompen los datos sin ningún aviso.

**Corrección:**
```java
public Producto aplicarDescuento(Long id, double porcentaje) {
    if (porcentaje < 0 || porcentaje > 100) {
        throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
    }
    // ... resto del método
}
```

---

### BUG #6 — Pérdida de precisión al usar `double` en cálculos monetarios

**Archivo:** `src/main/java/com/demo/productos/service/ProductoService.java` · línea 85
**Descripción:** Convertir `BigDecimal` a `double` y volver introduce errores de punto flotante que pueden acumular diferencias en precios y afectar facturación.

**Corrección:**
```java
BigDecimal factor = BigDecimal.ONE.subtract(
    BigDecimal.valueOf(porcentaje).divide(BigDecimal.valueOf(100))
);
BigDecimal nuevoPrecio = producto.getPrecio()
    .multiply(factor)
    .setScale(2, RoundingMode.HALF_UP);
producto.setPrecio(nuevoPrecio);
```

---

### BUG #7 — No se valida que `categoria` no sea `null` ni vacía

**Archivo:** `src/main/java/com/demo/productos/service/ProductoService.java` · línea 93
**Descripción:** Pasar `null` o una cadena vacía como categoría puede lanzar una excepción no controlada o devolver resultados incorrectos.

**Corrección:**
```java
public List<Producto> buscarPorCategoria(String categoria) {
    if (categoria == null || categoria.isBlank()) {
        throw new IllegalArgumentException("La categoría no puede ser nula ni vacía");
    }
    return productoRepository.findByCategoriaIgnoreCase(categoria);
}
```

---

### BUG #8 — No se valida que `min <= max` en el rango de precios

**Archivo:** `src/main/java/com/demo/productos/service/ProductoService.java` · línea 104
**Descripción:** Si `min` es mayor que `max`, la consulta devuelve un resultado vacío o incorrecto sin informar al cliente del error en los parámetros.

**Corrección:**
```java
public List<Producto> buscarPorRangoPrecio(BigDecimal min, BigDecimal max) {
    if (min == null || max == null) {
        throw new IllegalArgumentException("Los valores min y max no pueden ser nulos");
    }
    if (min.compareTo(max) > 0) {
        throw new IllegalArgumentException("El precio mínimo no puede ser mayor que el máximo");
    }
    return productoRepository.findByPrecioEntre(min, max);
}
```

---

## Frontend — `frontend/src/services/productoService.js`

---

### BUG #9 — Método HTTP incorrecto en `eliminar` (GET en lugar de DELETE)

**Archivo:** `frontend/src/services/productoService.js` · línea 19
**Descripción:** La función `eliminar` usa `API.get` en vez de `API.delete`, por lo que nunca invoca el endpoint correcto. El producto no se elimina y la petición puede retornar datos inesperados.

**Corrección:**
```javascript
// Antes:
export const eliminar = (id) => API.get(`/${id}`);

// Después:
export const eliminar = (id) => API.delete(`/${id}`);
```

---

## Frontend — `frontend/src/App.jsx`

---

### BUG #10 — Mensaje de error genérico que no muestra el error real del servidor

**Archivo:** `frontend/src/App.jsx` · línea 23
**Descripción:** Cualquier fallo al cargar productos muestra el mismo texto estático, ocultando la causa real (timeout, 404, 500, etc.) y dificultando el diagnóstico.

**Corrección:**
```javascript
} catch (err) {
    const mensaje =
        err.response?.data?.message ||
        err.message ||
        'Error desconocido al cargar productos';
    setError(`Error al cargar productos: ${mensaje}`);
}
```

---

## Frontend — `frontend/src/components/ProductoLista.jsx`

---

### BUG #11 — No se maneja el caso de lista vacía con mensaje amigable

**Archivo:** `frontend/src/components/ProductoLista.jsx` · línea 6
**Descripción:** Cuando `productos` existe pero está vacío (`[]`), la tabla se renderiza sin filas y el usuario ve una tabla en blanco sin entender si hay un problema o simplemente no hay datos.

**Corrección:**
```javascript
function ProductoLista({ productos, onEditar, onEliminar }) {
    if (!productos) return <p>Cargando...</p>;
    if (productos.length === 0) return <p>No hay productos registrados.</p>;
    // ... resto del componente
}
```

---

### BUG #12 — El precio no se formatea como moneda

**Archivo:** `frontend/src/components/ProductoLista.jsx` · línea 54
**Descripción:** El precio se muestra como número crudo (ej. `1500`) en lugar de un valor monetario formateado, lo que reduce la legibilidad y puede causar ambigüedad.

**Corrección:**
```jsx
{/* Antes: */}
<td style={estiloTd}>{p.precio}</td>

{/* Después: */}
<td style={estiloTd}>
    {Number(p.precio).toLocaleString('es-AR', { style: 'currency', currency: 'ARS' })}
</td>
```
> Ajustar el locale y la moneda según el mercado objetivo del proyecto.

---

## Frontend — `frontend/src/components/ProductoFormulario.jsx`

---

### BUG #13 — Los errores de campo no se limpian al editarlos

**Archivo:** `frontend/src/components/ProductoFormulario.jsx` · línea 37
**Descripción:** Cuando el usuario corrige un campo con error y empieza a escribir, el mensaje de error permanece visible hasta el próximo envío del formulario, generando una UX confusa.

**Corrección:**
```javascript
const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    // Limpiar el error del campo al editarlo:
    setErrores(prev => ({ ...prev, [name]: undefined }));
};
```

---

### BUG #14 — No se muestran los errores de validación devueltos por el backend

**Archivo:** `frontend/src/components/ProductoFormulario.jsx` · línea 71
**Descripción:** Si el backend devuelve errores de validación por campo (ej. Bean Validation de Spring), todos se ignoran y se muestra un único mensaje genérico. El usuario no sabe qué campo corregir.

**Corrección:**
```javascript
} catch (err) {
    const backendErrors = err.response?.data?.errors;
    if (backendErrors && typeof backendErrors === 'object') {
        setErrores(backendErrors);
    } else {
        const mensaje =
            err.response?.data?.message ||
            err.message ||
            'Error al guardar el producto';
        setErrores({ general: mensaje });
    }
}
```

---

## Resumen

| # | Archivo | Descripción breve | Severidad |
|---|---------|-------------------|-----------|
| 1 | `ProductoService.java` | Inyección por campo en vez de constructor | Media |
| 2 | `ProductoService.java` | Sin validación de `id` (null/negativo) | Alta |
| 3 | `ProductoService.java` | Sin verificación de nombre duplicado | Media |
| 4 | `ProductoService.java` | Borrado físico en vez de lógico | Alta |
| 5 | `ProductoService.java` | Sin validación del porcentaje de descuento | Alta |
| 6 | `ProductoService.java` | Pérdida de precisión con `double` en precios | Alta |
| 7 | `ProductoService.java` | Sin validación de categoría (null/vacía) | Media |
| 8 | `ProductoService.java` | Sin validación de rango de precios (min ≤ max) | Media |
| 9 | `productoService.js` | Método HTTP GET en vez de DELETE | Crítica |
| 10 | `App.jsx` | Mensaje de error genérico sin detalles | Baja |
| 11 | `ProductoLista.jsx` | Sin mensaje para lista vacía | Baja |
| 12 | `ProductoLista.jsx` | Precio sin formato de moneda | Baja |
| 13 | `ProductoFormulario.jsx` | Errores de campo no se limpian al editar | Media |
| 14 | `ProductoFormulario.jsx` | Errores de validación del backend ignorados | Media |
