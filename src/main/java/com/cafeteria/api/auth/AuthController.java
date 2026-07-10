package com.cafeteria.api.auth;

import com.cafeteria.api.auth.dto.AuthResponse;
import com.cafeteria.api.auth.dto.CambioPasswordRequest;
import com.cafeteria.api.auth.dto.LoginRequest;
import com.cafeteria.api.auth.dto.RegistroRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegistroService registroService;
    private final SesionService sesionService;
    private final PasswordService passwordService;

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return registroService.registrar(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return sesionService.login(request);
    }

    /** Revoca el token actual: deja de servir aunque no haya expirado. */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        sesionService.cerrarSesion(authorization);
    }

    /** Cambio de contraseña del usuario autenticado (cliente o personal). */
    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(Authentication authentication,
                                @Valid @RequestBody CambioPasswordRequest request) {
        passwordService.cambiarPassword(authentication.getName(), request);
    }
}
