package com.cafeteria.api.interno.venta;

import com.cafeteria.api.interno.pedido.PedidoInterno;
import com.cafeteria.api.interno.pedido.PedidoInternoRepository;
import com.cafeteria.api.interno.producto.Producto;
import com.cafeteria.api.interno.producto.ProductoRepository;
import com.cafeteria.api.interno.trabajador.Trabajador;
import com.cafeteria.api.interno.trabajador.TrabajadorAuthRepository;
import com.cafeteria.api.interno.trabajador.TrabajadorRepository;
import com.cafeteria.api.interno.venta.dto.ItemVentaRequest;
import com.cafeteria.api.interno.venta.dto.VentaMostradorRequest;
import com.cafeteria.api.interno.venta.dto.VentaMostradorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Venta de mostrador: el cliente llega a la sucursal, el personal
 * arma el pedido y lo cobra al momento. Sin cliente registrado, sin
 * código de retiro que presentar después: el pedido nace ya PAGADO
 * y entra a la misma cola de preparación que los pedidos en línea.
 *
 * Requiere el Script 6 (CLIENTE_ID opcional + grants de INSERT
 * sobre PEDIDOS/DETALLE_PEDIDO/PAGOS para ROL_EMPLEADO).
 */
@Service
@RequiredArgsConstructor
public class VentaMostradorService {

    private final ProductoRepository productoRepository;
    private final TrabajadorAuthRepository trabajadorAuthRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final PedidoVentaRepository pedidoVentaRepository;
    private final DetallePedidoVentaRepository detallePedidoVentaRepository;
    private final PagoVentaRepository pagoVentaRepository;
    private final PedidoInternoRepository pedidoInternoRepository;

    public VentaMostradorResponse crear(String emailEmpleado, VentaMostradorRequest request) {
        Long sucursalId = sucursalDelEmpleado(emailEmpleado);

        // Una sola consulta para todos los ítems (en vez de una por ítem):
        // el precio se sigue releyendo siempre del inventario en la BD, solo
        // que en lote.
        List<Long> productoIds = request.items().stream()
                .map(ItemVentaRequest::productoId)
                .distinct()
                .toList();
        Map<Long, Producto> productosPorId = productoRepository.findAllById(productoIds).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        BigDecimal total = BigDecimal.ZERO;
        var detalles = new ArrayList<DetallePedidoVenta>();

        for (ItemVentaRequest item : request.items()) {
            Producto producto = productosPorId.get(item.productoId());
            if (producto == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El producto " + item.productoId() + " no existe");
            }
            if (!"S".equals(producto.getDisponible())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El producto " + producto.getNombre() + " no está disponible");
            }

            var detalle = new DetallePedidoVenta();
            detalle.setProductoId(producto.getId());
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalles.add(detalle);

            total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(item.cantidad())));
        }

        PedidoVenta pedido = new PedidoVenta();
        pedido.setSucursalId(sucursalId);
        pedido.setTotal(total);
        pedidoVentaRepository.save(pedido);

        for (DetallePedidoVenta detalle : detalles) {
            detalle.setPedidoId(pedido.getId());
            detallePedidoVentaRepository.save(detalle);
        }

        // Se cobró en persona: el pago nace APROBADO. Ese INSERT dispara
        // TRG_PAGOS_APROBADO igual que un UPDATE, generando el código de
        // retiro y pasando el pedido a PAGADO de inmediato.
        PagoVenta pago = new PagoVenta();
        pago.setPedidoId(pedido.getId());
        pago.setMetodoPago(request.metodoPago());
        pago.setMonto(total);
        pago.setEstado("APROBADO");
        pago.setReferenciaTransaccion("MOSTRADOR-" + pedido.getId());
        pagoVentaRepository.save(pago);

        PedidoInterno resultado = pedidoInternoRepository.findById(pedido.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo leer el pedido creado"));

        return new VentaMostradorResponse(
                resultado.getId(), resultado.getTotal(),
                resultado.getEstado(), resultado.getCodigoRetiro());
    }

    private Long sucursalDelEmpleado(String email) {
        Long trabajadorId = trabajadorAuthRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Trabajador no encontrado"))
                .getId();
        Trabajador trabajador = trabajadorRepository.findById(trabajadorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Trabajador no encontrado"));
        return trabajador.getSucursalId();
    }
}
