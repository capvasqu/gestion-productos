package com.demo.productos.service;

import com.demo.productos.dto.ProductoDTO;
import com.demo.productos.exception.ProductoNotFoundException;
import com.demo.productos.model.Producto;
import com.demo.productos.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del ProductoService")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto productoEjemplo;
    private ProductoDTO dtoEjemplo;

    @BeforeEach
    void setUp() {
        productoEjemplo = new Producto();
        productoEjemplo.setId(1L);
        productoEjemplo.setNombre("Laptop Dell");
        productoEjemplo.setDescripcion("Laptop para desarrollo");
        productoEjemplo.setPrecio(new BigDecimal("999.99"));
        productoEjemplo.setStock(10);
        productoEjemplo.setCategoria("Electrónica");
        productoEjemplo.setActivo(true);

        dtoEjemplo = new ProductoDTO(
                "Laptop Dell",
                "Laptop para desarrollo",
                new BigDecimal("999.99"),
                10,
                "Electrónica"
        );
    }

    @Test
    @DisplayName("Debe retornar todos los productos")
    void obtenerTodos_debeRetornarLista() {
        when(productoRepository.findAll()).thenReturn(List.of(productoEjemplo));

        List<Producto> resultado = productoService.obtenerTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Laptop Dell");
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar producto cuando existe el ID")
    void obtenerPorId_cuandoExiste_debeRetornarProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));

        Producto resultado = productoService.obtenerPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el ID no existe")
    void obtenerPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerPorId(99L))
                .isInstanceOf(ProductoNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Debe crear y retornar el producto guardado")
    void crear_debeGuardarYRetornarProducto() {
        when(productoRepository.save(any(Producto.class))).thenReturn(productoEjemplo);

        Producto resultado = productoService.crear(dtoEjemplo);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Laptop Dell");
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe aplicar descuento correctamente")
    void aplicarDescuento_debeReducirPrecio() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto resultado = productoService.aplicarDescuento(1L, 10.0);

        assertThat(resultado.getPrecio()).isLessThan(new BigDecimal("999.99"));
    }

    // TODO: agregar test para eliminar()
    // TODO: agregar test para buscarPorCategoria() con categoria null
    // TODO: agregar test para aplicarDescuento() con porcentaje > 100
    // TODO: agregar test para buscarPorRangoPrecio() con min > max
}
