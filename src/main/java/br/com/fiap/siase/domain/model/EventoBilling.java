package br.com.fiap.siase.domain.model;

import java.time.Instant;
import java.util.UUID;

public record EventoBilling(
        String tipoEvento,
        UUID osId,
        String numeroOs,
        String payload,
        Instant ocorridoEm
) {
  // consumidos (billing-queue)
  public static final String OS_CRIADA = "OS_CRIADA";
  public static final String OS_CANCELADA = "OS_CANCELADA";
  public static final String ORCAMENTO_RECUSADO = "ORCAMENTO_RECUSADO";

  // produzidos (os-updates-queue)
  public static final String ORCAMENTO_GERADO = "ORCAMENTO_GERADO";
  public static final String PAGAMENTO_CONFIRMADO = "PAGAMENTO_CONFIRMADO";
  public static final String PAGAMENTO_CANCELADO = "PAGAMENTO_CANCELADO";

  public static EventoBilling de(String tipo, UUID osId, String numeroOs, String payload) {
    return new EventoBilling(tipo, osId, numeroOs, payload, Instant.now());
  }
}
