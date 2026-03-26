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
@DisplayName("ProductoService Tests")
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
        productoEjemplo.setDescripcion("Development laptop");
        productoEjemplo.setPrecio(new BigDecimal("999.99"));
        productoEjemplo.setStock(10);
        productoEjemplo.setCategoria("Electronics");
        productoEjemplo.setActivo(true);

        dtoEjemplo = new ProductoDTO(
                "Laptop Dell",
                "Development laptop",
                new BigDecimal("999.99"),
                10,
                "Electronics"
        );
    }

    @Test
    @DisplayName("Should return all products")
    void obtenerTodos_debeRetornarLista() {
        when(productoRepository.findAll()).thenReturn(List.of(productoEjemplo));

        List<Producto> resultado = productoService.obtenerTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Laptop Dell");
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return product when ID exists")
    void obtenerPorId_cuandoExiste_debeRetornarProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));

        Producto resultado = productoService.obtenerPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw exception when ID does not exist")
    void obtenerPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerPorId(99L))
                .isInstanceOf(ProductoNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should create and return the saved product")
    void crear_debeGuardarYRetornarProducto() {
        when(productoRepository.save(any(Producto.class))).thenReturn(productoEjemplo);

        Producto resultado = productoService.crear(dtoEjemplo);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Laptop Dell");
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("Should apply discount correctly")
    void aplicarDescuento_debeReducirPrecio() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto resultado = productoService.aplicarDescuento(1L, 10.0);

        assertThat(resultado.getPrecio()).isLessThan(new BigDecimal("999.99"));
    }

    // TODO: add test for eliminar()
    // TODO: add test for buscarPorCategoria() with null categoria
    // TODO: add test for aplicarDescuento() with porcentaje > 100
    // TODO: add test for buscarPorRangoPrecio() with min > max
}
