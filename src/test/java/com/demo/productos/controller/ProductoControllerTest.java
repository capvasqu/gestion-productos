package com.demo.productos.controller;

import com.demo.productos.dto.ProductoDTO;
import com.demo.productos.exception.ProductoNotFoundException;
import com.demo.productos.model.Producto;
import com.demo.productos.service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@DisplayName("ProductoController Tests")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Producto productoEjemplo;
    private ProductoDTO dtoEjemplo;

    @BeforeEach
    void setUp() {
        productoEjemplo = new Producto();
        productoEjemplo.setId(1L);
        productoEjemplo.setNombre("Mouse Logitech");
        productoEjemplo.setPrecio(new BigDecimal("29.99"));
        productoEjemplo.setStock(50);
        productoEjemplo.setCategoria("Peripherals");
        productoEjemplo.setActivo(true);

        dtoEjemplo = new ProductoDTO("Mouse Logitech", "Wireless mouse",
                new BigDecimal("29.99"), 50, "Peripherals");
    }

    @Test
    @DisplayName("GET /productos should return list with status 200")
    void obtenerTodos_debeRetornar200() throws Exception {
        when(productoService.obtenerTodos()).thenReturn(List.of(productoEjemplo));

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Mouse Logitech"))
                .andExpect(jsonPath("$[0].precio").value(29.99));
    }

    @Test
    @DisplayName("GET /productos/{id} should return 404 when not found")
    void obtenerPorId_cuandoNoExiste_debeRetornar404() throws Exception {
        when(productoService.obtenerPorId(99L))
                .thenThrow(new ProductoNotFoundException("Product not found with id: 99"));

        mockMvc.perform(get("/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 99"));
    }

    @Test
    @DisplayName("POST /productos should create product and return 201")
    void crear_conDatosValidos_debeRetornar201() throws Exception {
        when(productoService.crear(any(ProductoDTO.class))).thenReturn(productoEjemplo);

        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoEjemplo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /productos with empty name should return 400")
    void crear_conNombreVacio_debeRetornar400() throws Exception {
        dtoEjemplo.setNombre("");

        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoEjemplo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nombre").exists());
    }

    // TODO: add test for PUT /productos/{id}
    // TODO: add test for DELETE /productos/{id}
    // TODO: add test for PATCH /productos/{id}/descuento with invalid porcentaje
}
