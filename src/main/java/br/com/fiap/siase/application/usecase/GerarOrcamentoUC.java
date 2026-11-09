package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.domain.model.Orcamento;
import br.com.fiap.siase.domain.port.BillingEventPublisherPort;
import br.com.fiap.siase.domain.port.OrcamentoRepositoryPort;
import br.com.fiap.siase.domain.port.PagamentoGatewayPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
public class GerarOrcamentoUC {

    private final OrcamentoRepositoryPort orcamentoRepository;
    private final PagamentoGatewayPort pagamentoGateway;
    private final BillingEventPublisherPort eventPublisher;

    public Orcamento executar(String osId, String osNumero, String clienteId,
                               String clienteNome, String clienteEmail, BigDecimal valor) {
        // idempotência: se já existe orçamento para esta OS, retorna o existente
        var existente = orcamentoRepository.findByOsId(osId);
        if (existente.isPresent()) {
            log.info("Orçamento já existe para OS {}, ignorando duplicata", osNumero);
            return existente.get();
        }

        var preferencia = pagamentoGateway.criarPreferencia(osNumero, clienteEmail, clienteNome, valor);

        var orcamento = Orcamento.builder()
                .osId(osId)
                .osNumero(osNumero)
                .clienteId(clienteId)
                .clienteNome(clienteNome)
                .clienteEmail(clienteEmail)
                .valor(valor)
                .linkPagamento(preferencia.linkPagamento())
                .mercadoPagoPreferenceId(preferencia.preferenceId())
                .build();

        var salvo = orcamentoRepository.save(orcamento);
        eventPublisher.publicarOrcamentoGerado(osNumero, osId);
        log.info("Orçamento gerado para OS {} — link: {}", osNumero, preferencia.linkPagamento());
        return salvo;
    }
}
