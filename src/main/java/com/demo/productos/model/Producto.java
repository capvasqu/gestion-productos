package com.demo.productos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a product in the system.
 * Mapped to the 'productos' table in the database.
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
     * Product name.
     */
    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 100)
    @Column(nullable = false)
    private String nombre;

    // TODO: add Javadoc to this field
    @Size(max = 500)
    private String descripcion;

    // TODO: add Javadoc to this field
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    // TODO: add Javadoc to this field
    @Min(0)
    @Column(nullable = false)
    private Integer stock;

    /**
     * Product category.
     */
    @NotBlank
    private String categoria;

    // TODO: add Javadoc to this field
    private Boolean activo;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // TODO: add Javadoc to this field
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
