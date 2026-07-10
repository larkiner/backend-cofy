package com.cafeteria.api.interno.turno;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByTrabajadorIdOrderByFechaDesc(Long trabajadorId);
}
