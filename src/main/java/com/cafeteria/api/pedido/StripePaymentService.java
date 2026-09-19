package com.cafeteria.api.pedido;

import com.cafeteria.api.cliente.ClienteAuthRepository;
import com.cafeteria.api.pedido.dto.StripePaymentIntentResponse;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Crea un PaymentIntent por pedido; la aprobación final llega por webhook. */
@Service
@RequiredArgsConstructor
public class StripePaymentService {

    private static final String PREFIJO_INTENT = "pi_";

    private final PedidoClienteVistaRepository pedidoClienteVistaRepository;
    private final ClienteAuthRepository clienteAuthRepository;
    private final PagoRepository pagoRepository;

    @Value("${app.stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${app.stripe.currency:cop}")
    private String currency;

    @Transactional("clienteTransactionManager")
    public StripePaymentIntentResponse crearIntent(String emailCliente, Long pedidoId) {
        validarConfiguracion();
        PedidoClienteVista pedido = pedidoDelCliente(emailCliente, pedidoId);
        if (!"PENDIENTE_PAGO".equals(pedido.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El pedido no está pendiente de pago (estado: " + pedido.getEstado() + ")");
        }

        var pagoExistente = pagoRepository.findByPedidoId(pedidoId);
        if (pagoExistente.isPresent()) {
            String referencia = pagoExistente.get().getReferenciaTransaccion();
            if (referencia == null || !referencia.startsWith(PREFIJO_INTENT)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El pedido ya tiene un pago registrado");
            }
            return recuperarIntent(referencia);
        }

        PaymentIntent intent = crearPaymentIntent(pedido, emailCliente);
        Pago pago = new Pago();
        pago.setPedidoId(pedidoId);
        pago.setMetodoPago("TARJETA");
        pago.setMonto(pedido.getTotal());
        pago.setReferenciaTransaccion(intent.getId());

        try {
            pagoRepository.saveAndFlush(pago);
        } catch (DataIntegrityViolationException ex) {
            // Dos clics simultáneos usan la misma clave de idempotencia en
            // Stripe. La fila única local decide cuál solicitud continúa.
            Pago existente = pagoRepository.findByPedidoId(pedidoId)
                    .orElseThrow(() -> ex);
            return recuperarIntent(existente.getReferenciaTransaccion());
        }
        return respuesta(intent);
    }

    private PaymentIntent crearPaymentIntent(PedidoClienteVista pedido, String emailCliente) {
        try {
            long monto = montoEnUnidadMenor(pedido.getTotal());
            var params = PaymentIntentCreateParams.builder()
                    .setAmount(monto)
                    .setCurrency(currency.toLowerCase())
                    .setReceiptEmail(emailCliente)
                    .addPaymentMethodType("card")
                    .putMetadata("pedido_id", pedido.getPedidoId().toString())
                    .build();
            var opciones = RequestOptions.builder()
                    .setIdempotencyKey("pedido-" + pedido.getPedidoId() + "-stripe")
                    .build();
            return clienteStripe().paymentIntents().create(params, opciones);
        } catch (StripeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No fue posible iniciar el pago con Stripe", ex);
        }
    }

    private StripePaymentIntentResponse recuperarIntent(String paymentIntentId) {
        if (paymentIntentId == null || !paymentIntentId.startsWith(PREFIJO_INTENT)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El pedido no tiene un pago de Stripe reutilizable");
        }
        try {
            return respuesta(clienteStripe().paymentIntents().retrieve(paymentIntentId));
        } catch (StripeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No fue posible recuperar el pago de Stripe", ex);
        }
    }

    private PedidoClienteVista pedidoDelCliente(String email, Long pedidoId) {
        PedidoClienteVista pedido = pedidoClienteVistaRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        Long clienteId = clienteAuthRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Cliente no encontrado"))
                .getId();
        if (!clienteId.equals(pedido.getClienteId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado");
        }
        return pedido;
    }

    private static long montoEnUnidadMenor(BigDecimal total) {
        try {
            // COP es una moneda de dos decimales para la API de Stripe.
            return total.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El total del pedido no tiene un monto válido para Stripe", ex);
        }
    }

    private StripeClient clienteStripe() {
        return new StripeClient(stripeSecretKey);
    }

    private void validarConfiguracion() {
        if (stripeSecretKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Los pagos con tarjeta no están configurados en este entorno");
        }
    }

    private static StripePaymentIntentResponse respuesta(PaymentIntent intent) {
        return new StripePaymentIntentResponse(intent.getClientSecret(), intent.getId());
    }
}
