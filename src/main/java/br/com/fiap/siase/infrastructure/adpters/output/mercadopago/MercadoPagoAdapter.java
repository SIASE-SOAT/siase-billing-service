package br.com.fiap.siase.infrastructure.adpters.output.mercadopago;

import br.com.fiap.siase.application.ports.MercadoPagoPort;
import br.com.fiap.siase.domain.model.Orcamento;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Integração com a API REST do Mercado Pago (java.net.http).
 *
 * - criarPagamentoPix → POST /v1/payments com payment_method_id=pix
 *   (retorna QR Code dinâmico em point_of_interaction.transaction_data)
 * - consultarPagamento → GET /v1/payments/{id}
 * - reembolsar → POST /v1/payments/{id}/refunds
 *
 * Em dev/teste sem token real, ativar `app.mercadopago.mock=true` retorna
 * respostas simuladas (permite demonstrar o fluxo sem conta MP).
 */
@Component
public class MercadoPagoAdapter implements MercadoPagoPort {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoAdapter.class);
    private static final String BASE = "https://api.mercadopago.com";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper mapper;
    private final String accessToken;
    private final boolean mock;

    public MercadoPagoAdapter(ObjectMapper mapper,
                              @Value("${app.mercadopago.access-token}") String accessToken,
                              @Value("${app.mercadopago.mock:false}") boolean mock) {
        this.mapper = mapper;
        this.accessToken = accessToken;
        this.mock = mock;
    }

    @Override
    public PagamentoPix criarPagamentoPix(Orcamento orcamento, BigDecimal valor, String callbackUrl) {
        if (mock) {
            String id = "pay-mock-" + orcamento.getId();
            return new PagamentoPix(id,
                    "000201mock-pix-copia-e-cola-" + id,
                    "bW9jay1xci1jb2Rl");
        }
        try {
            String body = mapper.writeValueAsString(java.util.Map.of(
                    "transaction_amount", valor,
                    "description", "Ordem de Serviço " + orcamento.getNumeroOs(),
                    "payment_method_id", "pix",
                    "external_reference", orcamento.getOsId().toString(),
                    "notification_url", callbackUrl + "/api/billing/webhook/mercadopago",
                    "payer", java.util.Map.of("email", "cliente@siase.local")));

            JsonNode resp = chamar(HttpRequest.newBuilder(URI.create(BASE + "/v1/payments"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .header("X-Idempotency-Key", orcamento.getId().toString())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build());

            JsonNode txData = resp.path("point_of_interaction").path("transaction_data");
            return new PagamentoPix(resp.get("id").asText(),
                    txData.path("qr_code").asText(null),
                    txData.path("qr_code_base64").asText(null));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao criar pagamento Pix no Mercado Pago", e);
        }
    }

    @Override
    public StatusMp consultarPagamento(String mpPaymentId) {
        if (mock) {
            return null; // em mock, o webhook é simulado pelo teste/demo
        }
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + "/v1/payments/" + mpPaymentId))
                    .header("Authorization", "Bearer " + accessToken).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 404) {
                return null;
            }
            if (resp.statusCode() != 200) {
                throw new IllegalStateException("MP respondeu " + resp.statusCode());
            }
            JsonNode j = mapper.readTree(resp.body());
            return new StatusMp(j.get("id").asText(), j.get("status").asText(),
                    j.get("transaction_amount").decimalValue(),
                    j.hasNonNull("external_reference") ? j.get("external_reference").asText() : null);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao consultar pagamento no Mercado Pago", e);
        }
    }

    @Override
    public void reembolsar(String mpPaymentId) {
        if (mock) {
            return;
        }
        try {
            chamar(HttpRequest.newBuilder(URI.create(BASE + "/v1/payments/" + mpPaymentId + "/refunds"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao reembolsar pagamento " + mpPaymentId, e);
        }
    }

    private JsonNode chamar(HttpRequest request) throws Exception {
        HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 300) {
            throw new IllegalStateException("Mercado Pago respondeu " + resp.statusCode()
                    + ": " + resp.body());
        }
        return mapper.readTree(resp.body());
    }
}

