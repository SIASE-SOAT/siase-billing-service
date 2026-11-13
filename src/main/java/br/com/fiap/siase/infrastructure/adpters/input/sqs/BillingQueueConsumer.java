package br.com.fiap.siase.infrastructure.adpters.input.sqs;

import br.com.fiap.siase.application.usecase.CompensarOsCanceladaUseCase;
import br.com.fiap.siase.application.usecase.GerarOrcamentoUseCase;
import br.com.fiap.siase.domain.model.EventoBilling;
import br.com.fiap.siase.infrastructure.observability.SqsCorrelationId;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Component
public class BillingQueueConsumer {

  private static final Logger log = LoggerFactory.getLogger(BillingQueueConsumer.class);

  private final SqsClient sqs;
  private final ObjectMapper mapper;
  private final GerarOrcamentoUseCase gerarOrcamento;
  private final CompensarOsCanceladaUseCase compensar;
  private final MeterRegistry metrics;
  private final String queueUrl;

  public BillingQueueConsumer(SqsClient sqs, ObjectMapper mapper,
                              GerarOrcamentoUseCase gerarOrcamento, CompensarOsCanceladaUseCase compensar,
                              MeterRegistry metrics,
                              @Value("${app.sqs.billing-url}") String queueUrl) {
    this.sqs = sqs;
    this.mapper = mapper;
    this.gerarOrcamento = gerarOrcamento;
    this.compensar = compensar;
    this.metrics = metrics;
    this.queueUrl = queueUrl;
  }

  @Scheduled(fixedDelayString = "${app.sqs.poll-delay-ms:5000}")
  public void poll() {
    var response = sqs.receiveMessage(ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(10)
            .waitTimeSeconds(20)
            .messageAttributeNames("All")
            .build());

    for (Message message : response.messages()) {
      try {
        SqsCorrelationId.mdcFrom(message);
        EventoBilling evento = mapper.readValue(message.body(), EventoBilling.class);
        dispatch(evento);
        sqs.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl).receiptHandle(message.receiptHandle()).build());
      } catch (Exception e) {
        log.error("Falha ao processar mensagem {}: {}", message.messageId(), e.getMessage(), e);
      } finally {
        MDC.clear();
      }
    }
  }

  private void dispatch(EventoBilling evento) {
    metrics.counter("siase_saga_eventos_total", "servico", "billing",
            "evento", evento.tipoEvento(), "direcao", "consumido").increment();
    switch (evento.tipoEvento()) {
      case EventoBilling.OS_CRIADA -> gerarOrcamento.executar(evento);
      case EventoBilling.OS_CANCELADA, EventoBilling.ORCAMENTO_RECUSADO -> compensar.executar(evento);
      default -> log.warn("Evento desconhecido ignorado: {}", evento.tipoEvento());
    }
  }
}
