package com.cafeteria.api.sucursal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoint público: sucursales activas para elegir dónde retirar. */
@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalActivaRepository sucursalActivaRepository;

    @GetMapping
    public List<SucursalActiva> listar() {
        return sucursalActivaRepository.findAll();
    }
}
