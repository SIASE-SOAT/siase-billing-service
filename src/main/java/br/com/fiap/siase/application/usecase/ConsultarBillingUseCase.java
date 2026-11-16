package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.ports.OrcamentoRepositoryPort;
import br.com.fiap.siase.application.ports.PagamentoRepositoryPort;
import br.com.fiap.siase.domain.model.Orcamento;
import br.com.fiap.siase.domain.model.Pagamento;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ConsultarBillingUseCase {

    private final OrcamentoRepositoryPort orcamentos;
    private final PagamentoRepositoryPort pagamentos;

    public ConsultarBillingUseCase(OrcamentoRepositoryPort orcamentos,
                                   PagamentoRepositoryPort pagamentos) {
        this.orcamentos = orcamentos;
        this.pagamentos = pagamentos;
    }

    public Orcamento orcamentoDaOs(UUID osId) {
        return orcamentos.buscarPorOsId(osId)
                .orElseThrow(() -> new NaoEncontradoException("orçamento", osId));
    }

    public Pagamento pagamentoDaOs(UUID osId) {
        return pagamentos.buscarPorOsId(osId)
                .orElseThrow(() -> new NaoEncontradoException("pagamento", osId));
    }

    public Pagamento pagamentoPorId(UUID id) {
        return pagamentos.buscarPorId(id)
                .orElseThrow(() -> new NaoEncontradoException("pagamento", id));
    }
}
