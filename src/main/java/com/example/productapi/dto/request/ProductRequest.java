package com.example.productapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "Nama produk tidak boleh kosong")
    private String name;

    @NotNull(message = "Harga tidak boleh kosong")
    @Min(value = 1, message = "Harga harus lebih besar dari 0")
    private Double price;

    @NotNull(message = "Stok tidak boleh kosong")
    @Min(value = 0, message = "Stok minimal adalah 0")
    private Integer stock;
}