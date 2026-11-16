package br.com.fiap.siase.infrastructure.adpters.output.jpa;

import br.com.fiap.siase.domain.model.StatusPagamento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pagamentos")
public class PagamentoJpaEntity {

  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID orcamentoId;

  @Column(nullable = false)
  private UUID osId;

  @Column(nullable = false)
  private BigDecimal valor;

  @Column(unique = true)
  private String mpPaymentId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StatusPagamento status;

  @Column(nullable = false)
  private Instant criadoEm;

  @Column(nullable = false)
  private Instant atualizadoEm;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public UUID getOrcamentoId() { return orcamentoId; }
  public void setOrcamentoId(UUID o) { this.orcamentoId = o; }
  public UUID getOsId() { return osId; }
  public void setOsId(UUID osId) { this.osId = osId; }
  public BigDecimal getValor() { return valor; }
  public void setValor(BigDecimal valor) { this.valor = valor; }
  public String getMpPaymentId() { return mpPaymentId; }
  public void setMpPaymentId(String mp) { this.mpPaymentId = mp; }
  public StatusPagamento getStatus() { return status; }
  public void setStatus(StatusPagamento status) { this.status = status; }
  public Instant getCriadoEm() { return criadoEm; }
  public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
  public Instant getAtualizadoEm() { return atualizadoEm; }
  public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
