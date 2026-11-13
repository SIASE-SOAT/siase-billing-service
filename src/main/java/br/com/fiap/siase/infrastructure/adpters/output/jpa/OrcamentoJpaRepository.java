package br.com.fiap.siase.infrastructure.adpters.output.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrcamentoJpaRepository extends JpaRepository<OrcamentoJpaEntity, UUID> {
  Optional<OrcamentoJpaEntity> findByOsId(UUID osId);
}
