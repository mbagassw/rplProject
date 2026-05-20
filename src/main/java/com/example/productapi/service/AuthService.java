package com.example.productapi.service;

import com.example.productapi.dto.request.LoginRequest;
import com.example.productapi.dto.request.RegisterRequest;
import com.example.productapi.exception.ResourceNotFoundException;
import com.example.productapi.model.User;
import com.example.productapi.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List; // 👈 Wajib diimport untuk list user

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public User login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Username salah atau tidak terdaftar"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("Password salah!");
        }

        return user;
    }

    public User register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username sudah dipakai");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

        return userRepository.save(user);
    }

    // 👈 TAMBAHKAN METHOD INI: Untuk mengambil semua daftar user
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 👈 TAMBAHKAN METHOD INI: Untuk mengambil satu user berdasarkan ID
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}