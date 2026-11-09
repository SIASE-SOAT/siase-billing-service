package br.com.fiap.siase.application;

import br.com.fiap.siase.application.usecase.GerarOrcamentoUC;
import br.com.fiap.siase.domain.enums.StatusOrcamento;
import br.com.fiap.siase.domain.model.Orcamento;
import br.com.fiap.siase.domain.port.BillingEventPublisherPort;
import br.com.fiap.siase.domain.port.OrcamentoRepositoryPort;
import br.com.fiap.siase.domain.port.PagamentoGatewayPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GerarOrcamentoUCTest {

    @Mock OrcamentoRepositoryPort orcRepo;
    @Mock PagamentoGatewayPort gateway;
    @Mock BillingEventPublisherPort pub;

    GerarOrcamentoUC useCase;

    @BeforeEach
    void setUp() { useCase = new GerarOrcamentoUC(orcRepo, gateway, pub); }

    @Test
    void deveGerarOrcamentoComSucesso() {
        when(orcRepo.findByOsId(any())).thenReturn(Optional.empty());
        when(gateway.criarPreferencia(any(), any(), any(), any()))
                .thenReturn(new PagamentoGatewayPort.PreferenciaResult("PREF-001", "https://mp.com/pay"));
        when(orcRepo.save(any())).thenAnswer(inv -> {
            Orcamento o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        var result = useCase.executar("OS-ID-001", "OS-20250101-ABC", "CLI-001",
                "João", "joao@test.com", BigDecimal.valueOf(350));

        assertThat(result.getStatus()).isEqualTo(StatusOrcamento.PENDENTE);
        assertThat(result.getLinkPagamento()).isEqualTo("https://mp.com/pay");
        verify(pub).publicarOrcamentoGerado(any(), any());
    }

    @Test
    void deveRetornarExistenteQuandoOrcamentoJaExiste() {
        var existente = Orcamento.builder().id(UUID.randomUUID())
                .osId("OS-ID-001").osNumero("OS-20250101-ABC")
                .valor(BigDecimal.valueOf(350)).status(StatusOrcamento.PENDENTE).build();
        when(orcRepo.findByOsId("OS-ID-001")).thenReturn(Optional.of(existente));

        var result = useCase.executar("OS-ID-001", "OS-20250101-ABC", "CLI-001",
                "João", "joao@test.com", BigDecimal.valueOf(350));

        assertThat(result.getId()).isEqualTo(existente.getId());
        verify(gateway, never()).criarPreferencia(any(), any(), any(), any());
        verify(pub, never()).publicarOrcamentoGerado(any(), any());
    }

    @Test
    void naoDevePublicarEventoQuandoGatewayFalha() {
        when(orcRepo.findByOsId(any())).thenReturn(Optional.empty());
        when(gateway.criarPreferencia(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("MP indisponível"));

        assertThatThrownBy(() -> useCase.executar("OS-ID-001", "OS-20250101-ABC",
                "CLI-001", "João", "joao@test.com", BigDecimal.valueOf(350)))
                .isInstanceOf(RuntimeException.class);
        verify(pub, never()).publicarOrcamentoGerado(any(), any());
    }
}
