package br.com.fiap.siase.infrastructure.adpters.output.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PagamentoJpaRepository extends JpaRepository<PagamentoJpaEntity, UUID> {
  Optional<PagamentoJpaEntity> findByOsId(UUID osId);
  Optional<PagamentoJpaEntity> findByMpPaymentId(String mpPaymentId);
}
