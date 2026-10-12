package com.cafeteria.api.sucursal;

import com.cafeteria.api.config.CatalogoCacheControl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoint público: sucursales activas para elegir dónde retirar. */
@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalService sucursalService;
    private final CatalogoCacheControl catalogoCacheControl;

    @GetMapping
    public ResponseEntity<List<SucursalActiva>> listar() {
        return catalogoCacheControl.ok(sucursalService.listarActivas());
    }
}
