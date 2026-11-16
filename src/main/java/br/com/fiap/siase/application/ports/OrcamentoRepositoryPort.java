package br.com.fiap.siase.application.ports;


import br.com.fiap.siase.domain.model.Orcamento;

import java.util.Optional;
import java.util.UUID;

public interface OrcamentoRepositoryPort {

  Orcamento salvar(Orcamento orcamento);

  Optional<Orcamento> buscarPorId(UUID id);

  Optional<Orcamento> buscarPorOsId(UUID osId);
}
