package br.com.fiap.siase.infrastructure.persistence;

import br.com.fiap.siase.domain.model.Orcamento;
import br.com.fiap.siase.domain.model.Pagamento;
import br.com.fiap.siase.domain.port.OrcamentoRepositoryPort;
import br.com.fiap.siase.domain.port.PagamentoRepositoryPort;
import br.com.fiap.siase.infrastructure.persistence.mapper.BillingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrcamentoRepositoryAdapter implements OrcamentoRepositoryPort {
    private final OrcamentoJpaRepository jpa;
    private final BillingMapper mapper;

    @Override public Orcamento save(Orcamento o) { return mapper.toDomain(jpa.save(mapper.toEntity(o))); }
    @Override public Optional<Orcamento> findById(UUID id) { return jpa.findById(id).map(mapper::toDomain); }
    @Override public Optional<Orcamento> findByOsId(String osId) { return jpa.findByOsId(osId).map(mapper::toDomain); }
    @Override public Optional<Orcamento> findByOsNumero(String osNumero) { return jpa.findByOsNumero(osNumero).map(mapper::toDomain); }
}
