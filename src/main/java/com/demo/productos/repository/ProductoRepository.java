package com.demo.productos.repository;

import com.demo.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Producto}.
 * Provee operaciones CRUD y consultas personalizadas.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca productos por categoría (case-insensitive).
     *
     * @param categoria nombre de la categoría a buscar
     * @return lista de productos que pertenecen a la categoría
     */
    List<Producto> findByCategoriaIgnoreCase(String categoria);

    /**
     * Busca productos activos con stock disponible.
     */
    List<Producto> findByActivoTrueAndStockGreaterThan(Integer stock);

    /**
     * Busca productos cuyo nombre contiene el texto indicado.
     */
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // TODO: agregar Javadoc
    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :min AND :max AND p.activo = true")
    List<Producto> findByPrecioEntre(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    // TODO: agregar Javadoc
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria = :categoria AND p.activo = true")
    Long contarPorCategoria(@Param("categoria") String categoria);
}
