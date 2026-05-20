package com.example.productapi.model;

import com.example.productapi.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore; // 👈 Penting untuk diimport agar @JsonIgnore dikenali
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonIgnore // 👈 Berhasil mengamankan password dari response JSON
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}