package br.com.fiap.siase.domain.model;

import br.com.fiap.siase.domain.enums.StatusPagamento;
import br.com.fiap.siase.domain.exception.BusinessException;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pagamento {
    private UUID id;
    private String osId;
    private String osNumero;
    private String clienteEmail;
    private BigDecimal valor;
    private String formaPagamento;
    private String mercadoPagoPaymentId;
    @Builder.Default
    private StatusPagamento status = StatusPagamento.PENDENTE;
    private LocalDateTime dataPagamento;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public void confirmar(String mpPaymentId) {
        if (this.status != StatusPagamento.PENDENTE)
            throw new BusinessException("Apenas pagamentos pendentes podem ser confirmados.");
        this.status = StatusPagamento.PAGO;
        this.mercadoPagoPaymentId = mpPaymentId;
        this.dataPagamento = LocalDateTime.now();
    }

    public void marcarFalha() {
        if (this.status != StatusPagamento.PENDENTE)
            throw new BusinessException("Apenas pagamentos pendentes podem ser marcados como falha.");
        this.status = StatusPagamento.FALHOU;
    }

    public void cancelar() {
        if (this.status == StatusPagamento.PAGO)
            throw new BusinessException("Pagamento já confirmado não pode ser cancelado.");
        this.status = StatusPagamento.CANCELADO;
    }
}
