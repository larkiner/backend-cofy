package com.cafeteria.api.menu;

import com.cafeteria.api.config.CatalogoCacheControl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint público: el menú es visible sin iniciar sesión
 * (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final CatalogoCacheControl catalogoCacheControl;

    @GetMapping
    public ResponseEntity<List<MenuItem>> listarMenu() {
        return catalogoCacheControl.ok(menuService.obtenerMenu());
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<MenuItem>> listarPorCategoria(@PathVariable Long categoriaId) {
        return catalogoCacheControl.ok(menuService.obtenerMenuPorCategoria(categoriaId));
    }
}
