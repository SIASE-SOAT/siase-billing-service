package br.com.fiap.siase.infrastructure.persistence;

import br.com.fiap.siase.infrastructure.persistence.entity.PagamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoJpaRepository extends JpaRepository<PagamentoEntity, UUID> {
    Optional<PagamentoEntity> findByOsId(String osId);
    Optional<PagamentoEntity> findByMercadoPagoPaymentId(String mpPaymentId);
}
