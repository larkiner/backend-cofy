package com.cafeteria.api.pedido;

import com.cafeteria.api.cliente.ClienteAuthRepository;
import com.cafeteria.api.interno.pedido.PagoInterno;
import com.cafeteria.api.interno.pedido.PagoInternoRepository;
import com.cafeteria.api.menu.MenuItem;
import com.cafeteria.api.menu.MenuRepository;
import com.cafeteria.api.pedido.dto.CrearPedidoRequest;
import com.cafeteria.api.pedido.dto.ItemPedidoRequest;
import com.cafeteria.api.pedido.dto.PedidoCreadoResponse;
import com.cafeteria.api.pedido.dto.PedidoDetalleResponse;
import com.cafeteria.api.sucursal.SucursalActivaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final PagoRepository pagoRepository;
    private final PedidoClienteVistaRepository pedidoClienteVistaRepository;
    private final MenuRepository menuRepository;
    private final SucursalActivaRepository sucursalActivaRepository;
    private final ClienteAuthRepository clienteAuthRepository;
    // Repositorio del datasource INTERNO: solo lo usa la pasarela simulada
    private final PagoInternoRepository pagoInternoRepository;

    /**
     * Crea el pedido con su detalle en una sola transacción.
     * El precio SIEMPRE se toma del menú en la BD (nunca del cliente).
     */
    @Transactional("clienteTransactionManager")
    public PedidoCreadoResponse crear(String emailCliente, CrearPedidoRequest request) {
        Long clienteId = clienteIdDe(emailCliente);

        sucursalActivaRepository.findById(request.sucursalId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "La sucursal no existe o no está activa"));

        BigDecimal total = BigDecimal.ZERO;
        var detalles = new java.util.ArrayList<DetallePedido>();

        for (ItemPedidoRequest item : request.items()) {
            MenuItem producto = menuRepository.findById(item.productoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El producto " + item.productoId() + " no existe o no está disponible"));

            var detalle = new DetallePedido();
            detalle.setProductoId(producto.getProductoId());
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalles.add(detalle);

            total = total.add(producto.getPrecio()
                    .multiply(BigDecimal.valueOf(item.cantidad())));
        }

        Pedido pedido = new Pedido();
        pedido.setClienteId(clienteId);
        pedido.setSucursalId(request.sucursalId());
        pedido.setTotal(total);
        pedidoRepository.save(pedido);

        for (DetallePedido detalle : detalles) {
            detalle.setPedidoId(pedido.getId());
            detallePedidoRepository.save(detalle);
        }

        return new PedidoCreadoResponse(pedido.getId(), total, "PENDIENTE_PAGO");
    }

    /** Registra el pago en línea del pedido (queda PENDIENTE). */
    @Transactional("clienteTransactionManager")
    public void pagar(String emailCliente, Long pedidoId, String metodo) {
        PedidoClienteVista pedido = pedidoDelCliente(emailCliente, pedidoId);

        if (!"PENDIENTE_PAGO".equals(pedido.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El pedido no está pendiente de pago (estado: " + pedido.getEstado() + ")");
        }
        if (pagoRepository.existsByPedidoId(pedidoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El pedido ya tiene un pago registrado");
        }

        Pago pago = new Pago();
        pago.setPedidoId(pedidoId);
        pago.setMetodoPago(metodo);
        pago.setMonto(pedido.getTotal());
        pagoRepository.save(pago);
    }

    /**
     * SIMULACIÓN de la pasarela de pagos: aprueba el pago pendiente.
     * En producción esto sería un webhook que llama la pasarela real.
     * Al aprobarse, el trigger TRG_PAGOS_APROBADO de Oracle genera el
     * código de retiro y pasa el pedido a PAGADO.
     */
    public PedidoClienteVista confirmarPago(String emailCliente, Long pedidoId) {
        pedidoDelCliente(emailCliente, pedidoId); // valida propiedad

        PagoInterno pago = pagoInternoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "El pedido no tiene un pago registrado"));

        if (!"PENDIENTE".equals(pago.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El pago ya fue procesado (estado: " + pago.getEstado() + ")");
        }

        pago.setEstado("APROBADO");
        pago.setReferenciaTransaccion("SIM-" + UUID.randomUUID());
        pagoInternoRepository.save(pago);

        // Releer: el trigger ya generó el código de retiro
        return pedidoClienteVistaRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    }

    @Transactional(value = "clienteTransactionManager", readOnly = true)
    public List<PedidoClienteVista> misPedidos(String emailCliente) {
        return pedidoClienteVistaRepository
                .findByClienteIdOrderByFechaPedidoDesc(clienteIdDe(emailCliente));
    }

    @Transactional(value = "clienteTransactionManager", readOnly = true)
    public PedidoDetalleResponse detalle(String emailCliente, Long pedidoId) {
        PedidoClienteVista pedido = pedidoDelCliente(emailCliente, pedidoId);
        return new PedidoDetalleResponse(pedido,
                detallePedidoRepository.findByPedidoId(pedidoId));
    }

    // ------------------------------------------------------------

    private Long clienteIdDe(String email) {
        return clienteAuthRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Cliente no encontrado"))
                .getId();
    }

    /** Devuelve el pedido solo si pertenece al cliente autenticado. */
    private PedidoClienteVista pedidoDelCliente(String email, Long pedidoId) {
        PedidoClienteVista pedido = pedidoClienteVistaRepository.findById(pedidoId)
                .orElseThrow(PedidoService::pedidoNoEncontrado);
        if (!pedido.getClienteId().equals(clienteIdDe(email))) {
            throw pedidoNoEncontrado(); // 404: no revelar pedidos ajenos
        }
        return pedido;
    }

    private static ResponseStatusException pedidoNoEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado");
    }
}
