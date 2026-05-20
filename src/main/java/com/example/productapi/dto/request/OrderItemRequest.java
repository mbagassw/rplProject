package com.example.productapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {

    @NotNull(message = "Product ID tidak boleh kosong")
    private Long productId;

    @NotNull(message = "Quantity tidak boleh kosong")
    @Min(value = 1, message = "Quantity minimal harus 1, tidak boleh 0 atau minus!")
    private Integer quantity;
}