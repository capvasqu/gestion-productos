# BUG REPORT

Date: 2026-03-19
Total bugs found: **14**

---

## Backend — `src/main/java/com/demo/productos/service/ProductoService.java`

---

### BUG #1 — Field injection instead of constructor injection

**File:** `src/main/java/com/demo/productos/service/ProductoService.java` · line 23
**Description:** `@Autowired` is used directly on the field. This makes unit testing harder, hides the class's real dependencies, and can lead to objects in an invalid state.

**Fix:**
```java
// Remove @Autowired on the field:
// @Autowired
// private ProductoRepository productoRepository;

// Add final field and constructor:
private final ProductoRepository productoRepository;

public ProductoService(ProductoRepository productoRepository) {
    this.productoRepository = productoRepository;
}
```

---

### BUG #2 — `id` is not validated for null or negative values

**File:** `src/main/java/com/demo/productos/service/ProductoService.java` · line 41
**Description:** The `obtenerPorId` method accepts any value without prior verification. A null `id` will throw an unhandled `NullPointerException`; a negative `id` will produce a meaningless query.

**Fix:**
```java
public Producto obtenerPorId(Long id) {
    if (id == null || id <= 0) {
        throw new IllegalArgumentException("The id must be a positive number");
    }
    return productoRepository.findById(id)
            .orElseThrow(() -> new ProductoNotFoundException("Product not found with id: " + id));
}
```

---

### BUG #3 — No check for duplicate product name

**File:** `src/main/java/com/demo/productos/service/ProductoService.java` · line 54
**Description:** When creating a product, no check is performed to see if another product with the same name already exists, allowing duplicates in the database.

**Fix:**
```java
public Producto crear(ProductoDTO dto) {
    if (productoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
        throw new IllegalArgumentException("A product with the name already exists: " + dto.getNombre());
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
> Add to `ProductoRepository`: `boolean existsByNombreIgnoreCase(String nombre);`

---

### BUG #4 — Hard delete instead of soft delete

**File:** `src/main/java/com/demo/productos/service/ProductoService.java` · line 71
**Description:** `deleteById` permanently removes the record. If the product has references in other tables (orders, history), this can break referential integrity or erase important information.

**Fix:**
```java
public void eliminar(Long id) {
    Producto producto = obtenerPorId(id);
    producto.setActivo(false);
    productoRepository.save(producto);
}
```

---

### BUG #5 — Discount percentage not validated between 0 and 100

**File:** `src/main/java/com/demo/productos/service/ProductoService.java` · line 84
**Description:** A negative percentage would increase the price; one greater than 100 would make it negative. Both corrupt data with no warning.

**Fix:**
```java
public Producto aplicarDescuento(Long id, double porcentaje) {
    if (porcentaje < 0 || porcentaje > 100) {
        throw new IllegalArgumentException("The percentage must be between 0 and 100");
    }
    // ... rest of method
}
```

---

### BUG #6 — Precision loss from using `double` in monetary calculations

**File:** `src/main/java/com/demo/productos/service/ProductoService.java` · line 85
**Description:** Converting `BigDecimal` to `double` and back introduces floating-point errors that can accumulate price differences and affect billing.

**Fix:**
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

### BUG #7 — `categoria` not validated for null or blank

**File:** `src/main/java/com/demo/productos/service/ProductoService.java` · line 93
**Description:** Passing `null` or an empty string as category can throw an unhandled exception or return incorrect results.

**Fix:**
```java
public List<Producto> buscarPorCategoria(String categoria) {
    if (categoria == null || categoria.isBlank()) {
        throw new IllegalArgumentException("The category cannot be null or blank");
    }
    return productoRepository.findByCategoriaIgnoreCase(categoria);
}
```

---

### BUG #8 — Price range not validated for `min <= max`

**File:** `src/main/java/com/demo/productos/service/ProductoService.java` · line 104
**Description:** If `min` is greater than `max`, the query returns an empty or incorrect result without informing the client about the invalid parameters.

**Fix:**
```java
public List<Producto> buscarPorRangoPrecio(BigDecimal min, BigDecimal max) {
    if (min == null || max == null) {
        throw new IllegalArgumentException("The min and max values cannot be null");
    }
    if (min.compareTo(max) > 0) {
        throw new IllegalArgumentException("The minimum price cannot be greater than the maximum");
    }
    return productoRepository.findByPrecioEntre(min, max);
}
```

---

## Frontend — `frontend/src/services/productoService.js`

---

### BUG #9 — Wrong HTTP method in `eliminar` (GET instead of DELETE)

**File:** `frontend/src/services/productoService.js` · line 19
**Description:** The `eliminar` function uses `API.get` instead of `API.delete`, so it never calls the correct endpoint. The product is not deleted and the request may return unexpected data.

**Fix:**
```javascript
// Before:
export const eliminar = (id) => API.get(`/${id}`);

