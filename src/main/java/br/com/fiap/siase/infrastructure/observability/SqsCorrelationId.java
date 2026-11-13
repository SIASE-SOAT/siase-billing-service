package br.com.fiap.siase.infrastructure.observability;

import org.slf4j.MDC;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;


public final class SqsCorrelationId {

  public static final String ATTRIBUTE = "correlationId";

  private SqsCorrelationId() {}

  /** Adiciona o correlationId atual do MDC (ou gera um) como message attribute. */
  public static SendMessageRequest.Builder withCorrelationId(SendMessageRequest.Builder builder) {
    String id = Optional.ofNullable(MDC.get(CorrelationIdFilter.MDC_KEY))
            .orElse(UUID.randomUUID().toString());
    return builder.messageAttributes(Map.of(
            ATTRIBUTE, MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(id)
                    .build()));
  }

  /** Restaura o correlationId da mensagem no MDC do consumer. */
  public static void mdcFrom(Message message) {
    String id = Optional.ofNullable(message.messageAttributes())
            .map(attrs -> attrs.get(ATTRIBUTE))
            .map(MessageAttributeValue::stringValue)
            .orElse(UUID.randomUUID().toString());
    MDC.put(CorrelationIdFilter.MDC_KEY, id);
  }
}
