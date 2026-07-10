package com.cafeteria.api.interno.trabajador;

import com.cafeteria.api.interno.trabajador.dto.CrearTrabajadorRequest;
import com.cafeteria.api.interno.trabajador.dto.TrabajadorCreadoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Perfil del trabajador autenticado y gestión de personal. */
@RestController
@RequestMapping("/api/interno")
@RequiredArgsConstructor
public class TrabajadorController {

    private final TrabajadorPerfilRepository trabajadorPerfilRepository;
    private final TrabajadorAdminService trabajadorAdminService;

    @GetMapping("/me")
    public TrabajadorPerfil miPerfil(Authentication authentication) {
        return trabajadorPerfilRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Perfil no encontrado"));
    }

    /**
     * Directorio de trabajadores (solo SUPERVISOR/ADMIN, ver SecurityConfig).
     * Lo usa el panel interno para asignar turnos.
     */
    @GetMapping("/trabajadores")
    public List<TrabajadorPerfil> listar() {
        return trabajadorPerfilRepository.findAllByOrderByNombreAsc();
    }

    /** Alta de personal: SOLO ADMIN (restricción en SecurityConfig). */
    @PostMapping("/trabajadores")
    @ResponseStatus(HttpStatus.CREATED)
    public TrabajadorCreadoResponse crear(@Valid @RequestBody CrearTrabajadorRequest request) {
        return trabajadorAdminService.crear(request);
    }
}