// After:
export const eliminar = (id) => API.delete(`/${id}`);
```

---

## Frontend — `frontend/src/App.jsx`

---

### BUG #10 — Generic error message that does not show the actual server error

**File:** `frontend/src/App.jsx` · line 23
**Description:** Any failure when loading products displays the same static text, hiding the real cause (timeout, 404, 500, etc.) and making diagnosis difficult.

**Fix:**
```javascript
} catch (err) {
    const message =
        err.response?.data?.message ||
        err.message ||
        'Unknown error loading products';
    setError(`Error loading products: ${message}`);
}
```

---

## Frontend — `frontend/src/components/ProductoLista.jsx`

---

### BUG #11 — Empty list not handled with a user-friendly message

**File:** `frontend/src/components/ProductoLista.jsx` · line 6
**Description:** When `productos` exists but is empty (`[]`), the table renders with no rows and the user sees a blank table without knowing whether there is a problem or simply no data.

**Fix:**
```javascript
function ProductoLista({ productos, onEditar, onEliminar }) {
    if (!productos) return <p>Loading...</p>;
    if (productos.length === 0) return <p>No products registered.</p>;
    // ... rest of component
}
```

---

### BUG #12 — Price not formatted as currency

**File:** `frontend/src/components/ProductoLista.jsx` · line 54
**Description:** The price is displayed as a raw number (e.g. `1500`) instead of a formatted monetary value, reducing readability and potentially causing ambiguity.

**Fix:**
```jsx
{/* Before: */}
<td style={estiloTd}>{p.precio}</td>

{/* After: */}
<td style={estiloTd}>
    {Number(p.precio).toLocaleString('es-AR', { style: 'currency', currency: 'ARS' })}
</td>
```
> Adjust the locale and currency to match the project's target market.

---

## Frontend — `frontend/src/components/ProductoFormulario.jsx`

---

### BUG #13 — Field errors not cleared when the field is edited

**File:** `frontend/src/components/ProductoFormulario.jsx` · line 37
**Description:** When the user corrects a field with an error and starts typing, the error message remains visible until the next form submission, creating a confusing UX.

**Fix:**
```javascript
const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    // Clear the field error when it is edited:
    setErrores(prev => ({ ...prev, [name]: undefined }));
};
```

---

### BUG #14 — Backend validation errors not displayed

**File:** `frontend/src/components/ProductoFormulario.jsx` · line 71
**Description:** If the backend returns per-field validation errors (e.g. from Spring Bean Validation), they are all ignored and a single generic message is shown. The user does not know which field to correct.

**Fix:**
```javascript
} catch (err) {
    const backendErrors = err.response?.data?.errors;
    if (backendErrors && typeof backendErrors === 'object') {
        setErrores(backendErrors);
    } else {
        const message =
            err.response?.data?.message ||
            err.message ||
            'Error saving the product';
        setErrores({ general: message });
    }
}
```

---

## Summary

| # | File | Brief description | Severity |
|---|------|-------------------|----------|
| 1 | `ProductoService.java` | Field injection instead of constructor | Medium |
| 2 | `ProductoService.java` | No `id` validation (null/negative) | High |
| 3 | `ProductoService.java` | No duplicate name check | Medium |
| 4 | `ProductoService.java` | Hard delete instead of soft delete | High |
| 5 | `ProductoService.java` | No discount percentage validation | High |
| 6 | `ProductoService.java` | Precision loss using `double` for prices | High |
| 7 | `ProductoService.java` | No category validation (null/blank) | Medium |
| 8 | `ProductoService.java` | No price range validation (min ≤ max) | Medium |
| 9 | `productoService.js` | HTTP GET instead of DELETE | Critical |
| 10 | `App.jsx` | Generic error message without details | Low |
| 11 | `ProductoLista.jsx` | No message for empty list | Low |
| 12 | `ProductoLista.jsx` | Price not formatted as currency | Low |
| 13 | `ProductoFormulario.jsx` | Field errors not cleared on edit | Medium |
| 14 | `ProductoFormulario.jsx` | Backend validation errors ignored | Medium |
