package com.cafeteria.api.interno.producto;

import com.cafeteria.api.interno.producto.dto.CategoriaRequest;
import com.cafeteria.api.interno.producto.dto.ProductoRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Inventario (productos y categorías). SOLO rol ADMIN:
 * la restricción está en SecurityConfig.
 */
@RestController
@RequestMapping("/api/interno")
@RequiredArgsConstructor
public class ProductoAdminController {

    private final ProductoAdminService productoAdminService;

    // ----------------------- PRODUCTOS -----------------------

    @GetMapping("/productos")
    public List<Producto> listarProductos() {
        return productoAdminService.listarProductos();
    }

    @PostMapping("/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crearProducto(@Valid @RequestBody ProductoRequest request) {
        return productoAdminService.crearProducto(request);
    }

    @PutMapping("/productos/{id}")
    public Producto actualizarProducto(@PathVariable Long id,
                                       @Valid @RequestBody ProductoRequest request) {
        return productoAdminService.actualizarProducto(id, request);
    }

    // ----------------------- CATEGORÍAS ----------------------

    @GetMapping("/categorias")
    public List<Categoria> listarCategorias() {
        return productoAdminService.listarCategorias();
    }

    @PostMapping("/categorias")
    @ResponseStatus(HttpStatus.CREATED)
    public Categoria crearCategoria(@Valid @RequestBody CategoriaRequest request) {
        return productoAdminService.crearCategoria(request);
    }
}
