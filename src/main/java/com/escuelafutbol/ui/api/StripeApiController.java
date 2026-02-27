package com.escuelafutbol.ui.api;

import com.escuelafutbol.domain.service.StripeService;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
public class StripeApiController {

    private final StripeService stripeService;

    public StripeApiController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/payment-intent")
    public ResponseEntity<Map<String, String>> crearPaymentIntent(@RequestBody Map<String, Object> body) {
        try {
            long importe = Long.parseLong(body.get("importe").toString());
            String descripcion = body.get("descripcion").toString();
            String clientSecret = stripeService.crearPaymentIntent(importe, descripcion);
            return ResponseEntity.ok(Map.of("clientSecret", clientSecret));
        } catch (StripeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}