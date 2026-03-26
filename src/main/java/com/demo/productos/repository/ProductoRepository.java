package com.demo.productos.repository;

import com.demo.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * JPA Repository for the {@link Producto} entity.
 * Provides CRUD operations and custom queries.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Finds products by category (case-insensitive).
     *
     * @param categoria category name to search
     * @return list of products belonging to the category
     */
    List<Producto> findByCategoriaIgnoreCase(String categoria);

    /**
     * Finds active products with available stock.
     */
    List<Producto> findByActivoTrueAndStockGreaterThan(Integer stock);

    /**
     * Finds products whose name contains the given text.
     */
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // TODO: add Javadoc
    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :min AND :max AND p.activo = true")
    List<Producto> findByPrecioEntre(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    // TODO: add Javadoc
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria = :categoria AND p.activo = true")
    Long contarPorCategoria(@Param("categoria") String categoria);
}
