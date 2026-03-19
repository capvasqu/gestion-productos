package com.demo.productos.controller;

import com.demo.productos.dto.ProductoDTO;
import com.demo.productos.model.Producto;
import com.demo.productos.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST para el manejo de productos.
 * Expone endpoints bajo la ruta /api/productos.
 */
@RestController
@RequestMapping("/productos")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Retorna la lista completa de productos.
     *
     * @return lista de productos con status 200
     */
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    /**
     * Busca un producto por su ID.
     *
     * @param id identificador del producto
     * @return producto encontrado con status 200, o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    // TODO: agregar Javadoc
    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(dto));
    }

    // TODO: agregar Javadoc
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id,
                                                @Valid @RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    // TODO: agregar Javadoc
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // TODO: agregar Javadoc
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> porCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(productoService.buscarPorCategoria(categoria));
    }

    // TODO: agregar Javadoc
    @GetMapping("/disponibles")
    public ResponseEntity<List<Producto>> disponibles() {
        return ResponseEntity.ok(productoService.buscarDisponibles());
    }

    // TODO: agregar Javadoc
    @GetMapping("/precio")
    public ResponseEntity<List<Producto>> porRangoPrecio(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return ResponseEntity.ok(productoService.buscarPorRangoPrecio(min, max));
    }

    // TODO: agregar Javadoc
    @PatchMapping("/{id}/descuento")
    public ResponseEntity<Producto> aplicarDescuento(
            @PathVariable Long id,
            @RequestParam double porcentaje) {
        return ResponseEntity.ok(productoService.aplicarDescuento(id, porcentaje));
    }
}
