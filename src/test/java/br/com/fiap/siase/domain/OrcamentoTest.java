package br.com.fiap.siase.domain;

import br.com.fiap.siase.domain.enums.StatusOrcamento;
import br.com.fiap.siase.domain.exception.BusinessException;
import br.com.fiap.siase.domain.model.Orcamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class OrcamentoTest {

    private Orcamento orcamentoPendente() {
        return Orcamento.builder()
                .osId("OS-ID-001").osNumero("OS-20250101-ABC")
                .valor(BigDecimal.valueOf(350)).build();
    }

    @Test
    void deveAprovarOrcamentoPendente() {
        var orc = orcamentoPendente();
        orc.aprovar();
        assertThat(orc.getStatus()).isEqualTo(StatusOrcamento.APROVADO);
    }

    @Test
    void deveRecusarOrcamentoPendente() {
        var orc = orcamentoPendente();
        orc.recusar();
        assertThat(orc.getStatus()).isEqualTo(StatusOrcamento.RECUSADO);
    }

    @Test
    void deveCancelarOrcamento() {
        var orc = orcamentoPendente();
        orc.cancelar();
        assertThat(orc.getStatus()).isEqualTo(StatusOrcamento.CANCELADO);
    }

    @Test
    void naoDeveAprovarOrcamentoJaAprovado() {
        var orc = orcamentoPendente();
        orc.aprovar();
        assertThatThrownBy(orc::aprovar)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pendentes");
    }

    @Test
    void naoDeveCancelarOrcamentoJaCancelado() {
        var orc = orcamentoPendente();
        orc.cancelar();
        assertThatThrownBy(orc::cancelar)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cancelado");
    }
}
