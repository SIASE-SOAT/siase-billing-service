package br.com.fiap.siase.application;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.application.ports.MercadoPagoPort;
import br.com.fiap.siase.application.ports.OrcamentoRepositoryPort;
import br.com.fiap.siase.application.ports.PagamentoRepositoryPort;
import br.com.fiap.siase.application.usecase.CompensarOsCanceladaUseCase;
import br.com.fiap.siase.application.usecase.GerarOrcamentoUseCase;
import br.com.fiap.siase.application.usecase.ProcessarWebhookMercadoPagoUseCase;
import br.com.fiap.siase.domain.model.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class BillingUseCasesTest {

    public static class OrcamentoRepoMem implements OrcamentoRepositoryPort {
        public Map<UUID, Orcamento> dados = new HashMap<>();
        public Orcamento salvar(Orcamento o) { dados.put(o.getId(), o); return o; }
        public Optional<Orcamento> buscarPorId(UUID id) { return Optional.ofNullable(dados.get(id)); }
        public Optional<Orcamento> buscarPorOsId(UUID osId) {
            return dados.values().stream().filter(o -> o.getOsId().equals(osId)).findFirst();
        }
    }

    public static class PagamentoRepoMem implements PagamentoRepositoryPort {
        public Map<UUID, Pagamento> dados = new HashMap<>();
        public Pagamento salvar(Pagamento p) { dados.put(p.getId(), p); return p; }
        public Optional<Pagamento> buscarPorId(UUID id) { return Optional.ofNullable(dados.get(id)); }
        public Optional<Pagamento> buscarPorOsId(UUID osId) {
            return dados.values().stream().filter(p -> p.getOsId().equals(osId)).findFirst();
        }
        public Optional<Pagamento> buscarPorMpPaymentId(String mp) {
            return dados.values().stream().filter(p -> mp.equals(p.getMpPaymentId())).findFirst();
        }
    }

    public static class MpFake implements MercadoPagoPort {
        public Map<String, StatusMp> pagamentosMp = new HashMap<>();
        public List<String> reembolsos = new ArrayList<>();
        public PagamentoPix criarPagamentoPix(Orcamento o, BigDecimal v, String cb) {
            return new PagamentoPix("pay-1", "pix-copia-e-cola-1", "cXItY29kZS0x");
        }
        public StatusMp consultarPagamento(String id) { return pagamentosMp.get(id); }
        public void reembolsar(String mpPaymentId) { reembolsos.add(mpPaymentId); }
    }

    public static class PubMem implements EventoPublicadorPort {
        public List<String> eventos = new ArrayList<>();
        public void publicarParaOs(EventoBilling e) { eventos.add(e.tipoEvento()); }
    }

    private EventoBilling osCriada(UUID osId) {
        return EventoBilling.de(EventoBilling.OS_CRIADA, osId, "OS-TEST123", null);
    }

    @Test
    void osCriadaGeraOrcamentoComPreferenciaEPublicaUpdate() {
        var orc = new OrcamentoRepoMem();
        var mp = new MpFake();
        var pub = new PubMem();
        var uc = new GerarOrcamentoUseCase(orc, mp, pub, new SimpleMeterRegistry(), "http://billing");
        UUID osId = UUID.randomUUID();

        Orcamento o = uc.executar(osCriada(osId));

        assertEquals(StatusOrcamento.AGUARDANDO_PAGAMENTO, o.getStatus());
        assertNotNull(o.getLinkPagamento());
        assertEquals(List.of(EventoBilling.ORCAMENTO_GERADO), pub.eventos);
    }

    @Test
    void osCriadaDuplicadaNaoGeraSegundoOrcamento() {
        var orc = new OrcamentoRepoMem();
        var uc = new GerarOrcamentoUseCase(orc, new MpFake(), new PubMem(),
                new SimpleMeterRegistry(), "http://billing");
        UUID osId = UUID.randomUUID();

        Orcamento primeiro = uc.executar(osCriada(osId));
        Orcamento segundo = uc.executar(osCriada(osId));

        assertEquals(primeiro.getId(), segundo.getId());
    }

    @Test
    void webhookAprovadoConfirmaPagamentoEPublicaEvento() {
        var orc = new OrcamentoRepoMem();
        var pag = new PagamentoRepoMem();
        var mp = new MpFake();
        var pub = new PubMem();
        var gerar = new GerarOrcamentoUseCase(orc, mp, new PubMem(), new SimpleMeterRegistry(), "http://b");
        var uc = new ProcessarWebhookMercadoPagoUseCase(orc, pag, mp, pub, new SimpleMeterRegistry());
        UUID osId = UUID.randomUUID();
        gerar.executar(osCriada(osId));
        mp.pagamentosMp.put("pay-1", new MercadoPagoPort.StatusMp("pay-1", "approved",
                new BigDecimal("250"), osId.toString()));

        uc.executar("pay-1");

        assertEquals(StatusPagamento.CONFIRMADO, pag.buscarPorOsId(osId).orElseThrow().getStatus());
        assertEquals(StatusOrcamento.PAGO,
                orc.buscarPorOsId(osId).orElseThrow().getStatus());
        assertTrue(pub.eventos.contains(EventoBilling.PAGAMENTO_CONFIRMADO));
    }

    @Test
    void osCanceladaComPagamentoConfirmadoReembolsaEPublicaCompensacao() {
        var orc = new OrcamentoRepoMem();
        var pag = new PagamentoRepoMem();
        var mp = new MpFake();
        var pub = new PubMem();
        var gerar = new GerarOrcamentoUseCase(orc, mp, pub, new SimpleMeterRegistry(), "http://b");
        var webhook = new ProcessarWebhookMercadoPagoUseCase(orc, pag, mp, pub, new SimpleMeterRegistry());
        var uc = new CompensarOsCanceladaUseCase(orc, pag, mp, pub, new SimpleMeterRegistry());
        UUID osId = UUID.randomUUID();
        gerar.executar(osCriada(osId));
        mp.pagamentosMp.put("pay-9", new MercadoPagoPort.StatusMp("pay-9", "approved",
                new BigDecimal("250"), osId.toString()));
        webhook.executar("pay-9");

        uc.executar(EventoBilling.de(EventoBilling.OS_CANCELADA, osId, "OS-TEST123", null));

        assertEquals(List.of("pay-9"), mp.reembolsos);
        assertEquals(StatusPagamento.REEMBOLSADO, pag.buscarPorOsId(osId).orElseThrow().getStatus());
        assertTrue(pub.eventos.contains(EventoBilling.PAGAMENTO_CANCELADO));
    }

    @Test
    void orcamentoRecusadoCancelaOrcamentoSemPagamento() {
        var orc = new OrcamentoRepoMem();
        var pag = new PagamentoRepoMem();
        var mp = new MpFake();
        var pub = new PubMem();
        var gerar = new GerarOrcamentoUseCase(orc, mp, pub, new SimpleMeterRegistry(), "http://b");
        var uc = new CompensarOsCanceladaUseCase(orc, pag, mp, pub, new SimpleMeterRegistry());
        UUID osId = UUID.randomUUID();
        gerar.executar(osCriada(osId));

        uc.executar(EventoBilling.de(EventoBilling.ORCAMENTO_RECUSADO, osId, "OS-TEST123", null));

        assertEquals(StatusOrcamento.CANCELADO, orc.buscarPorOsId(osId).orElseThrow().getStatus());
        assertTrue(mp.reembolsos.isEmpty());
    }
}
