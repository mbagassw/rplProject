package com.example.productapi.dto.request;

// Import ini wajib agar IntelliJ tahu di mana letak Enum Role Anda
import com.example.productapi.model.enums.Role;

public class RegisterRequest {
    private String username;
    private String password;
    private Role role; // 👈 Tipe data diubah dari String menjadi Enum Role

    // Constructor Kosong (Wajib untuk Jackson Databind)
    public RegisterRequest() {
    }

    // Constructor Full (Sudah disesuaikan menggunakan Enum Role)
    public RegisterRequest(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getter dan Setter (Sudah disesuaikan menggunakan Enum Role)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}