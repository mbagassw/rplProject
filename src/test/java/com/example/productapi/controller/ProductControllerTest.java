package com.example.productapi.controller;

import com.example.productapi.dto.request.ProductRequest;
import com.example.productapi.model.Product;
import com.example.productapi.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("1. GET semua produk")
    void testGetAllProducts() throws Exception {

        Product p1 = new Product(
                1L,
                "Sepatu Nike",
                1000000.0,
                10
        );

        Product p2 = new Product(
                2L,
                "Tas Adidas",
                500000.0,
                5
        );

        when(productService.getAllProducts())
                .thenReturn(Arrays.asList(p1,p2));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name")
                        .value("Sepatu Nike"))
                .andExpect(jsonPath("$[1].name")
                        .value("Tas Adidas"));
    }


    @Test
    @DisplayName("2. GET produk berdasarkan ID")
    void testGetProductById() throws Exception {

        Product product = new Product(
                1L,
                "Sepatu Nike",
                1000000.0,
                10
        );

        when(productService.getProductById(1L))
                .thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.name")
                        .value("Sepatu Nike"));
    }


    @Test
    @DisplayName("3. POST tambah produk")
    void testCreateProduct() throws Exception {

        ProductRequest request = new ProductRequest();

        request.setName("Sepatu Nike");
        request.setPrice(1000000.0);
        request.setStock(10);

        Product product = new Product(
                1L,
                "Sepatu Nike",
                1000000.0,
                10
        );

        when(productService.createProduct(any(ProductRequest.class)))
                .thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name")
                        .value("Sepatu Nike"))
                .andExpect(jsonPath("$.price")
                        .value(1000000.0));
    }


    @Test
    @DisplayName("4. PUT update produk")
    void testUpdateProduct() throws Exception {

        ProductRequest request = new ProductRequest();

        request.setName("Tas Adidas");
        request.setPrice(500000.0);
        request.setStock(5);

        Product updated = new Product(
                1L,
                "Tas Adidas",
                500000.0,
                5
        );

        when(productService.updateProduct(
                eq(1L),
                any(ProductRequest.class)
        )).thenReturn(updated);

        mockMvc.perform(put("/api/products/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Tas Adidas"));
    }


    @Test
    @DisplayName("5. DELETE produk")
    void testDeleteProduct() throws Exception {

        doNothing().when(productService)
                .deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Produk dengan ID 1 berhasil dihapus"
                        ));

        verify(productService)
                .deleteProduct(1L);
    }


    @Test
    @DisplayName("6. POST validasi gagal")
    void testCreateProductValidationFail()
            throws Exception {

        ProductRequest request =
                new ProductRequest();

        request.setName("");
        request.setPrice(0.0);
        request.setStock(-1);

        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest());
    }

}