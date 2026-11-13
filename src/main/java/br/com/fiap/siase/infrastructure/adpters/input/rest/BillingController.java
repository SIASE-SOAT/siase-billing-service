package br.com.fiap.siase.infrastructure.adpters.input.rest;

import br.com.fiap.siase.application.usecase.ConsultarBillingUseCase;
import br.com.fiap.siase.application.usecase.ProcessarWebhookMercadoPagoUseCase;
import br.com.fiap.siase.domain.model.Orcamento;

import br.com.fiap.siase.domain.model.Pagamento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Billing — Orçamentos e Pagamentos")
public class BillingController {

  private final ConsultarBillingUseCase consultar;
  private final ProcessarWebhookMercadoPagoUseCase webhook;

  public BillingController(ConsultarBillingUseCase consultar,
                           ProcessarWebhookMercadoPagoUseCase webhook) {
    this.consultar = consultar;
    this.webhook = webhook;
  }

  @GetMapping("/ordens/{osId}/orcamento")
  public Orcamento orcamentoDaOs(@PathVariable UUID osId) {
    return consultar.orcamentoDaOs(osId);
  }

  @GetMapping("/ordens/{osId}/pagamento")
  public Pagamento pagamentoDaOs(@PathVariable UUID osId) {
    return consultar.pagamentoDaOs(osId);
  }

  @GetMapping("/pagamentos/{id}")
  public Pagamento pagamentoPorId(@PathVariable UUID id) {
    return consultar.pagamentoPorId(id);
  }

  @PostMapping("/billing/webhook/mercadopago")
  @Operation(summary = "Webhook do Mercado Pago (confirmação de pagamento)")
  public ResponseEntity<Void> webhook(
          @RequestParam(required = false) String type,
          @RequestParam(required = false, name = "data.id") String dataId,
          @RequestParam(required = false) String id,
          @RequestBody(required = false) Map<String, Object> body) {

    String paymentId = extrairPaymentId(type, dataId, id, body);
    if (paymentId != null) {
      webhook.executar(paymentId);
    }
    return ResponseEntity.ok().build(); // MP exige 200 rápido
  }

  private String extrairPaymentId(String type, String dataId, String id, Map<String, Object> body) {
    if (dataId != null) return dataId;
    if ("payment".equals(type) && id != null) return id;
    if (body != null && "payment".equals(body.get("type")) && body.get("data") instanceof Map<?, ?> data) {
      Object pid = data.get("id");
      if (pid != null) return pid.toString();
    }
    return null;
  }
}
