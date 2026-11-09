package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.domain.exception.ResourceNotFoundException;
import br.com.fiap.siase.domain.model.Pagamento;
import br.com.fiap.siase.domain.port.BillingEventPublisherPort;
import br.com.fiap.siase.domain.port.OrcamentoRepositoryPort;
import br.com.fiap.siase.domain.port.PagamentoGatewayPort;
import br.com.fiap.siase.domain.port.PagamentoRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ProcessarWebhookMercadoPagoUC {

    private final PagamentoRepositoryPort pagamentoRepository;
    private final OrcamentoRepositoryPort orcamentoRepository;
    private final PagamentoGatewayPort pagamentoGateway;
    private final BillingEventPublisherPort eventPublisher;

    public void executar(String mpPaymentId, String topic) {
        if (!"payment".equals(topic)) {
            log.debug("Webhook ignorado — topic: {}", topic);
            return;
        }

        // idempotência: se já processamos este pagamento, ignorar
        if (pagamentoRepository.findByMercadoPagoPaymentId(mpPaymentId).isPresent()) {
            log.info("Pagamento {} já processado, ignorando", mpPaymentId);
            return;
        }

        var resultado = pagamentoGateway.consultarPagamento(mpPaymentId);

        if (!"approved".equals(resultado.status())) {
            log.info("Pagamento {} com status {}, aguardando aprovação", mpPaymentId, resultado.status());
            return;
        }

        // O external_reference enviado na criação da preferência é o osNumero.
        // O MercadoPagoAdapter.consultarPagamento() retorna o externalReference no campo formaPagamento
        // por convenção — ver MercadoPagoAdapter. Aqui buscamos o orçamento pelo osNumero.
        String osNumero = resultado.externalReference();
        var orcamento = orcamentoRepository.findByOsNumero(osNumero)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Orçamento não encontrado para OS: " + osNumero));

        var pagamento = Pagamento.builder()
                .osId(orcamento.getOsId())
                .osNumero(osNumero)
                .clienteEmail(orcamento.getClienteEmail())
                .valor(resultado.valor())
                .formaPagamento(resultado.formaPagamento())
                .build();

        pagamento.confirmar(mpPaymentId);
        var salvo = pagamentoRepository.save(pagamento);

        eventPublisher.publicarPagamentoConfirmado(
                salvo.getOsNumero(), salvo.getOsId(),
                salvo.getValor().toPlainString(), salvo.getFormaPagamento());

        log.info("Pagamento {} confirmado para OS {}", mpPaymentId, salvo.getOsNumero());
    }
}
