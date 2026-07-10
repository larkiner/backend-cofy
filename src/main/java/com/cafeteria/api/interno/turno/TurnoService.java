package com.cafeteria.api.interno.turno;

import com.cafeteria.api.interno.trabajador.TrabajadorAuthRepository;
import com.cafeteria.api.interno.trabajador.TrabajadorRepository;
import com.cafeteria.api.interno.turno.dto.TurnoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Reglas de negocio de los turnos. El controller solo expone estos
 * métodos; toda la validación y el acceso a datos viven aquí.
 */
@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final TrabajadorAuthRepository trabajadorAuthRepository;

    @Transactional(value = "empleadoTransactionManager", readOnly = true)
    public List<Turno> misTurnos(String emailTrabajador) {
        Long trabajadorId = trabajadorAuthRepository
                .findByEmailIgnoreCase(emailTrabajador)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Trabajador no encontrado"))
                .getId();
        return turnoRepository.findByTrabajadorIdOrderByFechaDesc(trabajadorId);
    }

    @Transactional("empleadoTransactionManager")
    public Turno crear(TurnoRequest request) {
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
