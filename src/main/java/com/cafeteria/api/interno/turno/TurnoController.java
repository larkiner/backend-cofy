package com.cafeteria.api.interno.turno;

import com.cafeteria.api.interno.turno.dto.TurnoRequest;
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

import java.util.List;

/**
 * Turnos: crear (solo SUPERVISOR/ADMIN, ver SecurityConfig)
 * y consultar los propios (cualquier rol interno).
 */
@RestController
@RequestMapping("/api/interno/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    /** Turnos del trabajador autenticado. */
    @GetMapping("/mios")
    public List<Turno> misTurnos(Authentication authentication) {
        return turnoService.misTurnos(authentication.getName());
    }

    /** Asignar un turno (solo SUPERVISOR/ADMIN). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Turno crear(@Valid @RequestBody TurnoRequest request) {
        return turnoService.crear(request);
    }
}
