package com.cafeteria.api.auth;

import com.cafeteria.api.auth.CredencialService.Credenciales;
import com.cafeteria.api.auth.dto.CambioPasswordRequest;
import com.cafeteria.api.cliente.ClienteRepository;
import com.cafeteria.api.interno.trabajador.Trabajador;
import com.cafeteria.api.interno.trabajador.TrabajadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Cambio de contraseña del usuario autenticado.
 *
 * El destino del UPDATE difiere por permisos de BD, no por capricho:
 * ROL_CLIENTE solo tiene UPDATE sobre password_hash (sin SELECT a la
 * tabla), por eso el cliente se actualiza con una query dedicada; el
 * personal sí puede cargar y guardar su entidad.
 */
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final CredencialService credencialService;
    private final InvalidacionSesionService invalidacionSesionService;
    private final ClienteRepository clienteRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final PasswordEncoder passwordEncoder;

    public void cambiarPassword(String email, CambioPasswordRequest request) {
        Credenciales creds = credencialService.buscarPorEmail(email)
                .orElseThrow(CredencialService::credencialesInvalidas);
        credencialService.verificarPassword(request.passwordActual(), creds);

        PoliticaPassword.validar(request.passwordNueva());

        String nuevoHash = passwordEncoder.encode(request.passwordNueva());

        if (CredencialService.ROL_CLIENTE.equals(creds.rol())) {
            clienteRepository.actualizarPasswordHash(creds.id(), nuevoHash);
        } else {
            Trabajador trabajador = trabajadorRepository.findById(creds.id())
                    .orElseThrow(CredencialService::credencialesInvalidas);
            trabajador.setPasswordHash(nuevoHash);
            trabajadorRepository.save(trabajador);
        }

        // Al cambiar la contraseña, cualquier token emitido antes deja de
        // servir: se cierran todas las sesiones activas (incluida la actual).
        invalidacionSesionService.invalidarSesionesDe(creds.email());
    }
}
