package br.com.fiap.siase.infrastructure.web;

import br.com.fiap.siase.application.dto.input.MercadoPagoWebhookRequest;
import br.com.fiap.siase.application.dto.output.OrcamentoResponse;
import br.com.fiap.siase.application.dto.output.PagamentoResponse;
import br.com.fiap.siase.application.usecase.ConsultarBillingUC;
import br.com.fiap.siase.application.usecase.ProcessarWebhookMercadoPagoUC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BillingController {

    private final ConsultarBillingUC consultarBillingUC;
    private final ProcessarWebhookMercadoPagoUC webhookUC;

    @GetMapping("/ordens/{osId}/orcamento")
    @Tag(name = "Orçamentos")
    @Operation(summary = "Consultar orçamento da OS")
    public ResponseEntity<OrcamentoResponse> orcamento(@PathVariable String osId) {
        return ResponseEntity.ok(OrcamentoResponse.from(consultarBillingUC.orcamentoPorOsId(osId)));
    }

    @GetMapping("/ordens/{osId}/pagamento")
    @Tag(name = "Pagamentos")
    @Operation(summary = "Consultar pagamento da OS")
    public ResponseEntity<PagamentoResponse> pagamentoPorOs(@PathVariable String osId) {
        return ResponseEntity.ok(PagamentoResponse.from(consultarBillingUC.pagamentoPorOsId(osId)));
    }

    @GetMapping("/pagamentos/{id}")
    @Tag(name = "Pagamentos")
    @Operation(summary = "Consultar pagamento por ID")
    public ResponseEntity<PagamentoResponse> pagamentoPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(PagamentoResponse.from(consultarBillingUC.pagamentoPorId(id)));
    }

    @PostMapping("/billing/webhook/mercadopago")
    @Tag(name = "Webhook")
    @Operation(summary = "Webhook Mercado Pago (notificação de pagamento)")
    public ResponseEntity<Void> webhook(@RequestBody MercadoPagoWebhookRequest request) {
        if (request.data() != null && request.data().id() != null) {
            webhookUC.executar(request.data().id(), request.type());
        }
        return ResponseEntity.ok().build();
    }
}
