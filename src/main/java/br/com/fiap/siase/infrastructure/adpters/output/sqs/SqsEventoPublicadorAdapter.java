package br.com.fiap.siase.infrastructure.adpters.output.sqs;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.domain.model.EventoBilling;
import br.com.fiap.siase.infrastructure.observability.SqsCorrelationId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class SqsEventoPublicadorAdapter implements EventoPublicadorPort {

  private static final Logger log = LoggerFactory.getLogger(SqsEventoPublicadorAdapter.class);

  private final SqsClient sqs;
  private final ObjectMapper mapper;
  private final String osUpdatesQueueUrl;

  public SqsEventoPublicadorAdapter(SqsClient sqs, ObjectMapper mapper,
                                    @Value("${app.sqs.os-updates-url}") String osUpdatesQueueUrl) {
    this.sqs = sqs;
    this.mapper = mapper;
    this.osUpdatesQueueUrl = osUpdatesQueueUrl;
  }

  @Override
  public void publicarParaOs(EventoBilling evento) {
    try {
      SendMessageRequest request = SqsCorrelationId.withCorrelationId(
                      SendMessageRequest.builder()
                              .queueUrl(osUpdatesQueueUrl)
                              .messageBody(mapper.writeValueAsString(evento)))
              .build();
      sqs.sendMessage(request);
      log.info("Evento {} da OS {} publicado em os-updates", evento.tipoEvento(), evento.numeroOs());
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Falha ao serializar evento", e);
    }
  }
}
