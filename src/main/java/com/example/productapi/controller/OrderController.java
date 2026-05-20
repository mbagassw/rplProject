package com.example.productapi.controller;

import com.example.productapi.dto.request.OrderRequest;
import com.example.productapi.model.Order;
import com.example.productapi.model.enums.OrderStatus;
import com.example.productapi.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8000")
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // USER & ADMIN: Membuat order baru
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest request) {
        Order createdOrder = orderService.createOrder(request);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    // ADMIN: Lihat semua order yang masuk
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // USER: Lihat riwayat order milik sendiri berdasarkan User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);

        // Validasi: Jika data kosong, kembalikan response 404 berstruktur rapi
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "timestamp", LocalDateTime.now().toString(),
                    "status", HttpStatus.NOT_FOUND.value(),
                    "error", "Not Found",
                    "message", "Order tidak ditemukan untuk User ID: " + userId,
                    "path", "/api/orders/user/" + userId
            ));
        }

        // Jika data ditemukan, kembalikan status 200 OK beserta list data
        return ResponseEntity.ok(orders);
    }

    // ADMIN: Mengubah / Konfirmasi status order (PENDING, PAID, SHIPPED, REJECTED)
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> confirmOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        Order updatedOrder = orderService.confirmOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    // TAMBAHAN: ADMIN / USER untuk menghapus / membatalkan pesanan berdasarkan ID Nota
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id); // Memanggil method di layer service untuk proses hapus

            // Mengembalikan respons JSON sukses berstruktur rapi
            return ResponseEntity.ok().body(Map.of(
                    "timestamp", LocalDateTime.now().toString(),
                    "status", HttpStatus.OK.value(),
                    "message", "Pesanan dengan ID Nota #" + id + " berhasil dihapus secara permanen."
            ));
        } catch (Exception e) {
            // Jika ID Nota tidak ditemukan atau gagal dihapus
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "timestamp", LocalDateTime.now().toString(),
                    "status", HttpStatus.BAD_REQUEST.value(),
                    "error", "Bad Request",
                    "message", e.getMessage() == null ? "Gagal menghapus pesanan." : e.getMessage()
            ));
        }
    }
}