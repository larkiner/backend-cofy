package com.cafeteria.api.auth;

import com.cafeteria.api.auth.dto.AuthResponse;
import com.cafeteria.api.auth.dto.RegistroRequest;
import com.cafeteria.api.cliente.Cliente;
import com.cafeteria.api.cliente.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Registro público: crea SIEMPRE un cliente. El personal (empleado/admin)
 * se da de alta desde el panel interno por un ADMIN
 * (POST /api/interno/trabajadores), no por auto-registro.
 */
@Service
@RequiredArgsConstructor
public class RegistroService {

    private final ClienteRepository clienteRepository;
    private final CredencialService credencialService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse registrar(RegistroRequest request) {
        String email = request.email().trim().toLowerCase();

        if (credencialService.emailRegistrado(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una cuenta con ese email");
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(request.nombre().trim());
        cliente.setEmail(email);
        cliente.setTelefono(request.telefono());
        cliente.setPasswordHash(passwordEncoder.encode(request.password()));
        clienteRepository.save(cliente);

        String rol = CredencialService.ROL_CLIENTE;
        return new AuthResponse(jwtService.generarToken(email, rol), email, rol);
    }
}
