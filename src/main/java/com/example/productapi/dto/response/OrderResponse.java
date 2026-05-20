package com.example.productapi.dto.response;

import com.example.productapi.model.enums.OrderStatus;
import lombok.Data;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private String username;
    private OrderStatus status;
    private Double totalPrice;
    private List<OrderItemResponse> orderItems; // 👈 Sekarang OrderItemResponse ini sudah aman/tidak merah lagi
}