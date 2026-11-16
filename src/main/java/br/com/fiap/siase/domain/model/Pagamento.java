package br.com.fiap.siase.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Pagamento {

    private final UUID id;
    private final UUID orcamentoId;
    private final UUID osId;
    private final BigDecimal valor;
    private String mpPaymentId;
    private StatusPagamento status;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    public Pagamento(UUID id, UUID orcamentoId, UUID osId, BigDecimal valor,
                     String mpPaymentId, StatusPagamento status,
                     Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.orcamentoId = orcamentoId;
        this.osId = osId;
        this.valor = valor;
        this.mpPaymentId = mpPaymentId;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Pagamento confirmar(UUID orcamentoId, UUID osId,
                                      BigDecimal valor, String mpPaymentId) {
        Instant agora = Instant.now();
        return new Pagamento(UUID.randomUUID(), orcamentoId, osId, valor,
                mpPaymentId, StatusPagamento.CONFIRMADO, agora, agora);
    }

    public void reembolsar() {
        this.status = StatusPagamento.REEMBOLSADO;
        this.atualizadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOrcamentoId() { return orcamentoId; }
    public UUID getOsId() { return osId; }
    public BigDecimal getValor() { return valor; }
    public String getMpPaymentId() { return mpPaymentId; }
    public StatusPagamento getStatus() { return status; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
