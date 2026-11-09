package br.com.fiap.siase.bdd;

import br.com.fiap.siase.application.usecase.CancelarOrcamentoUC;
import br.com.fiap.siase.application.usecase.GerarOrcamentoUC;
import br.com.fiap.siase.application.usecase.ProcessarWebhookMercadoPagoUC;
import br.com.fiap.siase.domain.enums.StatusOrcamento;
import br.com.fiap.siase.domain.enums.StatusPagamento;
import br.com.fiap.siase.domain.model.Orcamento;
import br.com.fiap.siase.domain.model.Pagamento;
import br.com.fiap.siase.domain.port.BillingEventPublisherPort;
import br.com.fiap.siase.domain.port.OrcamentoRepositoryPort;
import br.com.fiap.siase.domain.port.PagamentoGatewayPort;
import br.com.fiap.siase.domain.port.PagamentoRepositoryPort;
import io.cucumber.java.pt.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BillingSteps {

    private final OrcamentoRepositoryPort orcRepo = mock(OrcamentoRepositoryPort.class);
    private final PagamentoRepositoryPort pagRepo = mock(PagamentoRepositoryPort.class);
    private final PagamentoGatewayPort gateway = mock(PagamentoGatewayPort.class);
    private final BillingEventPublisherPort pub = mock(BillingEventPublisherPort.class);

    private final GerarOrcamentoUC gerarUC = new GerarOrcamentoUC(orcRepo, gateway, pub);
    private final ProcessarWebhookMercadoPagoUC webhookUC = new ProcessarWebhookMercadoPagoUC(pagRepo, orcRepo, gateway, pub);
    private final CancelarOrcamentoUC cancelarUC = new CancelarOrcamentoUC(orcRepo, pagRepo);

    private Orcamento orcamentoAtual;
    private Pagamento pagamentoAtual;
    private int vezesSalvoOrcamento;

    @Dado("que não existe orçamento para a OS {string}")
    public void naoExisteOrcamento(String osNumero) {
        when(orcRepo.findByOsId(any())).thenReturn(Optional.empty());
        when(gateway.criarPreferencia(any(), any(), any(), any()))
                .thenReturn(new PagamentoGatewayPort.PreferenciaResult("PREF-001", "https://mp.com/pay/PREF-001"));
        when(orcRepo.save(any())).thenAnswer(inv -> {
            Orcamento o = inv.getArgument(0);
            o = Orcamento.builder()
                    .id(UUID.randomUUID()).osId("OS-ID-001").osNumero(osNumero)
                    .clienteId("CLI-001").clienteNome("João").clienteEmail("joao@test.com")
                    .valor(o.getValor()).status(StatusOrcamento.PENDENTE)
                    .linkPagamento(o.getLinkPagamento())
                    .mercadoPagoPreferenceId(o.getMercadoPagoPreferenceId())
                    .build();
            orcamentoAtual = o;
            return o;
        });
    }

    @Dado("que já existe orçamento para a OS {string}")
    public void jaExisteOrcamento(String osNumero) {
        orcamentoAtual = Orcamento.builder()
                .id(UUID.randomUUID()).osId("OS-ID-002").osNumero(osNumero)
                .valor(BigDecimal.valueOf(350)).status(StatusOrcamento.PENDENTE).build();
        when(orcRepo.findByOsId(any())).thenReturn(Optional.of(orcamentoAtual));
        vezesSalvoOrcamento = 0;
        when(orcRepo.save(any())).thenAnswer(inv -> { vezesSalvoOrcamento++; return inv.getArgument(0); });
    }

    @Dado("que existe um orçamento pendente para a OS {string}")
    public void existeOrcamentoPendente(String osNumero) {
        orcamentoAtual = Orcamento.builder()
                .id(UUID.randomUUID()).osId("OS-ID-" + osNumero.substring(osNumero.length() - 2))
                .osNumero(osNumero).valor(BigDecimal.valueOf(350))
                .clienteEmail("cliente@test.com")
                .status(StatusOrcamento.PENDENTE).build();
        when(orcRepo.findByOsId(any())).thenReturn(Optional.of(orcamentoAtual));
        when(orcRepo.findByOsNumero(osNumero)).thenReturn(Optional.of(orcamentoAtual));
        when(orcRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pagRepo.findByMercadoPagoPaymentId(any())).thenReturn(Optional.empty());
        when(pagRepo.findByOsId(any())).thenReturn(Optional.empty());
        when(pagRepo.save(any())).thenAnswer(inv -> {
            pagamentoAtual = inv.getArgument(0);
            return pagamentoAtual;
        });
        when(gateway.consultarPagamento(any())).thenReturn(
                new PagamentoGatewayPort.WebhookResult("MP-PAY-001", "approved",
                        BigDecimal.valueOf(350), "pix", orcamentoAtual.getOsNumero()));
    }

    @Dado("que existe um pagamento pendente para a OS {string}")
    public void existePagamentoPendente(String osNumero) {
        pagamentoAtual = Pagamento.builder()
                .id(UUID.randomUUID()).osId("OS-ID-005").osNumero(osNumero)
                .valor(BigDecimal.valueOf(350)).status(StatusPagamento.PENDENTE).build();
        when(pagRepo.findByOsId(any())).thenReturn(Optional.of(pagamentoAtual));
        when(pagRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orcRepo.findByOsId(any())).thenReturn(Optional.empty());
    }

    @Quando("o evento {string} for recebido para a OS {string} com valor {string}")
    public void eventoOsCriada(String tipo, String osNumero, String valor) {
        if ("OS_CRIADA".equals(tipo)) {
            gerarUC.executar("OS-ID-001", osNumero, "CLI-001", "João", "joao@test.com",
                    new BigDecimal(valor));
        }
    }

    @Quando("o evento {string} for recebido novamente para a OS {string} com valor {string}")
    public void eventoOsCriadaNovamente(String tipo, String osNumero, String valor) {
        gerarUC.executar("OS-ID-002", osNumero, "CLI-001", "João", "joao@test.com",
                new BigDecimal(valor));
    }

    @Quando("o webhook do Mercado Pago notificar pagamento aprovado {string}")
    public void webhookAprovado(String mpPaymentId) {
        webhookUC.executar(mpPaymentId, "payment");
    }

    @Quando("o webhook do Mercado Pago notificar pagamento com status {string} {string}")
    public void webhookComStatus(String status, String mpPaymentId) {
        when(gateway.consultarPagamento(mpPaymentId)).thenReturn(
                new PagamentoGatewayPort.WebhookResult(mpPaymentId, status,
                        BigDecimal.valueOf(350), "pix", "OS-20250101-TEST06"));
        when(pagRepo.findByMercadoPagoPaymentId(mpPaymentId)).thenReturn(Optional.empty());
        webhookUC.executar(mpPaymentId, "payment");
    }

    @Quando("o evento {string} for recebido para a OS {string}")
    public void eventoCancelada(String tipo, String osNumero) {
        if ("OS_CANCELADA".equals(tipo)) {
            cancelarUC.executar("OS-ID-" + osNumero.substring(osNumero.length() - 2), "OS cancelada");
        }
    }

    @Então("um orçamento deve ser criado com status {string}")
    public void verificarStatusOrcamento(String status) {
        assertThat(orcamentoAtual).isNotNull();
        assertThat(orcamentoAtual.getStatus().name()).isEqualTo(status);
    }

    @E("o link de pagamento deve estar preenchido")
    public void verificarLinkPagamento() {
        assertThat(orcamentoAtual.getLinkPagamento()).isNotBlank();
    }

    @E("o evento {string} deve ser publicado")
    public void verificarEventoPublicado(String tipo) {
        switch (tipo) {
            case "ORCAMENTO_GERADO" -> verify(pub, atLeastOnce()).publicarOrcamentoGerado(any(), any());
            case "PAGAMENTO_CONFIRMADO" -> verify(pub, atLeastOnce()).publicarPagamentoConfirmado(any(), any(), any(), any());
        }
    }

    @Então("apenas um orçamento deve existir para a OS")
    public void verificarIdempotencia() {
        assertThat(vezesSalvoOrcamento).isZero();
    }

    @Então("o pagamento deve ser registrado com status {string}")
    public void verificarStatusPagamento(String status) {
        assertThat(pagamentoAtual).isNotNull();
        assertThat(pagamentoAtual.getStatus().name()).isEqualTo(status);
    }

    @Então("o orçamento deve ter status {string}")
    public void verificarStatusOrcamentoCancelado(String status) {
        verify(orcRepo, atLeastOnce()).save(argThat(o -> o.getStatus().name().equals(status)));
    }

    @Então("o pagamento deve ter status {string}")
    public void verificarStatusPagamentoCancelado(String status) {
        verify(pagRepo, atLeastOnce()).save(argThat(p -> p.getStatus().name().equals(status)));
    }

    @Então("nenhum pagamento confirmado deve ser registrado")
    public void verificarNenhumPagamento() {
        verify(pagRepo, never()).save(argThat(p -> p.getStatus() == StatusPagamento.PAGO));
    }

    @E("o evento {string} não deve ser publicado")
    public void verificarEventoNaoPublicado(String tipo) {
        if ("PAGAMENTO_CONFIRMADO".equals(tipo))
            verify(pub, never()).publicarPagamentoConfirmado(any(), any(), any(), any());
    }
}
