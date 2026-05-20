package com.example.productapi.controller;

import com.example.productapi.dto.request.LoginRequest;
import com.example.productapi.dto.request.RegisterRequest;
import com.example.productapi.model.User;
import com.example.productapi.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8000")
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // 1. ENDPOINT BARU: ADMIN/USER dapat melihat semua daftar user yang ada
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // 2. ENDPOINT BARU: Melihat detail 1 user berdasarkan ID
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = authService.getUserById(id);

        // Jika user tidak ditemukan, lempar response 404 (Memanfaatkan Handler Anda)
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "User dengan ID " + id + " tidak ditemukan"
            ));
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest request) {
        User user = authService.login(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}