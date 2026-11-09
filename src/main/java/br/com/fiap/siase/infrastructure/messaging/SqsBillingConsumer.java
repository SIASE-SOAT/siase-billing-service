package br.com.fiap.siase.infrastructure.messaging;

import br.com.fiap.siase.application.usecase.CancelarOrcamentoUC;
import br.com.fiap.siase.application.usecase.GerarOrcamentoUC;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsBillingConsumer {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final GerarOrcamentoUC gerarOrcamentoUC;
    private final CancelarOrcamentoUC cancelarOrcamentoUC;

    @Value("${aws.sqs.queue-url-billing}")
    private String billingQueueUrl;

    @Scheduled(fixedDelayString = "${aws.sqs.poll-interval-ms:5000}")
    public void consumir() {
        try {
            var messages = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                    .queueUrl(billingQueueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(5)
                    .build()).messages();

            for (var msg : messages) {
                try {
                    processar(msg.body());
                    sqsClient.deleteMessage(DeleteMessageRequest.builder()
                            .queueUrl(billingQueueUrl)
                            .receiptHandle(msg.receiptHandle())
                            .build());
                } catch (Exception e) {
                    log.error("Erro ao processar mensagem billing: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao consumir fila billing: {}", e.getMessage());
        }
    }

    private void processar(String body) throws Exception {
        JsonNode node = objectMapper.readTree(body);
        String tipo = node.get("tipo").asText();

        switch (tipo) {
            case "OS_CRIADA" -> gerarOrcamentoUC.executar(
                    node.get("osId").asText(),
                    node.get("osNumero").asText(),
                    node.get("clienteId").asText(),
                    node.get("clienteNome").asText(),
                    node.get("clienteEmail").asText(),
                    new BigDecimal(node.get("total").asText())
            );
            case "OS_CANCELADA" -> cancelarOrcamentoUC.executar(
                    node.get("osId").asText(),
                    node.has("motivo") ? node.get("motivo").asText() : "OS cancelada"
            );
            case "ORCAMENTO_RECUSADO" -> cancelarOrcamentoUC.executar(
                    node.get("osId").asText(), "Orçamento recusado pelo cliente"
            );
            default -> log.debug("Tipo de evento billing ignorado: {}", tipo);
        }
    }
}
