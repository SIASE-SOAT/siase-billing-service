package br.com.fiap.siase.application.ports;


import br.com.fiap.siase.domain.model.Pagamento;

import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepositoryPort {

  Pagamento salvar(Pagamento pagamento);

  Optional<Pagamento> buscarPorId(UUID id);

  Optional<Pagamento> buscarPorOsId(UUID osId);

  Optional<Pagamento> buscarPorMpPaymentId(String mpPaymentId);
}
