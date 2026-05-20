package com.example.productapi.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProductRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {

        ValidatorFactory factory =
                Validation.buildDefaultValidatorFactory();

        validator = factory.getValidator();
    }

    @Test
    @DisplayName("1. ProductRequest valid")
    void testValidProductRequest() {

        ProductRequest request = new ProductRequest();

        request.setName("Sepatu Nike");
        request.setPrice(1000000.0);
        request.setStock(10);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("2. Nama produk kosong")
    void testNameBlank() {

        ProductRequest request = new ProductRequest();

        request.setName("");
        request.setPrice(1000000.0);
        request.setStock(10);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertEquals(
                "Nama produk tidak boleh kosong",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    @DisplayName("3. Harga kosong")
    void testPriceNull() {

        ProductRequest request = new ProductRequest();

        request.setName("Sepatu Nike");
        request.setPrice(null);
        request.setStock(10);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertEquals(
                "Harga tidak boleh kosong",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    @DisplayName("4. Harga kurang dari 1")
    void testPriceInvalid() {

        ProductRequest request = new ProductRequest();

        request.setName("Sepatu Nike");
        request.setPrice(0.0);
        request.setStock(10);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertEquals(
                "Harga harus lebih besar dari 0",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    @DisplayName("5. Stok kurang dari 0")
    void testStockInvalid() {

        ProductRequest request = new ProductRequest();

        request.setName("Sepatu Nike");
        request.setPrice(1000000.0);
        request.setStock(-1);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertEquals(
                "Stok minimal adalah 0",
                violations.iterator().next().getMessage()
        );
    }
}