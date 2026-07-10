package com.cafeteria.api.interno.pedido;

import com.cafeteria.api.interno.pedido.dto.CambioEstadoRequest;
import com.cafeteria.api.interno.pedido.dto.EntregaRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Tablero de pedidos del personal (cualquier rol interno).
 * Flujo en barra: PAGADO -> EN_PREPARACION -> LISTO -> ENTREGADO.
 */
@RestController
@RequestMapping("/api/interno/pedidos")
@RequiredArgsConstructor
public class PedidoInternoController {

    private final PedidoInternoService pedidoInternoService;

    @GetMapping
    public List<PedidoEmpleadoVista> tablero(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long sucursalId) {
        return pedidoInternoService.tablero(estado, sucursalId);
    }

    @PutMapping("/{id}/estado")
    public PedidoInterno cambiarEstado(@PathVariable Long id,
                                       @Valid @RequestBody CambioEstadoRequest request) {
        return pedidoInternoService.cambiarEstado(id, request.estado());
    }

    @PostMapping("/entregar")
    public PedidoInterno entregar(Authentication auth,
                                  @Valid @RequestBody EntregaRequest request) {
        return pedidoInternoService.entregar(request.codigoRetiro(), auth.getName());
    }
}
