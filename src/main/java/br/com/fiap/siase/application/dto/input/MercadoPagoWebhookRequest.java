package br.com.fiap.siase.application.dto.input;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MercadoPagoWebhookRequest(
        String action,
        @JsonProperty("api_version") String apiVersion,
        WebhookData data,
        String type
) {
    public record WebhookData(String id) {}
}
