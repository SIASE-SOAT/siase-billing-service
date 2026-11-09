package br.com.fiap.siase.application.dto.output;

import br.com.fiap.siase.domain.model.Orcamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrcamentoResponse(
        UUID id, String osId, String osNumero, String clienteNome,
        BigDecimal valor, String status, String linkPagamento, LocalDateTime criadoEm
) {
    public static OrcamentoResponse from(Orcamento o) {
        return new OrcamentoResponse(o.getId(), o.getOsId(), o.getOsNumero(),
                o.getClienteNome(), o.getValor(), o.getStatus().name(),
                o.getLinkPagamento(), o.getCriadoEm());
    }
}
