package com.cafeteria.api.auth;

import com.cafeteria.api.cliente.ClienteAuth;
import com.cafeteria.api.cliente.ClienteAuthRepository;
import com.cafeteria.api.interno.trabajador.TrabajadorAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * Punto único de lectura y verificación de credenciales, sin importar si
 * la cuenta es de un cliente o de un trabajador. Centralizar aquí la
 * distinción entre ambos orígenes evita repetir el mismo {@code if} en
 * cada servicio de auth: para soportar un tercer tipo de cuenta bastaría
 * con tocar esta clase (Principio Abierto/Cerrado).
 */
@Service
@RequiredArgsConstructor
public class CredencialService {

    public static final String ROL_CLIENTE = "CLIENTE";
    private static final String ESTADO_ACTIVO = "ACTIVO";

    /** Vista uniforme de una cuenta autenticable (cliente o personal). */
    public record Credenciales(Long id, String email, String passwordHash, String estado, String rol) {
    }

    private final ClienteAuthRepository clienteAuthRepository;
    private final TrabajadorAuthRepository trabajadorAuthRepository;
    private final PasswordEncoder passwordEncoder;

    /** ¿Ya existe una cuenta (cliente o trabajador) con ese email? */
    public boolean emailRegistrado(String email) {
        return clienteAuthRepository.existsByEmailIgnoreCase(email)
                || trabajadorAuthRepository.existsByEmailIgnoreCase(email);
    }

    /** Busca la cuenta por email; primero entre clientes, luego entre personal. */
    public Optional<Credenciales> buscarPorEmail(String email) {
        Optional<ClienteAuth> cliente = clienteAuthRepository.findByEmailIgnoreCase(email);
        if (cliente.isPresent()) {
            ClienteAuth c = cliente.get();
            return Optional.of(new Credenciales(
                    c.getId(), c.getEmail(), c.getPasswordHash(), c.getEstado(), ROL_CLIENTE));
        }
        return trabajadorAuthRepository.findByEmailIgnoreCase(email)
                .map(t -> new Credenciales(
                        t.getId(), t.getEmail(), t.getPasswordHash(), t.getEstado(), t.getRol()));
    }

    /** Login: la contraseña debe coincidir y la cuenta estar activa. */
    public void verificarLogin(String passwordPlano, Credenciales creds) {
        verificarPassword(passwordPlano, creds);
        if (!ESTADO_ACTIVO.equals(creds.estado())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta está inactiva");
        }
    }

    /** Solo comprueba que la contraseña coincida (p. ej. al cambiarla). */
    public void verificarPassword(String passwordPlano, Credenciales creds) {
        if (!passwordEncoder.matches(passwordPlano, creds.passwordHash())) {
            throw credencialesInvalidas();
        }
    }

    /** Mensaje genérico: no revelar si el email existe o no. */
    public static ResponseStatusException credencialesInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
    }
}
