package com.cafeteria.api.interno.pedido;

import com.cafeteria.api.interno.trabajador.TrabajadorAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PedidoInternoService {

    /** Transiciones que puede hacer el personal desde el tablero. */
    private static final Map<String, String> TRANSICIONES = Map.of(
            "PAGADO", "EN_PREPARACION",
            "EN_PREPARACION", "LISTO");

    /** Estados en los que un pedido puede entregarse al cliente. */
    private static final Set<String> ENTREGABLES =
            Set.of("PAGADO", "EN_PREPARACION", "LISTO");

    private final PedidoInternoRepository pedidoInternoRepository;
    private final PedidoEmpleadoVistaRepository pedidoEmpleadoVistaRepository;
    private final TrabajadorAuthRepository trabajadorAuthRepository;

    @Transactional(value = "empleadoTransactionManager", readOnly = true)
    public List<PedidoEmpleadoVista> tablero(String estado, Long sucursalId) {
        if (sucursalId != null && estado != null) {
            return pedidoEmpleadoVistaRepository
                    .findBySucursalIdAndEstadoOrderByFechaPedidoAsc(sucursalId, estado);
        }
        if (estado != null) {
            return pedidoEmpleadoVistaRepository.findByEstadoOrderByFechaPedidoAsc(estado);
        }
        if (sucursalId != null) {
            return pedidoEmpleadoVistaRepository.findBySucursalIdOrderByFechaPedidoDesc(sucursalId);
        }
        return pedidoEmpleadoVistaRepository.findAllByOrderByFechaPedidoDesc();
    }

    @Transactional("empleadoTransactionManager")
    public PedidoInterno cambiarEstado(Long pedidoId, String nuevoEstado) {
        PedidoInterno pedido = pedidoInternoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        String permitido = TRANSICIONES.get(pedido.getEstado());
        if (!nuevoEstado.equals(permitido)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transición no permitida: " + pedido.getEstado() + " -> " + nuevoEstado);
        }

        pedido.setEstado(nuevoEstado);
        return pedidoInternoRepository.save(pedido);
    }

    /** El cliente presenta su código en la sucursal y se le entrega el pedido. */
    @Transactional("empleadoTransactionManager")
    public PedidoInterno entregar(String codigoRetiro, String emailEmpleado) {
        PedidoInterno pedido = pedidoInternoRepository
                .findByCodigoRetiro(codigoRetiro.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Código de retiro no válido"));

        if (!ENTREGABLES.contains(pedido.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El pedido no se puede entregar (estado: " + pedido.getEstado() + ")");
        }

        Long trabajadorId = trabajadorAuthRepository
                .findByEmailIgnoreCase(emailEmpleado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Trabajador no encontrado"))
                .getId();

        pedido.setEstado("ENTREGADO");
        pedido.setTrabajadorId(trabajadorId);
        pedido.setFechaEntrega(LocalDateTime.now());
        return pedidoInternoRepository.save(pedido);
    }
}
