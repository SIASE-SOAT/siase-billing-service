package br.com.fiap.siase.domain.port;

import br.com.fiap.siase.domain.model.Pagamento;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepositoryPort {
    Pagamento save(Pagamento pagamento);
    Optional<Pagamento> findById(UUID id);
    Optional<Pagamento> findByOsId(String osId);
    Optional<Pagamento> findByMercadoPagoPaymentId(String mpPaymentId);
}
