package com.cafeteria.api.auth;

import com.cafeteria.api.auth.CredencialService.Credenciales;
import com.cafeteria.api.auth.dto.AuthResponse;
import com.cafeteria.api.auth.dto.LoginRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Ciclo de vida de la sesión: iniciarla (login) y cerrarla (logout).
 * No distingue entre cliente y personal: delega la búsqueda de
 * credenciales en {@link CredencialService}.
 */
@Service
@RequiredArgsConstructor
public class SesionService {

    private static final String BEARER = "Bearer ";

    private final CredencialService credencialService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim();

        Credenciales creds = credencialService.buscarPorEmail(email)
                .orElseThrow(CredencialService::credencialesInvalidas);
        credencialService.verificarLogin(request.password(), creds);

        return new AuthResponse(
                jwtService.generarToken(creds.email(), creds.rol()),
                creds.email(), creds.rol());
    }

    /**
     * Revoca el token actual (jti) hasta su expiración natural. Como las
     * sesiones son STATELESS no hay nada que borrar del servidor: solo
     * se marca ese JWT como inválido para que JwtAuthFilter lo rechace.
     */
    public void cerrarSesion(String header) {
        if (header == null || !header.startsWith(BEARER)) {
            throw noAutenticado();
        }

        Claims claims;
        try {
            claims = jwtService.validar(header.substring(BEARER.length()));
        } catch (JwtException e) {
            throw noAutenticado();
        }

        tokenBlacklistService.revocar(claims.getId(), claims.getExpiration().toInstant());
    }

    private static ResponseStatusException noAutenticado() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
    }
}
