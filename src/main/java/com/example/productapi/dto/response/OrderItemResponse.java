package com.example.productapi.dto.response;

import lombok.Data;

@Data
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
}