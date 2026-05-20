package com.example.productapi.service;

import com.example.productapi.dto.request.OrderItemRequest;
import com.example.productapi.dto.request.OrderRequest;
import com.example.productapi.exception.ResourceNotFoundException;
import com.example.productapi.model.*;
import com.example.productapi.model.enums.OrderStatus;
import com.example.productapi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota pesanan tidak ditemukan dengan ID: " + id));

        // Proses hapus dari database (otomatis menghapus item di dalamnya jika diset CascadeType.ALL)
        orderRepository.delete(order);
    }
    @Transactional
    public Order createOrder(OrderRequest request) {
        // 1. Cari User
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(0.0);

        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0.0;

        // 2. Loop semua item yang dipesan
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produk ID " + itemReq.getProductId() + " tidak ditemukan"));

            // Cek kecukupan stok
            if (product.getStock() < itemReq.getQuantity()) {
                throw new IllegalArgumentException("Stok produk '" + product.getName() + "' tidak mencukupi");
            }

            // Potong stok produk
            product.setStock(product.getStock() - itemReq.getQuantity());
            productRepository.save(product);

            // Buat detail item order
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setPrice(product.getPrice());

            orderItems.add(orderItem);
            totalPrice += product.getPrice() * itemReq.getQuantity();
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(totalPrice);

        return orderRepository.save(order);
    }

    // Fungsi Konfirmasi Order oleh ADMIN (SUDAH DIPERBAIKI)
    @Transactional // 👈 Wajib ditambahkan untuk proses update data database
    public Order confirmOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order tidak ditemukan"));

        if (order.getStatus() == newStatus) {
            throw new IllegalArgumentException("Status order sudah " + newStatus);
        }

        order.setStatus(newStatus);
        orderRepository.saveAndFlush(order); // 👈 Memaksa perubahan tersimpan ke database saat ini juga

        // 👈 KUNCI UTAMA: Tarik ulang objek Order yang lengkap via JOIN FETCH agar tidak memicu Eror 500
        return orderRepository.findAll().stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElse(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}