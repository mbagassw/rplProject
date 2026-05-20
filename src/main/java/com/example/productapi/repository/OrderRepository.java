package com.example.productapi.repository;

import com.example.productapi.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Amankan GET ALL: Tarik Order -> Tarik User -> Tarik Items -> Tarik Product di dalam Items
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product")
    List<Order> findAll();

    // Amankan GET BY USER ID dengan rute penarikan data yang sama lengkapnya
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE o.user.id = :userId")
    List<Order> findByUserId(@Param("userId") Long userId);
}