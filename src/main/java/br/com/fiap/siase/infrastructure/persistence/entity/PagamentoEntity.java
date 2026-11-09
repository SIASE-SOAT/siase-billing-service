package br.com.fiap.siase.infrastructure.persistence.entity;

import br.com.fiap.siase.domain.enums.StatusPagamento;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagamentos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PagamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "os_id", nullable = false)
    private String osId;

    @Column(name = "os_numero", nullable = false)
    private String osNumero;

    @Column(name = "cliente_email")
    private String clienteEmail;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "forma_pagamento")
    private String formaPagamento;

    @Column(name = "mercado_pago_payment_id", unique = true)
    private String mercadoPagoPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}
