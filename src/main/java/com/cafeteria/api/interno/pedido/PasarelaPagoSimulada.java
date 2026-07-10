package com.cafeteria.api.interno.pedido;

import com.cafeteria.api.pedido.PasarelaPago;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * SIMULACIÓN de la pasarela de pagos: aprueba el pago pendiente en el
 * acto (como si la pasarela real hubiera respondido "aprobado").
 *
 * Vive en el paquete interno porque escribe sobre PAGOS a través del
 * datasource del personal ({@link PagoInternoRepository}). Implementa la
 * abstracción {@link PasarelaPago} que consume el lado cliente.
 */
@Service
@RequiredArgsConstructor
public class PasarelaPagoSimulada implements PasarelaPago {

    private final PagoInternoRepository pagoInternoRepository;

    @Override
    public void aprobarPago(Long pedidoId) {
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
    }
}
