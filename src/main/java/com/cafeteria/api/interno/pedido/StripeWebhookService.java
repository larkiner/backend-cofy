package com.cafeteria.api.interno.pedido;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Procesa eventos de Stripe con la firma del webhook validada. */
@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    private final PagoInternoRepository pagoInternoRepository;

    @Value("${app.stripe.webhook-secret:}")
    private String webhookSecret;

    @Transactional("empleadoTransactionManager")
    public void procesar(String payload, String firma) {
        if (webhookSecret.isBlank() || firma == null || firma.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Firma de Stripe inválida");
        }
        final Event evento;
        try {
            evento = Webhook.constructEvent(payload, firma, webhookSecret);
        } catch (SignatureVerificationException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Firma de Stripe inválida", ex);
        }

        if (!"payment_intent.succeeded".equals(evento.getType())
                && !"payment_intent.payment_failed".equals(evento.getType())) {
            return;
        }
        Object objeto = evento.getDataObjectDeserializer().getObject().orElse(null);
        if (!(objeto instanceof PaymentIntent intent)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Evento de Stripe inválido");
        }
        PagoInterno pago = pagoInternoRepository.findByReferenciaTransaccionForUpdate(intent.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pago de Stripe no encontrado"));

        if ("payment_intent.succeeded".equals(evento.getType()) && "PENDIENTE".equals(pago.getEstado())) {
            pago.setEstado("APROBADO");
        }
        if ("payment_intent.payment_failed".equals(evento.getType()) && "PENDIENTE".equals(pago.getEstado())) {
            pago.setEstado("RECHAZADO");
        }
    }
}
