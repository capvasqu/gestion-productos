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
 * Servicio que contiene la lógica de negocio para la gestión de productos.
 *
 * NOTA PARA PRÁCTICA CON CURSOR:
 * Este servicio contiene algunos bugs intencionales y oportunidades de mejora.
 * Usa Cursor para identificarlos y corregirlos.
 */
@Service
public class ProductoService {

    // BUG #1: Inyección por campo en lugar de constructor (viola buenas prácticas)
    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Retorna todos los productos del sistema.
     */
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    /**
     * Busca un producto por su ID.
     *
     * @param id identificador del producto
     * @return el producto encontrado
     */
    public Producto obtenerPorId(Long id) {
        // BUG #2: no valida que id no sea null ni negativo
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con id: " + id));
    }

    // TODO: agregar Javadoc a este método
    public Producto crear(ProductoDTO dto) {
        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setCategoria(dto.getCategoria());
        // BUG #3: no verifica si ya existe un producto con el mismo nombre
        return productoRepository.save(producto);
    }

    // TODO: agregar Javadoc a este método
    public Producto actualizar(Long id, ProductoDTO dto) {
        Producto producto = obtenerPorId(id);
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setCategoria(dto.getCategoria());
        return productoRepository.save(producto);
    }

    // TODO: agregar Javadoc a este método
    public void eliminar(Long id) {
        // BUG #4: elimina físicamente en lugar de hacer borrado lógico (activo = false)
        productoRepository.deleteById(id);
    }

    /**
     * Aplica un descuento al precio de un producto.
     *
     * @param id          identificador del producto
     * @param porcentaje  porcentaje de descuento a aplicar
     * @return producto con el precio actualizado
     */
    public Producto aplicarDescuento(Long id, double porcentaje) {
        Producto producto = obtenerPorId(id);
        // BUG #5: no valida que porcentaje esté entre 0 y 100
        // BUG #6: pérdida de precisión al usar double para cálculo monetario
        double nuevoPrecio = producto.getPrecio().doubleValue() * (1 - porcentaje / 100);
        producto.setPrecio(BigDecimal.valueOf(nuevoPrecio));
        return productoRepository.save(producto);
    }

    // TODO: agregar Javadoc a este método
    public List<Producto> buscarPorCategoria(String categoria) {
        // BUG #7: no valida que categoria no sea null o vacía
        return productoRepository.findByCategoriaIgnoreCase(categoria);
    }

    // TODO: agregar Javadoc a este método
    public List<Producto> buscarDisponibles() {
        return productoRepository.findByActivoTrueAndStockGreaterThan(0);
    }

    // TODO: agregar Javadoc a este método
    public List<Producto> buscarPorRangoPrecio(BigDecimal min, BigDecimal max) {
        // BUG #8: no valida que min <= max
        return productoRepository.findByPrecioEntre(min, max);
    }
}
