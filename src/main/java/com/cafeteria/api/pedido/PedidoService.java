package com.cafeteria.api.pedido;

import com.cafeteria.api.cliente.ClienteAuthRepository;
import com.cafeteria.api.menu.MenuItem;
import com.cafeteria.api.menu.MenuRepository;
import com.cafeteria.api.pedido.dto.CrearPedidoRequest;
import com.cafeteria.api.pedido.dto.ItemPedidoRequest;
import com.cafeteria.api.pedido.dto.PedidoCreadoResponse;
import com.cafeteria.api.pedido.dto.PedidoDetalleResponse;
import com.cafeteria.api.sucursal.SucursalActivaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    // Abstracción de la pasarela de pagos (DIP): PedidoService no sabe
    // si detrás hay una simulación o una pasarela real.
    private final PasarelaPago pasarelaPago;
    private final StripePaymentService stripePaymentService;

    /**
     * Si false, el cliente NO puede autoconfirmar su pago (endpoint solo
     * de desarrollo). En producción debe ir en false: la aprobación real
     * llega por webhook de la pasarela, no desde el cliente.
     */
    @Value("${app.pagos.simulacion-habilitada:false}")
    private boolean simulacionHabilitada;

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

        // Una sola consulta para todos los ítems (en vez de una por ítem):
        // el precio se sigue releyendo siempre del menú en la BD, solo que
        // en lote.
        List<Long> productoIds = request.items().stream()
                .map(ItemPedidoRequest::productoId)
                .distinct()
                .toList();
        Map<Long, MenuItem> menuPorProducto = menuRepository.findAllById(productoIds).stream()
                .collect(Collectors.toMap(MenuItem::getProductoId, Function.identity()));

        BigDecimal total = BigDecimal.ZERO;
        var detalles = new java.util.ArrayList<DetallePedido>();

        for (ItemPedidoRequest item : request.items()) {
            MenuItem producto = menuPorProducto.get(item.productoId());
            if (producto == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El producto " + item.productoId() + " no existe o no está disponible");
            }

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
        try {
            // UQ_PAGOS_PEDIDO es la última línea de defensa cuando dos pestañas
            // intentan pagar el mismo pedido a la vez. Forzamos el INSERT aquí
            // para devolver un 409 útil, en lugar de un 500 al cerrar la
            // transacción.
            pagoRepository.saveAndFlush(pago);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El pedido ya tiene un pago registrado", ex);
        }
    }

    /**
     * Confirma el pago del pedido delegando en la pasarela (DIP). Al
     * aprobarse, el trigger TRG_PAGOS_APROBADO de Oracle genera el código
     * de retiro y pasa el pedido a PAGADO; por eso se relee al final.
     */
    public PedidoClienteVista confirmarPago(String emailCliente, Long pedidoId) {
        if (!simulacionHabilitada) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La confirmación de pago simulada está deshabilitada en este entorno");
        }

        pedidoDelCliente(emailCliente, pedidoId); // valida propiedad

        pasarelaPago.aprobarPago(pedidoId);

        // Releer: el trigger ya generó el código de retiro
        return pedidoClienteVistaRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    }

    /** Inicia el cobro con Stripe; la aprobación llega después por webhook. */
    public com.cafeteria.api.pedido.dto.StripePaymentIntentResponse iniciarPagoStripe(
            String emailCliente, Long pedidoId) {
        return stripePaymentService.crearIntent(emailCliente, pedidoId);
    }

    @Transactional(value = "clienteTransactionManager", readOnly = true)
    public List<PedidoClienteVista> misPedidos(String emailCliente) {
        return pedidoClienteVistaRepository
                .findByClienteIdOrderByFechaPedidoDesc(clienteIdDe(emailCliente));
    }

    /**
     * Historial de compras del cliente: solo los pedidos que llegó a pagar
     * (se excluyen los que quedaron en PENDIENTE_PAGO).
     */
    @Transactional(value = "clienteTransactionManager", readOnly = true)
    public List<PedidoClienteVista> historialCompras(String emailCliente) {
        return pedidoClienteVistaRepository
                .findByClienteIdAndEstadoNotOrderByFechaPedidoDesc(
                        clienteIdDe(emailCliente), "PENDIENTE_PAGO");
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
