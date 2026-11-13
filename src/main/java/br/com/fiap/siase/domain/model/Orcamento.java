package br.com.fiap.siase.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Orcamento {

    private final UUID id;
    private final UUID osId;
    private final String numeroOs;
    private final BigDecimal valor;
    private StatusOrcamento status;
    private String preferenciaMpId;
    private String linkPagamento;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    public Orcamento(UUID id, UUID osId, String numeroOs, BigDecimal valor,
                     StatusOrcamento status, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.osId = osId;
        this.numeroOs = numeroOs;
        this.valor = valor;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Orcamento criar(UUID osId, String numeroOs, BigDecimal valor) {
        Instant agora = Instant.now();
        return new Orcamento(UUID.randomUUID(), osId, numeroOs, valor,
                StatusOrcamento.GERADO, agora, agora);
    }

    public void registrarPreferencia(String preferenciaId, String link) {
        this.preferenciaMpId = preferenciaId;
        this.linkPagamento = link;
        this.status = StatusOrcamento.AGUARDANDO_PAGAMENTO;
        this.atualizadoEm = Instant.now();
    }

    public void confirmarPagamento() {
        this.status = StatusOrcamento.PAGO;
        this.atualizadoEm = Instant.now();
    }

    /** Restaura dados persistidos sem alterar o status (uso do adapter JPA). */
    public void restaurarPreferenciaPersistida(String preferenciaId, String link) {
        this.preferenciaMpId = preferenciaId;
        this.linkPagamento = link;
    }

    public void cancelar() {
        if (status == StatusOrcamento.PAGO) {
            throw new IllegalStateException("Orçamento pago não pode ser cancelado");
        }
        this.status = StatusOrcamento.CANCELADO;
        this.atualizadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOsId() { return osId; }
    public String getNumeroOs() { return numeroOs; }
    public BigDecimal getValor() { return valor; }
    public StatusOrcamento getStatus() { return status; }
    public String getPreferenciaMpId() { return preferenciaMpId; }
    public String getLinkPagamento() { return linkPagamento; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
