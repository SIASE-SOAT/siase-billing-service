package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.application.ports.MercadoPagoPort;
import br.com.fiap.siase.application.ports.OrcamentoRepositoryPort;
import br.com.fiap.siase.application.ports.PagamentoRepositoryPort;
import br.com.fiap.siase.domain.model.EventoBilling;
import br.com.fiap.siase.domain.model.Orcamento;
import br.com.fiap.siase.domain.model.Pagamento;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Webhook do Mercado Pago: confirma o pagamento e publica
 * PAGAMENTO_CONFIRMADO → os-service entrega a OS.
 */
@Service
public class ProcessarWebhookMercadoPagoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessarWebhookMercadoPagoUseCase.class);

    private final OrcamentoRepositoryPort orcamentos;
    private final PagamentoRepositoryPort pagamentos;
    private final MercadoPagoPort mercadoPago;
    private final EventoPublicadorPort publicador;
    private final MeterRegistry metrics;

    public ProcessarWebhookMercadoPagoUseCase(OrcamentoRepositoryPort orcamentos,
                                              PagamentoRepositoryPort pagamentos, MercadoPagoPort mercadoPago,
                                              EventoPublicadorPort publicador, MeterRegistry metrics) {
        this.orcamentos = orcamentos;
        this.pagamentos = pagamentos;
        this.mercadoPago = mercadoPago;
        this.publicador = publicador;
        this.metrics = metrics;
    }

    @Transactional
    public void executar(String mpPaymentId) {
        // idempotência: webhook pode ser reenviado pelo MP
        if (pagamentos.buscarPorMpPaymentId(mpPaymentId).isPresent()) {
            log.info("Pagamento {} já processado", mpPaymentId);
            return;
        }

        MercadoPagoPort.StatusMp mp = mercadoPago.consultarPagamento(mpPaymentId);
        if (mp == null || mp.externalReference() == null
                || !"approved".equalsIgnoreCase(mp.status())) {
            log.info("Pagamento {} não aprovado (status: {})", mpPaymentId,
                    mp != null ? mp.status() : "inexistente");
            return;
        }

        Orcamento orcamento = orcamentos.buscarPorOsId(osIdDoPagamento(mp))
                .orElseThrow(() -> new IllegalStateException(
                        "Orçamento não encontrado para pagamento " + mpPaymentId));

        orcamento.confirmarPagamento();
        orcamentos.salvar(orcamento);

        Pagamento pagamento = pagamentos.salvar(
                Pagamento.confirmar(orcamento.getId(), orcamento.getOsId(),
                        mp.valor(), mpPaymentId));

        publicador.publicarParaOs(EventoBilling.de(EventoBilling.PAGAMENTO_CONFIRMADO,
                orcamento.getOsId(), orcamento.getNumeroOs(),
                "{\"pagamentoId\":\"" + pagamento.getId() + "\"}"));
        metrics.counter("siase_saga_eventos_total", "servico", "billing",
                "evento", EventoBilling.PAGAMENTO_CONFIRMADO, "direcao", "produzido").increment();
    }

    private java.util.UUID osIdDoPagamento(MercadoPagoPort.StatusMp mp) {
        // a preferência é criada com external_reference = osId (ver adapter MP)
        return java.util.UUID.fromString(mp.externalReference());
    }
}
