package br.com.fiap.siase.domain.enums;

public enum StatusOrcamento {
    PENDENTE("Pendente"),
    APROVADO("Aprovado"),
    RECUSADO("Recusado"),
    CANCELADO("Cancelado");

    private final String descricao;
    StatusOrcamento(String d) { this.descricao = d; }
    public String getDescricao() { return descricao; }
}
