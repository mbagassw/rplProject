package com.example.productapi.model;

import com.example.productapi.model.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({
            "password", "role", "authorities", "enabled",
            "accountNonExpired", "accountNonLocked", "credentialsNonExpired",
            "hibernateLazyInitializer", "handler" // 👈 Ditambahkan agar GET tidak Eror 500 saat data kosong/lazy
    })
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Double totalPrice;

    @Column(name = "order_date", nullable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // 👈 Tambahkan anotasi ini
    private LocalDateTime orderDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> orderItems;

    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDateTime.now();
    }
}