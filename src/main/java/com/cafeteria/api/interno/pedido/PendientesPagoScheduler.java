package com.cafeteria.api.interno.pedido;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Cancela periódicamente los pedidos que quedaron en PENDIENTE_PAGO sin que el
 * cliente llegara a iniciar un pago (carrito abandonado, pago que dio error
 * antes de registrarse, etc.). Sin este barrido esos pedidos se acumulan para
 * siempre en el tablero y en el historial del cliente.
 *
 * No toca los pedidos que ya tienen un pago registrado: pueden seguir
 * pagándose. Configuración: app.pedidos.pendiente-pago-minutos (por defecto
 * 30; 0 o negativo desactiva el barrido) y app.pedidos.expiracion-cada-ms
 * (por defecto 300000 = 5 min).
 */
@Component
public class PendientesPagoScheduler {

    private static final Logger log = LoggerFactory.getLogger(PendientesPagoScheduler.class);

    private final PedidoInternoRepository pedidoInternoRepository;
    private final long minutos;

    public PendientesPagoScheduler(
            PedidoInternoRepository pedidoInternoRepository,
            @Value("${app.pedidos.pendiente-pago-minutos:30}") long minutos) {
        this.pedidoInternoRepository = pedidoInternoRepository;
        this.minutos = minutos;
    }

    @Scheduled(fixedRateString = "${app.pedidos.expiracion-cada-ms:300000}",
            initialDelayString = "${app.pedidos.expiracion-cada-ms:300000}")
    public void cancelarPendientesAbandonados() {
        cancelarPendientesAnterioresA(LocalDateTime.now());
    }

    /** Separado para poder probarlo con una hora fija. */
    int cancelarPendientesAnterioresA(LocalDateTime ahora) {
        if (minutos <= 0) {
            return 0;
        }
        int cancelados = pedidoInternoRepository
                .cancelarPendientesSinPago(ahora.minusMinutes(minutos));
        if (cancelados > 0) {
            log.info("Pedidos PENDIENTE_PAGO sin pago cancelados por antigüedad (> {} min): {}",
                    minutos, cancelados);
        }
        return cancelados;
    }
}
