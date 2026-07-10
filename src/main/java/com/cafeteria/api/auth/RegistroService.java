package com.cafeteria.api.auth;

import com.cafeteria.api.auth.dto.AuthResponse;
import com.cafeteria.api.auth.dto.RegistroRequest;
import com.cafeteria.api.cliente.Cliente;
import com.cafeteria.api.cliente.ClienteRepository;
import com.cafeteria.api.interno.sucursal.SucursalInternaRepository;
import com.cafeteria.api.interno.trabajador.Trabajador;
import com.cafeteria.api.interno.trabajador.TrabajadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Alta de cuentas. Única responsabilidad: crear clientes o trabajadores.
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
public class RegistroService {

    private final ClienteRepository clienteRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final SucursalInternaRepository sucursalInternaRepository;
    private final CredencialService credencialService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.registro.password-empleado}")
    private String passwordEmpleado;

    @Value("${app.registro.password-admin}")
    private String passwordAdmin;

    public AuthResponse registrar(RegistroRequest request) {
        String email = request.email().trim().toLowerCase();

        if (credencialService.emailRegistrado(email)) {
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

        String rol = CredencialService.ROL_CLIENTE;
        return new AuthResponse(jwtService.generarToken(email, rol), email, rol);
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
}
