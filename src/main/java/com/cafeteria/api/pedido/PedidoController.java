package com.cafeteria.api.pedido;

import com.cafeteria.api.pedido.dto.CrearPedidoRequest;
import com.cafeteria.api.pedido.dto.PagoRequest;
import com.cafeteria.api.pedido.dto.PedidoCreadoResponse;
import com.cafeteria.api.pedido.dto.PedidoDetalleResponse;
import com.cafeteria.api.pedido.dto.StripePaymentIntentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Pedidos del cliente autenticado (rol CLIENTE).
 * Flujo: crear pedido -> pagar -> confirmar pago (pasarela simulada)
 *        -> aparece el código de retiro.
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoCreadoResponse crear(Authentication auth,
                                      @Valid @RequestBody CrearPedidoRequest request) {
        return pedidoService.crear(auth.getName(), request);
    }

    @GetMapping
    public List<PedidoClienteVista> misPedidos(Authentication auth) {
        return pedidoService.misPedidos(auth.getName());
    }

    /** Historial de compras: solo los pedidos que el cliente llegó a pagar. */
    @GetMapping("/historial")
    public List<PedidoClienteVista> historial(Authentication auth) {
        return pedidoService.historialCompras(auth.getName());
    }

    @GetMapping("/{id}")
    public PedidoDetalleResponse detalle(Authentication auth, @PathVariable Long id) {
        return pedidoService.detalle(auth.getName(), id);
    }

    @PostMapping("/{id}/pago")
    @ResponseStatus(HttpStatus.CREATED)
    public void pagar(Authentication auth, @PathVariable Long id,
                      @Valid @RequestBody PagoRequest request) {
        pedidoService.pagar(auth.getName(), id, request.metodo());
    }

    /** Crea o recupera el PaymentIntent asociado al pedido del cliente. */
    @PostMapping("/{id}/pago/stripe")
    public StripePaymentIntentResponse iniciarPagoStripe(Authentication auth, @PathVariable Long id) {
        return pedidoService.iniciarPagoStripe(auth.getName(), id);
    }

    /** Simula la confirmación de la pasarela: devuelve el pedido con su código. */
    @PostMapping("/{id}/pago/confirmar")
    public PedidoClienteVista confirmarPago(Authentication auth, @PathVariable Long id) {
        return pedidoService.confirmarPago(auth.getName(), id);
    }
}
