package br.com.fiap.siase.bdd;

import br.com.fiap.siase.application.ports.MercadoPagoPort;
import br.com.fiap.siase.application.BillingUseCasesTest.MpFake;
import br.com.fiap.siase.application.BillingUseCasesTest.OrcamentoRepoMem;
import br.com.fiap.siase.application.BillingUseCasesTest.PagamentoRepoMem;
import br.com.fiap.siase.application.BillingUseCasesTest.PubMem;
import br.com.fiap.siase.application.usecase.*;
import br.com.fiap.siase.domain.model.*;
import io.cucumber.java.pt.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class BillingSteps {

  private final OrcamentoRepoMem orc = new OrcamentoRepoMem();
  private final PagamentoRepoMem pag = new PagamentoRepoMem();
  private final MpFake mp = new MpFake();
  private final PubMem pub = new PubMem();
  private final UUID osId = UUID.randomUUID();
  private EventoBilling evento;

  private GerarOrcamentoUseCase gerar() {
    return new GerarOrcamentoUseCase(orc, mp, pub, new SimpleMeterRegistry(), "http://billing");
  }

  @Dado("um evento {string} recebido")
  public void eventoRecebido(String tipo) {
    evento = EventoBilling.de(tipo, osId, "OS-BDD1", null);
  }

  @Dado("um orçamento existente para a OS")
  public void orcamentoExistente() {
    gerar().executar(EventoBilling.de(EventoBilling.OS_CRIADA, osId, "OS-BDD1", null));
  }

  @Dado("um orçamento pago para a OS")
  public void orcamentoPago() {
    orcamentoExistente();
    mp.pagamentosMp.put("pay-bdd", new MercadoPagoPort.StatusMp("pay-bdd", "approved",
            new BigDecimal("250"), osId.toString()));
    new ProcessarWebhookMercadoPagoUseCase(orc, pag, mp, pub, new SimpleMeterRegistry())
            .executar("pay-bdd");
  }

  @Quando("o billing processa o evento")
  public void processarEvento() {
    gerar().executar(evento);
  }

  @Quando("o webhook do Mercado Pago confirma o pagamento")
  public void webhookConfirma() {
    mp.pagamentosMp.put("pay-bdd", new MercadoPagoPort.StatusMp("pay-bdd", "approved",
            new BigDecimal("250"), osId.toString()));
    new ProcessarWebhookMercadoPagoUseCase(orc, pag, mp, pub, new SimpleMeterRegistry())
            .executar("pay-bdd");
  }

  @Quando("o evento {string} é recebido")
  public void eventoCompensacao(String tipo) {
    new CompensarOsCanceladaUseCase(orc, pag, mp, pub, new SimpleMeterRegistry())
            .executar(EventoBilling.de(tipo, osId, "OS-BDD1", null));
  }

  @Então("um orçamento é gerado com preferência Mercado Pago")
  public void orcamentoComPreferencia() {
    var o = orc.buscarPorOsId(osId).orElseThrow();
    assertNotNull(o.getPreferenciaMpId());
    assertNotNull(o.getLinkPagamento());
  }

  @Então("o evento {string} é publicado para o os-service")
  public void eventoPublicado(String tipo) {
    assertTrue(pub.eventos.contains(tipo), "esperava " + tipo + " em " + pub.eventos);
  }

  @Então("o pagamento fica {string}")
  public void pagamentoStatus(String status) {
    assertEquals(StatusPagamento.valueOf(status), pag.buscarPorOsId(osId).orElseThrow().getStatus());
  }

  @Então("o pagamento é reembolsado no Mercado Pago")
  public void pagamentoReembolsado() {
    assertFalse(mp.reembolsos.isEmpty());
  }
}
