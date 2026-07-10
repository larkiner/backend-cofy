package com.cafeteria.api.auth;

import com.cafeteria.api.auth.dto.AuthResponse;
import com.cafeteria.api.auth.dto.CambioPasswordRequest;
import com.cafeteria.api.auth.dto.LoginRequest;
import com.cafeteria.api.auth.dto.RegistroRequest;
import com.cafeteria.api.cliente.Cliente;
import com.cafeteria.api.cliente.ClienteAuth;
import com.cafeteria.api.cliente.ClienteAuthRepository;
import com.cafeteria.api.cliente.ClienteRepository;
import com.cafeteria.api.interno.sucursal.SucursalInternaRepository;
import com.cafeteria.api.interno.trabajador.Trabajador;
import com.cafeteria.api.interno.trabajador.TrabajadorAuth;
import com.cafeteria.api.interno.trabajador.TrabajadorAuthRepository;
import com.cafeteria.api.interno.trabajador.TrabajadorRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * Registro y login unificados para clientes y personal.
 *
 * REGLA DE REGISTRO (pedida por el negocio):
 *  - Si la contraseña del registro es EXACTAMENTE la clave de empleado
 *    (app.registro.password-empleado) -> se crea como TRABAJADOR rol BARISTA.
 *  - Si es la clave de admin (app.registro.password-admin) -> TRABAJADOR rol ADMIN.
 *  - Cualquier otra contraseña -> CLIENTE normal.
 * En todos los casos la contraseña queda BCrypt-hasheada y puede
 * cambiarse después con PUT /api/auth/password.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ROL_CLIENTE = "CLIENTE";
    private static final String BEARER = "Bearer ";

    private final ClienteRepository clienteRepository;
    private final ClienteAuthRepository clienteAuthRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final TrabajadorAuthRepository trabajadorAuthRepository;
    private final SucursalInternaRepository sucursalInternaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${app.registro.password-empleado}")
    private String passwordEmpleado;

    @Value("${app.registro.password-admin}")
    private String passwordAdmin;

    // ------------------------- REGISTRO -------------------------

    public AuthResponse registrar(RegistroRequest request) {
        String email = request.email().trim().toLowerCase();

        if (clienteAuthRepository.existsByEmailIgnoreCase(email)
                || trabajadorAuthRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una cuenta con ese email");
        }

        if (request.password().equals(passwordAdmin)) {
            return registrarTrabajador(request, email, "ADMIN");
        }
        if (request.password().equals(passwordEmpleado)) {
            return registrarTrabajador(request, email, "BARISTA");
        }
        return registrarCliente(request, email);
    }

    private AuthResponse registrarCliente(RegistroRequest request, String email) {
        Cliente cliente = new Cliente();
        cliente.setNombre(request.nombre().trim());
        cliente.setEmail(email);
        cliente.setTelefono(request.telefono());
        cliente.setPasswordHash(passwordEncoder.encode(request.password()));
        clienteRepository.save(cliente);

        return new AuthResponse(jwtService.generarToken(email, ROL_CLIENTE), email, ROL_CLIENTE);
    }

    private AuthResponse registrarTrabajador(RegistroRequest request, String email, String rol) {
        Long sucursalId = resolverSucursal(request.sucursalId());

        Trabajador trabajador = new Trabajador();
        trabajador.setNombre(request.nombre().trim());
        trabajador.setEmail(email);
        trabajador.setTelefono(request.telefono());
        trabajador.setPasswordHash(passwordEncoder.encode(request.password()));
        trabajador.setRol(rol);
        trabajador.setSucursalId(sucursalId);
        trabajadorRepository.save(trabajador);

        return new AuthResponse(jwtService.generarToken(email, rol), email, rol);
    }

    private Long resolverSucursal(Long sucursalId) {
        if (sucursalId != null) {
            if (!sucursalInternaRepository.existsById(sucursalId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La sucursal " + sucursalId + " no existe");
            }
            return sucursalId;
        }
        return sucursalInternaRepository.findFirstByEstadoOrderByIdAsc("ACTIVA")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "No hay sucursales activas para asignar al trabajador"))
                .getId();
    }

    // --------------------------- LOGIN --------------------------

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim();

        // 1) ¿Es un cliente?
        Optional<ClienteAuth> cliente = clienteAuthRepository.findByEmailIgnoreCase(email);
        if (cliente.isPresent()) {
            validarCredenciales(request.password(),
                    cliente.get().getPasswordHash(), cliente.get().getEstado());
            return new AuthResponse(
                    jwtService.generarToken(cliente.get().getEmail(), ROL_CLIENTE),
                    cliente.get().getEmail(), ROL_CLIENTE);
        }

        // 2) ¿Es personal interno?
        TrabajadorAuth trabajador = trabajadorAuthRepository.findByEmailIgnoreCase(email)
                .orElseThrow(AuthService::credencialesInvalidas);
        validarCredenciales(request.password(),
                trabajador.getPasswordHash(), trabajador.getEstado());
        return new AuthResponse(
                jwtService.generarToken(trabajador.getEmail(), trabajador.getRol()),
                trabajador.getEmail(), trabajador.getRol());
    }

    // -------------------------- LOGOUT ---------------------------

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

    private void validarCredenciales(String password, String hash, String estado) {
        if (!passwordEncoder.matches(password, hash)) {
            throw credencialesInvalidas();
        }
        if (!"ACTIVO".equals(estado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta está inactiva");
        }
    }

    // -------------------- CAMBIO DE CONTRASEÑA ------------------

    public void cambiarPassword(String email, String rol, CambioPasswordRequest request) {
        if (ROL_CLIENTE.equals(rol)) {
            ClienteAuth auth = clienteAuthRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(AuthService::credencialesInvalidas);
            if (!passwordEncoder.matches(request.passwordActual(), auth.getPasswordHash())) {
                throw credencialesInvalidas();
            }
            clienteRepository.actualizarPasswordHash(auth.getId(),
                    passwordEncoder.encode(request.passwordNueva()));
            return;
        }

        TrabajadorAuth auth = trabajadorAuthRepository.findByEmailIgnoreCase(email)
                .orElseThrow(AuthService::credencialesInvalidas);
        if (!passwordEncoder.matches(request.passwordActual(), auth.getPasswordHash())) {
            throw credencialesInvalidas();
        }
        Trabajador trabajador = trabajadorRepository.findById(auth.getId())
                .orElseThrow(AuthService::credencialesInvalidas);
        trabajador.setPasswordHash(passwordEncoder.encode(request.passwordNueva()));
        trabajadorRepository.save(trabajador);
    }

    /** Mensaje genérico: no revelar si el email existe o no. */
    private static ResponseStatusException credencialesInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
    }
}
