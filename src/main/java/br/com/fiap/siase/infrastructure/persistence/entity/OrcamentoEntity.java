package br.com.fiap.siase.infrastructure.persistence.entity;

import br.com.fiap.siase.domain.enums.StatusOrcamento;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orcamentos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrcamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "os_id", nullable = false, unique = true)
    private String osId;

    @Column(name = "os_numero", nullable = false)
    private String osNumero;

    @Column(name = "cliente_id", nullable = false)
    private String clienteId;

    @Column(name = "cliente_nome", nullable = false)
    private String clienteNome;

    @Column(name = "cliente_email")
    private String clienteEmail;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrcamento status;

    @Column(name = "link_pagamento", length = 500)
    private String linkPagamento;

    @Column(name = "mercado_pago_preference_id")
    private String mercadoPagoPreferenceId;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}
