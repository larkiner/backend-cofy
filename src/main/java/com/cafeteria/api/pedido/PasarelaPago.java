package com.cafeteria.api.pedido;

/**
 * Abstracción de la pasarela de pagos (Inversión de Dependencias).
 *
 * {@link PedidoService} depende de esta interfaz, no de una pasarela
 * concreta. Hoy la implementa {@code PasarelaPagoSimulada} (aprueba el
 * pago en el acto); en producción se puede sustituir por una que llame
 * a una pasarela real —o reaccione a su webhook— sin tocar PedidoService.
 */
public interface PasarelaPago {

    /**
     * Aprueba el pago pendiente del pedido indicado. Al quedar el pago
     * en APROBADO, el trigger TRG_PAGOS_APROBADO de Oracle genera el
     * código de retiro y pasa el pedido a PAGADO.
     *
     * @throws org.springframework.web.server.ResponseStatusException
     *         si el pedido no tiene pago registrado o el pago ya fue procesado.
     */
    void aprobarPago(Long pedidoId);
}
