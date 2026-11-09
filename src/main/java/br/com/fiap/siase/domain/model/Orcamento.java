package br.com.fiap.siase.domain.model;

import br.com.fiap.siase.domain.enums.StatusOrcamento;
import br.com.fiap.siase.domain.exception.BusinessException;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Orcamento {
    private UUID id;
    private String osId;
    private String osNumero;
    private String clienteId;
    private String clienteNome;
    private String clienteEmail;
    private BigDecimal valor;
    @Builder.Default
    private StatusOrcamento status = StatusOrcamento.PENDENTE;
    private String linkPagamento;
    private String mercadoPagoPreferenceId;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public void aprovar() {
        if (this.status != StatusOrcamento.PENDENTE)
            throw new BusinessException("Apenas orçamentos pendentes podem ser aprovados.");
        this.status = StatusOrcamento.APROVADO;
    }

    public void recusar() {
        if (this.status != StatusOrcamento.PENDENTE)
            throw new BusinessException("Apenas orçamentos pendentes podem ser recusados.");
        this.status = StatusOrcamento.RECUSADO;
    }

    public void cancelar() {
        if (this.status == StatusOrcamento.CANCELADO)
            throw new BusinessException("Orçamento já está cancelado.");
        this.status = StatusOrcamento.CANCELADO;
    }
}
