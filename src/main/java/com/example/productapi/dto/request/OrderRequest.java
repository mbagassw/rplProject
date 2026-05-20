package com.example.productapi.dto.request;

import jakarta.validation.Valid; // 👈 Wajib diimport untuk validasi berjenjang (nested validation)
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {

    @NotNull(message = "User ID tidak boleh kosong")
    private Long userId;

    @NotEmpty(message = "Item pesanan tidak boleh kosong")
    @Valid // 👈 Tambahkan ini agar Spring Boot memeriksa aturan @Min(1) di dalam list item!
    private List<OrderItemRequest> items;
}