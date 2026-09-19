package com.cafeteria.api.pedido.dto;

/** Datos mínimos que Stripe.js necesita para completar el pago en el navegador. */
public record StripePaymentIntentResponse(String clientSecret, String paymentIntentId) {
}
