package br.com.fiap.siase.domain.port;

import br.com.fiap.siase.domain.model.Orcamento;
import java.util.Optional;
import java.util.UUID;

public interface OrcamentoRepositoryPort {
    Orcamento save(Orcamento orcamento);
    Optional<Orcamento> findById(UUID id);
    Optional<Orcamento> findByOsId(String osId);
    Optional<Orcamento> findByOsNumero(String osNumero);
}
