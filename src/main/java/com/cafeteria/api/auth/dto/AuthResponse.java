package com.cafeteria.api.auth.dto;

public record AuthResponse(
        String token,
        String email,
        String rol
) {
}
