package com.cafeteria.api.interno.pedido;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint público exclusivamente para eventos firmados por Stripe. */
@RestController
@RequestMapping("/api/pagos/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeWebhookService stripeWebhookService;

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.OK)
    public void webhook(@RequestHeader(value = "Stripe-Signature", required = false) String firma,
                        @RequestBody String payload) {
        stripeWebhookService.procesar(payload, firma);
    }
}
