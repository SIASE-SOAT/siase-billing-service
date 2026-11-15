package br.com.fiap.siase.application.ports;


import br.com.fiap.siase.domain.model.Orcamento;

import java.math.BigDecimal;

/**
 * Integração com o Mercado Pago: pagamento Pix com QR Code dinâmico
 * (criação via POST /v1/payments) e consulta de status.
 */
public interface MercadoPagoPort {

  /**
   * Pagamento Pix criado no Mercado Pago.
   *
   * @param mpPaymentId  id do pagamento (usado no webhook/reembolso)
   * @param qrCode       payload "Pix copia e cola" (EMV)
   * @param qrCodeBase64 imagem do QR Code em base64, quando retornada pelo MP
   */
  record PagamentoPix(String mpPaymentId, String qrCode, String qrCodeBase64) {}

  /** Cria um pagamento Pix (QR dinâmico) para o orçamento. */
  PagamentoPix criarPagamentoPix(Orcamento orcamento, BigDecimal valor, String callbackUrl);

  /** Consulta o pagamento no MP pelo payment id do webhook. Retorna null se inexistente. */
  record StatusMp(String paymentId, String status, BigDecimal valor, String externalReference) {}
  StatusMp consultarPagamento(String mpPaymentId);

  /** Reembolsa/cancela um pagamento (compensação). */
  void reembolsar(String mpPaymentId);
}
