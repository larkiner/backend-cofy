package com.cafeteria.api.interno.producto;

import com.cafeteria.api.interno.producto.dto.CategoriaRequest;
import com.cafeteria.api.interno.producto.dto.ProductoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Gestión del inventario: solo la usa el rol ADMIN (ver SecurityConfig). */
@Service
@RequiredArgsConstructor
public class ProductoAdminService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    // ----------------------- PRODUCTOS -----------------------

    @Transactional(value = "empleadoTransactionManager", readOnly = true)
    public List<Producto> listarProductos() {
        return productoRepository.findAllByOrderByCategoriaIdAscNombreAsc();
    }

    @Transactional("empleadoTransactionManager")
    public Producto crearProducto(ProductoRequest request) {
        validarCategoria(request.categoriaId());

        Producto producto = new Producto();
        aplicar(producto, request);
        return productoRepository.save(producto);
    }

    @Transactional("empleadoTransactionManager")
    public Producto actualizarProducto(Long id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado"));
        validarCategoria(request.categoriaId());

        aplicar(producto, request);
        return productoRepository.save(producto);
    }

    // ----------------------- CATEGORÍAS ----------------------

    @Transactional(value = "empleadoTransactionManager", readOnly = true)
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAllByOrderByNombreAsc();
    }

    @Transactional("empleadoTransactionManager")
    public Categoria crearCategoria(CategoriaRequest request) {
        if (categoriaRepository.existsByNombreIgnoreCase(request.nombre().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una categoría con ese nombre");
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(request.nombre().trim());
        categoria.setDescripcion(request.descripcion());
        return categoriaRepository.save(categoria);
    }

    // ----------------------------------------------------------

    private void validarCategoria(Long categoriaId) {
        if (!categoriaRepository.existsById(categoriaId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La categoría " + categoriaId + " no existe");
        }
    }

    private static void aplicar(Producto producto, ProductoRequest request) {
        producto.setNombre(request.nombre().trim());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
        producto.setCategoriaId(request.categoriaId());
        producto.setDisponible(request.disponible() != null ? request.disponible() : "S");
        producto.setImagenUrl(request.imagenUrl());
    }
}
