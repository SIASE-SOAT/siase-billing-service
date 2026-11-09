package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.domain.port.OrcamentoRepositoryPort;
import br.com.fiap.siase.domain.port.PagamentoRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CancelarOrcamentoUC {

    private final OrcamentoRepositoryPort orcamentoRepository;
    private final PagamentoRepositoryPort pagamentoRepository;

    public void executar(String osId, String motivo) {
        orcamentoRepository.findByOsId(osId).ifPresent(orc -> {
            try {
                orc.cancelar();
                orcamentoRepository.save(orc);
                log.info("Orçamento cancelado para OS {} — motivo: {}", orc.getOsNumero(), motivo);
            } catch (Exception e) {
                log.warn("Não foi possível cancelar orçamento para OS {}: {}", osId, e.getMessage());
            }
        });

        pagamentoRepository.findByOsId(osId).ifPresent(pag -> {
            try {
                pag.cancelar();
                pagamentoRepository.save(pag);
                log.info("Pagamento cancelado para OS {}", pag.getOsNumero());
            } catch (Exception e) {
                log.warn("Não foi possível cancelar pagamento para OS {}: {}", osId, e.getMessage());
            }
        });
    }
}
