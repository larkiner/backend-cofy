package com.cafeteria.api.interno.trabajador;

import com.cafeteria.api.auth.CredencialService;
import com.cafeteria.api.interno.sucursal.SucursalInternaRepository;
import com.cafeteria.api.interno.trabajador.dto.CrearTrabajadorRequest;
import com.cafeteria.api.interno.trabajador.dto.TrabajadorCreadoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Alta de personal iniciada por un ADMIN. Sustituye al antiguo registro
 * por "contraseña mágica": el rol se asigna explícitamente y solo un ADMIN
 * puede llamar aquí (restricción en SecurityConfig). El registro público
 * (/api/auth/registro) crea siempre CLIENTE.
 */
@Service
@RequiredArgsConstructor
public class TrabajadorAdminService {

    private final TrabajadorRepository trabajadorRepository;
    private final SucursalInternaRepository sucursalInternaRepository;
    private final CredencialService credencialService;
    private final PasswordEncoder passwordEncoder;

    public TrabajadorCreadoResponse crear(CrearTrabajadorRequest request) {
        String email = request.email().trim().toLowerCase();

        if (credencialService.emailRegistrado(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una cuenta con ese email");
        }
        if (!sucursalInternaRepository.existsById(request.sucursalId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La sucursal " + request.sucursalId() + " no existe");
        }

        Trabajador trabajador = new Trabajador();
        trabajador.setNombre(request.nombre().trim());
        trabajador.setEmail(email);
        trabajador.setTelefono(request.telefono());
        trabajador.setPasswordHash(passwordEncoder.encode(request.password()));
        trabajador.setRol(request.rol());
        trabajador.setSucursalId(request.sucursalId());
        trabajadorRepository.save(trabajador);

        return new TrabajadorCreadoResponse(
                trabajador.getId(), trabajador.getNombre(),
                trabajador.getEmail(), trabajador.getRol(), trabajador.getSucursalId());
    }
}
