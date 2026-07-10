package com.cafeteria.api.cliente;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Endpoints del cliente autenticado. Requieren el header
 * "Authorization: Bearer <token>" (ver JwtAuthFilter).
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClientePerfilRepository clientePerfilRepository;

    /** Perfil del cliente autenticado (el email viene del token JWT). */
    @GetMapping("/me")
    public ClientePerfil miPerfil(Authentication authentication) {
        String email = authentication.getName();
        return clientePerfilRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Perfil no encontrado"));
    }
}
