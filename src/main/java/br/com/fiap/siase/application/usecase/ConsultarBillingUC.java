package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.domain.exception.ResourceNotFoundException;
import br.com.fiap.siase.domain.model.Orcamento;
import br.com.fiap.siase.domain.model.Pagamento;
import br.com.fiap.siase.domain.port.OrcamentoRepositoryPort;
import br.com.fiap.siase.domain.port.PagamentoRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ConsultarBillingUC {

    private final OrcamentoRepositoryPort orcamentoRepository;
    private final PagamentoRepositoryPort pagamentoRepository;

    public Orcamento orcamentoPorOsId(String osId) {
        return orcamentoRepository.findByOsId(osId)
                .orElseThrow(() -> new ResourceNotFoundException("Orçamento não encontrado para OS: " + osId));
    }

    public Pagamento pagamentoPorOsId(String osId) {
        return pagamentoRepository.findByOsId(osId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado para OS: " + osId));
    }

    public Pagamento pagamentoPorId(UUID id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado: " + id));
    }
}
