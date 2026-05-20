package com.example.productapi.service;

import com.example.productapi.dto.request.OrderItemRequest;
import com.example.productapi.dto.request.OrderRequest;
import com.example.productapi.exception.ResourceNotFoundException;
import com.example.productapi.model.*;
import com.example.productapi.model.enums.OrderStatus;
import com.example.productapi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private User sampleUser;
    private Product sampleProduct;
    private OrderRequest sampleRequest;

    @BeforeEach
    void setUp() {
        // Inisialisasi data tiruan (Mock Data) sebelum setiap pengujian dijalankan
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("testuser");

        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setName("Sepatu Olahraga");
        sampleProduct.setPrice(500000.0);
        sampleProduct.setStock(10);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        sampleRequest = new OrderRequest();
        sampleRequest.setUserId(1L);
        sampleRequest.setItems(Collections.singletonList(itemRequest));
    }

    // ==========================================
    // TC-01: PENGUJIAN JALUR 1 (SKENARIO SUKSES)
    // ==========================================
    @Test
    void testCreateOrder_Success() {
        // Given (Kondisi Prasyarat)
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        // Mocking simpan data, kembalikan objek order yang sudah diset komponennya
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(25L); // Tiruan ID yang dihasilkan database
            return order;
        });

        // When (Aksi yang diuji)
        Order result = orderService.createOrder(sampleRequest);

        // Then (Verifikasi Hasil Aktual vs Ekspektasi)
        assertNotNull(result);
        assertEquals(25L, result.getId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(1000000.0, result.getTotalPrice()); // 500.000 * 2 item = 1.000.000
        assertEquals(8, sampleProduct.getStock()); // Stok harus terpotong (10 - 2 = 8)

        // Verifikasi bahwa repositori dipanggil dengan benar
        verify(productRepository, times(1)).save(sampleProduct);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    // ==========================================
    // TC-02: PENGUJIAN JALUR 2 (PRODUK TIDAK ADA)
    // ==========================================
    @Test
    void testCreateOrder_ProductNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        // Simulasikan produk tidak ditemukan di database (ID 999)
        sampleRequest.getItems().get(0).setProductId(999L);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(sampleRequest);
        });

        assertEquals("Produk ID 999 tidak ditemukan", exception.getMessage());

        // Pastikan proses simpan order dibatalkan
        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    // ==========================================
    // TC-03: PENGUJIAN JALUR 3 (STOK HABIS / KURANG)
    // ==========================================
    @Test
    void testCreateOrder_InsufficientStock() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        // Minta beli 15 item, sedangkan stok di database diset hanya ada 10
        sampleRequest.getItems().get(0).setQuantity(15);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(sampleRequest);
        });

        assertEquals("Stok produk 'Sepatu Olahraga' tidak mencukupi", exception.getMessage());
        assertEquals(10, sampleProduct.getStock()); // Pastikan stok aman dan tidak ikut berkurang

        // Pastikan tidak ada data yang disimpan ke database akibat eror pembatalan transaksi
        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    // ==========================================
    // PENGUJIAN METODE TAMBAHAN: DELETE ORDER
    // ==========================================
    @Test
    void testDeleteOrder_Success() {
        Order sampleOrder = new Order();
        sampleOrder.setId(25L);

        when(orderRepository.findById(25L)).thenReturn(Optional.of(sampleOrder));

        assertDoesNotThrow(() -> orderService.deleteOrder(25L));
        verify(orderRepository, times(1)).delete(sampleOrder);
    }
}