package com.cafeteria.api.menu;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public List<MenuItem> obtenerMenu() {
        return menuRepository.findAllByOrderByCategoriaAscNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<MenuItem> obtenerMenuPorCategoria(Long categoriaId) {
        return menuRepository.findByCategoriaIdOrderByNombreAsc(categoriaId);
    }
}
