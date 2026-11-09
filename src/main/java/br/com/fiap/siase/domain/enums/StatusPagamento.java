package br.com.fiap.siase.domain.enums;

public enum StatusPagamento {
    PENDENTE("Pendente"),
    PAGO("Pago"),
    FALHOU("Falhou"),
    CANCELADO("Cancelado");

    private final String descricao;
    StatusPagamento(String d) { this.descricao = d; }
    public String getDescricao() { return descricao; }
}
