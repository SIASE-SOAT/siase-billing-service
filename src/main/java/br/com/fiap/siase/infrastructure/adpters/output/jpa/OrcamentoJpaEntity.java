package br.com.fiap.siase.infrastructure.adpters.output.jpa;

import br.com.fiap.siase.domain.model.StatusOrcamento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orcamentos")
public class OrcamentoJpaEntity {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private UUID osId;

  @Column(nullable = false)
  private String numeroOs;

  @Column(nullable = false)
  private BigDecimal valor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StatusOrcamento status;

  private String preferenciaMpId;
  private String linkPagamento;

  @Column(nullable = false)
  private Instant criadoEm;

  @Column(nullable = false)
  private Instant atualizadoEm;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public UUID getOsId() { return osId; }
  public void setOsId(UUID osId) { this.osId = osId; }
  public String getNumeroOs() { return numeroOs; }
  public void setNumeroOs(String numeroOs) { this.numeroOs = numeroOs; }
  public BigDecimal getValor() { return valor; }
  public void setValor(BigDecimal valor) { this.valor = valor; }
  public StatusOrcamento getStatus() { return status; }
  public void setStatus(StatusOrcamento status) { this.status = status; }
  public String getPreferenciaMpId() { return preferenciaMpId; }
  public void setPreferenciaMpId(String p) { this.preferenciaMpId = p; }
  public String getLinkPagamento() { return linkPagamento; }
  public void setLinkPagamento(String l) { this.linkPagamento = l; }
  public Instant getCriadoEm() { return criadoEm; }
  public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
  public Instant getAtualizadoEm() { return atualizadoEm; }
  public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
