package com.demo.productos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un producto en el sistema.
 * Mapeada a la tabla 'productos' en la base de datos.
 *
 * @author Carlos
 * @version 1.0
 */
@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del producto.
     */
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100)
    @Column(nullable = false)
    private String nombre;

    // TODO: agregar Javadoc a este campo
    @Size(max = 500)
    private String descripcion;

    // TODO: agregar Javadoc a este campo
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    // TODO: agregar Javadoc a este campo
    @Min(0)
    @Column(nullable = false)
    private Integer stock;

    /**
     * Categoría del producto.
     */
    @NotBlank
    private String categoria;

    // TODO: agregar Javadoc a este campo
    private Boolean activo;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // TODO: agregar Javadoc a este campo
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (activo == null) activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
