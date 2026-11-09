package br.com.fiap.siase.infrastructure.persistence;

import br.com.fiap.siase.domain.model.Pagamento;
import br.com.fiap.siase.domain.port.PagamentoRepositoryPort;
import br.com.fiap.siase.infrastructure.persistence.mapper.BillingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PagamentoRepositoryAdapter implements PagamentoRepositoryPort {
    private final PagamentoJpaRepository jpa;
    private final BillingMapper mapper;

    @Override public Pagamento save(Pagamento p) { return mapper.toDomain(jpa.save(mapper.toEntity(p))); }
    @Override public Optional<Pagamento> findById(UUID id) { return jpa.findById(id).map(mapper::toDomain); }
    @Override public Optional<Pagamento> findByOsId(String osId) { return jpa.findByOsId(osId).map(mapper::toDomain); }
    @Override public Optional<Pagamento> findByMercadoPagoPaymentId(String mpId) { return jpa.findByMercadoPagoPaymentId(mpId).map(mapper::toDomain); }
}
