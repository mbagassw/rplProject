package com.example.productapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference; // 👈 Wajib diimport
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter // 👈 Gunakan @Getter & @Setter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference // 👈 Tambahkan ini (Menghentikan serialization balik ke atas)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price;
}