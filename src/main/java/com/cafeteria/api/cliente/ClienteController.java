package com.cafeteria.api.cliente;

import com.cafeteria.api.cliente.dto.ActualizarClienteRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints del cliente autenticado. Requieren el header
 * "Authorization: Bearer <token>" (ver JwtAuthFilter).
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    /** Perfil del cliente autenticado (el email viene del token JWT). */
    @GetMapping("/me")
    public ClientePerfil miPerfil(Authentication authentication) {
        return clienteService.miPerfil(authentication.getName());
    }

    /** Actualiza los datos editables (nombre y teléfono) del cliente autenticado. */
    @PutMapping("/me")
    public ClientePerfil actualizarDatos(Authentication authentication,
                                         @Valid @RequestBody ActualizarClienteRequest request) {
        return clienteService.actualizarDatos(authentication.getName(), request);
    }
}
