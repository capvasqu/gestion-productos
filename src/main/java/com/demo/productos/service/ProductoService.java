package com.demo.productos.service;

import com.demo.productos.dto.ProductoDTO;
import com.demo.productos.exception.ProductoNotFoundException;
import com.demo.productos.model.Producto;
import com.demo.productos.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service containing the business logic for product management.
 *
 * NOTE FOR CURSOR PRACTICE:
 * This service contains intentional bugs and improvement opportunities.
 * Use Cursor to identify and fix them.
 */
@Service
public class ProductoService {

    // BUG #1: Field injection instead of constructor injection (violates best practices)
    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Returns all products in the system.
     */
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    /**
     * Finds a product by its ID.
     *
     * @param id product identifier
     * @return the found product
     */
    public Producto obtenerPorId(Long id) {
        // BUG #2: does not validate that id is not null or negative
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException("Product not found with id: " + id));
    }

    // TODO: add Javadoc to this method
    public Producto crear(ProductoDTO dto) {
        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setCategoria(dto.getCategoria());
        // BUG #3: does not check if a product with the same name already exists
        return productoRepository.save(producto);
    }

    // TODO: add Javadoc to this method
    public Producto actualizar(Long id, ProductoDTO dto) {
        Producto producto = obtenerPorId(id);
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setCategoria(dto.getCategoria());
        return productoRepository.save(producto);
    }

    // TODO: add Javadoc to this method
    public void eliminar(Long id) {
        // BUG #4: physically deletes instead of soft delete (activo = false)
        productoRepository.deleteById(id);
    }

    /**
     * Applies a discount to a product's price.
     *
     * @param id         product identifier
     * @param porcentaje discount percentage to apply
     * @return product with updated price
     */
    public Producto aplicarDescuento(Long id, double porcentaje) {
        Producto producto = obtenerPorId(id);
        // BUG #5: does not validate that porcentaje is between 0 and 100
        // BUG #6: precision loss using double for monetary calculation
        double nuevoPrecio = producto.getPrecio().doubleValue() * (1 - porcentaje / 100);
        producto.setPrecio(BigDecimal.valueOf(nuevoPrecio));
        return productoRepository.save(producto);
    }

    // TODO: add Javadoc to this method
    public List<Producto> buscarPorCategoria(String categoria) {
        // BUG #7: does not validate that categoria is not null or empty
        return productoRepository.findByCategoriaIgnoreCase(categoria);
    }

    // TODO: add Javadoc to this method
    public List<Producto> buscarDisponibles() {
        return productoRepository.findByActivoTrueAndStockGreaterThan(0);
    }

    // TODO: add Javadoc to this method
    public List<Producto> buscarPorRangoPrecio(BigDecimal min, BigDecimal max) {
        // BUG #8: does not validate that min <= max
        return productoRepository.findByPrecioEntre(min, max);
    }
}
