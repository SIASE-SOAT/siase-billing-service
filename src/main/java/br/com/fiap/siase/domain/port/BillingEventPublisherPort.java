package br.com.fiap.siase.domain.port;

public interface BillingEventPublisherPort {
    void publicarOrcamentoGerado(String osNumero, String osId);
    void publicarPagamentoConfirmado(String osNumero, String osId, String valor, String formaPagamento);
    void publicarPagamentoFalhou(String osNumero, String osId);
}
