package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.application.ports.MercadoPagoPort;
import br.com.fiap.siase.application.ports.OrcamentoRepositoryPort;
import br.com.fiap.siase.domain.model.EventoBilling;
import br.com.fiap.siase.domain.model.Orcamento;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
public class GerarOrcamentoUseCase {

    private final OrcamentoRepositoryPort orcamentos;
    private final MercadoPagoPort mercadoPago;
    private final EventoPublicadorPort publicador;
    private final MeterRegistry metrics;
    private final String callbackUrl;

    public GerarOrcamentoUseCase(OrcamentoRepositoryPort orcamentos,
                                 MercadoPagoPort mercadoPago,
                                 EventoPublicadorPort publicador,
                                 MeterRegistry metrics,
                                 @Value("${app.billing.public-url}") String callbackUrl) {
        this.orcamentos = orcamentos;
        this.mercadoPago = mercadoPago;
        this.publicador = publicador;
        this.metrics = metrics;
        this.callbackUrl = callbackUrl;
    }

    @Transactional
    public Orcamento executar(EventoBilling evento) {
        // idempotência: OS_CRIADA duplicado não gera segundo orçamento
        var existente = orcamentos.buscarPorOsId(evento.osId());
        if (existente.isPresent()) {
            return existente.get();
        }

        BigDecimal valor = valorDoOrcamento(evento);
        Orcamento orcamento = orcamentos.salvar(
                Orcamento.criar(evento.osId(), evento.numeroOs(), valor));

        MercadoPagoPort.Preferencia pref =
                mercadoPago.criarPreferencia(orcamento, valor, callbackUrl);
        orcamento.registrarPreferencia(pref.id(), pref.linkPagamento());
        orcamentos.salvar(orcamento);

        EventoBilling update = EventoBilling.de(EventoBilling.ORCAMENTO_GERADO,
                orcamento.getOsId(), orcamento.getNumeroOs(),
                "{\"orcamentoId\":\"" + orcamento.getId() + "\",\"linkPagamento\":\""
                        + pref.linkPagamento() + "\"}");
        publicador.publicarParaOs(update);
        contar(EventoBilling.ORCAMENTO_GERADO);
        return orcamento;
    }

    private BigDecimal valorDoOrcamento(EventoBilling evento) {
        // payload opcional do evento pode trazer {"valor": X}
        if (evento.payload() != null && evento.payload().contains("valor")) {
            String digits = evento.payload().replaceAll("[^0-9.]", "");
            if (!digits.isBlank()) {
                return new BigDecimal(digits);
            }
        }
        return new BigDecimal("250.00");
    }

    private void contar(String evento) {
        metrics.counter("siase_saga_eventos_total",
                "servico", "billing", "evento", evento, "direcao", "produzido").increment();
    }
}
