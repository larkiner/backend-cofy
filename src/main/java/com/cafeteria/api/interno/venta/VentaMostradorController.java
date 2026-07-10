package com.cafeteria.api.interno.venta;

import com.cafeteria.api.interno.venta.dto.VentaMostradorRequest;
import com.cafeteria.api.interno.venta.dto.VentaMostradorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Venta de mostrador (cualquier rol interno: BARISTA, CAJERO,
 * SUPERVISOR o ADMIN pueden cobrar en caja).
 */
@RestController
@RequestMapping("/api/interno/pedidos/mostrador")
@RequiredArgsConstructor
public class VentaMostradorController {

    private final VentaMostradorService ventaMostradorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VentaMostradorResponse crear(Authentication auth,
                                        @Valid @RequestBody VentaMostradorRequest request) {
        return ventaMostradorService.crear(auth.getName(), request);
    }
}
