package com.cafeteria.api.menu;

import lombok.RequiredArgsConstructor;
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

    @GetMapping
    public List<MenuItem> listarMenu() {
        return menuService.obtenerMenu();
    }

    @GetMapping("/categoria/{categoriaId}")
    public List<MenuItem> listarPorCategoria(@PathVariable Long categoriaId) {
        return menuService.obtenerMenuPorCategoria(categoriaId);
    }
}
