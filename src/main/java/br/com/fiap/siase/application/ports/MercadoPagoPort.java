package br.com.fiap.siase.application.ports;


import br.com.fiap.siase.domain.model.Orcamento;

import java.math.BigDecimal;

/**
 * Integração com o Mercado Pago: preferências de pagamento e consulta de status.
 */
public interface MercadoPagoPort {

  record Preferencia(String id, String linkPagamento) {}

  /** Cria preferência de pagamento (link/checkout) para o orçamento. */
  Preferencia criarPreferencia(Orcamento orcamento, BigDecimal valor, String callbackUrl);

  /** Consulta o pagamento no MP pelo payment id do webhook. Retorna null se inexistente. */
  record StatusMp(String paymentId, String status, BigDecimal valor, String externalReference) {}
  StatusMp consultarPagamento(String mpPaymentId);

  /** Reembolsa/cancela um pagamento (compensação). */
  void reembolsar(String mpPaymentId);
}
