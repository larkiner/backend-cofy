package com.cafeteria.api.interno.turno;

import com.cafeteria.api.interno.trabajador.TrabajadorAuthRepository;
import com.cafeteria.api.interno.trabajador.TrabajadorRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Turnos: crear (solo SUPERVISOR/ADMIN, ver SecurityConfig)
 * y consultar los propios (cualquier rol interno).
 */
@RestController
@RequestMapping("/api/interno/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoRepository turnoRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final TrabajadorAuthRepository trabajadorAuthRepository;

    /** Turnos del trabajador autenticado. */
    @GetMapping("/mios")
    public List<Turno> misTurnos(Authentication authentication) {
        Long trabajadorId = trabajadorAuthRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Trabajador no encontrado"))
                .getId();
        return turnoRepository.findByTrabajadorIdOrderByFechaDesc(trabajadorId);
    }

    /** Asignar un turno (solo SUPERVISOR/ADMIN). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Turno crear(@Valid @RequestBody TurnoRequest request) {
        if (!request.horaFin().isAfter(request.horaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La hora de fin debe ser posterior a la de inicio");
        }
        if (!trabajadorRepository.existsById(request.trabajadorId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El trabajador " + request.trabajadorId() + " no existe");
        }

        Turno turno = new Turno();
        turno.setTrabajadorId(request.trabajadorId());
        turno.setFecha(request.fecha());
        turno.setHoraInicio(request.horaInicio());
        turno.setHoraFin(request.horaFin());
        return turnoRepository.save(turno);
    }
}
