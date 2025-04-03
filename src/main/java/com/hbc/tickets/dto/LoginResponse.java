package com.hbc.tickets.dto;

import com.hbc.tickets.model.Role;

public class LoginResponse {

    private String token;
    private Role role;

    public LoginResponse(String token, Role role) {
        this.token = token;
        this.role = role;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
