package br.com.fiap.siase.infrastructure.persistence;

import br.com.fiap.siase.infrastructure.persistence.entity.OrcamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OrcamentoJpaRepository extends JpaRepository<OrcamentoEntity, UUID> {
    Optional<OrcamentoEntity> findByOsId(String osId);
    Optional<OrcamentoEntity> findByOsNumero(String osNumero);
}
