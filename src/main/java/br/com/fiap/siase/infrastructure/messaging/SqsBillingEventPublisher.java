package br.com.fiap.siase.infrastructure.messaging;

import br.com.fiap.siase.domain.port.BillingEventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsBillingEventPublisher implements BillingEventPublisherPort {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-url-os-updates}")
    private String osUpdatesQueueUrl;

    @Value("${aws.sqs.queue-url-execution}")
    private String executionQueueUrl;

    @Override
    public void publicarOrcamentoGerado(String osNumero, String osId) {
        publicar(osUpdatesQueueUrl, Map.of(
                "tipo", "ORCAMENTO_GERADO",
                "osNumero", osNumero,
                "osId", osId
        ));
        log.info("Evento ORCAMENTO_GERADO publicado para OS {}", osNumero);
    }

    @Override
    public void publicarPagamentoConfirmado(String osNumero, String osId,
                                             String valor, String formaPagamento) {
        var payload = Map.of(
                "tipo", "PAGAMENTO_CONFIRMADO",
                "osNumero", osNumero,
                "osId", osId,
                "valor", valor,
                "formaPagamento", formaPagamento != null ? formaPagamento : ""
        );
        publicar(osUpdatesQueueUrl, payload);
        log.info("Evento PAGAMENTO_CONFIRMADO publicado para OS {}", osNumero);
    }

    @Override
    public void publicarPagamentoFalhou(String osNumero, String osId) {
        publicar(osUpdatesQueueUrl, Map.of(
                "tipo", "PAGAMENTO_FALHOU",
                "osNumero", osNumero,
                "osId", osId
        ));
        log.warn("Evento PAGAMENTO_FALHOU publicado para OS {}", osNumero);
    }

    private void publicar(String queueUrl, Object payload) {
        try {
            String body = objectMapper.writeValueAsString(payload);
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(body)
                    .build());
        } catch (Exception e) {
            log.error("Falha ao publicar evento SQS: {}", e.getMessage(), e);
        }
    }
}
