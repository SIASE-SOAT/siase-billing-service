package br.com.fiap.siase.application.dto.output;

import br.com.fiap.siase.domain.model.Pagamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoResponse(
        UUID id, String osId, String osNumero, BigDecimal valor,
        String formaPagamento, String status, LocalDateTime dataPagamento, LocalDateTime criadoEm
) {
    public static PagamentoResponse from(Pagamento p) {
        return new PagamentoResponse(p.getId(), p.getOsId(), p.getOsNumero(),
                p.getValor(), p.getFormaPagamento(), p.getStatus().name(),
                p.getDataPagamento(), p.getCriadoEm());
    }
}
