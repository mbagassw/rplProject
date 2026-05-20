package com.example.productapi.service;

import com.example.productapi.dto.request.ProductRequest;
import com.example.productapi.exception.ResourceNotFoundException;
import com.example.productapi.model.Product;
import com.example.productapi.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest request;

    @BeforeEach
    void setUp() {

        product = new Product(
                1L,
                "Sepatu Nike",
                1000000.0,
                10
        );

        request = new ProductRequest();
        request.setName("Sepatu Nike");
        request.setPrice(1000000.0);
        request.setStock(10);
    }

    @Test
    @DisplayName("1. Get All Products - Harus Mengembalikan Semua Produk")
    void testGetAllProducts() {

        when(productRepository.findAll())
                .thenReturn(Arrays.asList(product));

        var result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Sepatu Nike", result.get(0).getName());

        verify(productRepository,times(1))
                .findAll();
    }

    @Test
    @DisplayName("2. Get Product By Id - Sukses")
    void testGetProductByIdSuccess() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        Product result =
                productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Sepatu Nike",
                result.getName());

        verify(productRepository)
                .findById(1L);
    }

    @Test
    @DisplayName("3. Get Product By Id - Produk Tidak Ditemukan")
    void testGetProductByIdFailed() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(99L)
        );

        verify(productRepository)
                .findById(99L);
    }

    @Test
    @DisplayName("4. Create Product - Sukses")
    void testCreateProduct() {

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        Product result =
                productService.createProduct(request);

        assertNotNull(result);
        assertEquals("Sepatu Nike",
                result.getName());

        verify(productRepository)
                .save(any(Product.class));
    }

    @Test
    @DisplayName("5. Update Product - Sukses")
    void testUpdateProduct() {

        Product updated = new Product(
                1L,
                "Tas Adidas",
                500000.0,
                5
        );

        ProductRequest updateRequest =
                new ProductRequest();

        updateRequest.setName("Tas Adidas");
        updateRequest.setPrice(500000.0);
        updateRequest.setStock(5);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenReturn(updated);

        Product result =
                productService.updateProduct(
                        1L,
                        updateRequest
                );

        assertEquals(
                "Tas Adidas",
                result.getName()
        );

        assertEquals(
                500000.0,
                result.getPrice()
        );

        verify(productRepository)
                .save(any(Product.class));
    }

    @Test
    @DisplayName("6. Delete Product - Sukses")
    void testDeleteProduct() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        doNothing()
                .when(productRepository)
                .delete(product);

        productService.deleteProduct(1L);

        verify(productRepository)
                .delete(product);
    }
}