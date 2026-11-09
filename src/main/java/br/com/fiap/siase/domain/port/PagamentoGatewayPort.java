package br.com.fiap.siase.domain.port;

import java.math.BigDecimal;

public interface PagamentoGatewayPort {

    record PreferenciaResult(String preferenceId, String linkPagamento) {}

    record WebhookResult(String paymentId, String status, BigDecimal valor,
                         String formaPagamento, String externalReference) {}

    PreferenciaResult criarPreferencia(String osNumero, String clienteEmail,
                                       String clienteNome, BigDecimal valor);

    WebhookResult consultarPagamento(String paymentId);
}
