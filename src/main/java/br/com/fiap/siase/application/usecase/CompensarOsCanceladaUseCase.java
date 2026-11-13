package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.application.ports.MercadoPagoPort;
import br.com.fiap.siase.application.ports.OrcamentoRepositoryPort;
import br.com.fiap.siase.application.ports.PagamentoRepositoryPort;
import br.com.fiap.siase.domain.model.EventoBilling;
import br.com.fiap.siase.domain.model.Orcamento;
import br.com.fiap.siase.domain.model.StatusPagamento;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Compensação do Saga: ao receber OS_CANCELADA ou ORCAMENTO_RECUSADO,
 * cancela o orçamento e reembolsa pagamento confirmado (se houver),
 * publicando PAGAMENTO_CANCELADO para o os-service.
 */
@Service
public class CompensarOsCanceladaUseCase {

    private static final Logger log = LoggerFactory.getLogger(CompensarOsCanceladaUseCase.class);

    private final OrcamentoRepositoryPort orcamentos;
    private final PagamentoRepositoryPort pagamentos;
    private final MercadoPagoPort mercadoPago;
    private final EventoPublicadorPort publicador;
    private final MeterRegistry metrics;

    public CompensarOsCanceladaUseCase(OrcamentoRepositoryPort orcamentos,
                                       PagamentoRepositoryPort pagamentos, MercadoPagoPort mercadoPago,
                                       EventoPublicadorPort publicador, MeterRegistry metrics) {
        this.orcamentos = orcamentos;
        this.pagamentos = pagamentos;
        this.mercadoPago = mercadoPago;
        this.publicador = publicador;
        this.metrics = metrics;
    }

    @Transactional
    public void executar(EventoBilling evento) {
        orcamentos.buscarPorOsId(evento.osId()).ifPresent(orcamento -> {
            cancelarOrcamento(orcamento);
            reembolsarSePago(orcamento, evento);
        });
        metrics.counter("siase_saga_eventos_total", "servico", "billing",
                "evento", evento.tipoEvento(), "direcao", "consumido").increment();
        log.info("Compensação {} aplicada para OS {}", evento.tipoEvento(), evento.numeroOs());
    }

    private void cancelarOrcamento(Orcamento orcamento) {
        try {
            orcamento.cancelar();
            orcamentos.salvar(orcamento);
        } catch (IllegalStateException e) {
            // orçamento já pago — o cancelamento vira reembolso abaixo
            log.info("Orçamento {} já pago; aplicando reembolso", orcamento.getId());
        }
    }

    private void reembolsarSePago(Orcamento orcamento, EventoBilling evento) {
        pagamentos.buscarPorOsId(orcamento.getOsId())
                .filter(p -> p.getStatus() == StatusPagamento.CONFIRMADO)
                .ifPresent(pagamento -> {
                    mercadoPago.reembolsar(pagamento.getMpPaymentId());
                    pagamento.reembolsar();
                    pagamentos.salvar(pagamento);
                    publicador.publicarParaOs(EventoBilling.de(
                            EventoBilling.PAGAMENTO_CANCELADO,
                            evento.osId(), evento.numeroOs(), null));
                });
    }
}
