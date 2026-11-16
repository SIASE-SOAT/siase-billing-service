package br.com.fiap.siase.infrastructure.adpters.output.jpa;


import br.com.fiap.siase.application.ports.OrcamentoRepositoryPort;
import br.com.fiap.siase.application.ports.PagamentoRepositoryPort;
import br.com.fiap.siase.domain.model.Orcamento;
import br.com.fiap.siase.domain.model.Pagamento;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class BillingRepositoryAdapters {

  @Component
  static class OrcamentoAdapter implements OrcamentoRepositoryPort {

    private final OrcamentoJpaRepository jpa;

    OrcamentoAdapter(OrcamentoJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public Orcamento salvar(Orcamento o) { return paraDominio(jpa.save(paraEntidade(o))); }

    @Override
    public Optional<Orcamento> buscarPorId(UUID id) { return jpa.findById(id).map(this::paraDominio); }

    @Override
    public Optional<Orcamento> buscarPorOsId(UUID osId) { return jpa.findByOsId(osId).map(this::paraDominio); }

    private OrcamentoJpaEntity paraEntidade(Orcamento o) {
      OrcamentoJpaEntity e = new OrcamentoJpaEntity();
      e.setId(o.getId()); e.setOsId(o.getOsId()); e.setNumeroOs(o.getNumeroOs());
      e.setValor(o.getValor()); e.setStatus(o.getStatus());
      e.setPreferenciaMpId(o.getPreferenciaMpId()); e.setLinkPagamento(o.getLinkPagamento());
      e.setCriadoEm(o.getCriadoEm()); e.setAtualizadoEm(o.getAtualizadoEm());
      return e;
    }

    private Orcamento paraDominio(OrcamentoJpaEntity e) {
      Orcamento o = new Orcamento(e.getId(), e.getOsId(), e.getNumeroOs(), e.getValor(),
              e.getStatus(), e.getCriadoEm(), e.getAtualizadoEm());
      if (e.getPreferenciaMpId() != null) {
        o.restaurarPreferenciaPersistida(e.getPreferenciaMpId(), e.getLinkPagamento());
      }
      return o;
    }
  }

  @Component
  static class PagamentoAdapter implements PagamentoRepositoryPort {

    private final PagamentoJpaRepository jpa;

    PagamentoAdapter(PagamentoJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public Pagamento salvar(Pagamento p) { return paraDominio(jpa.save(paraEntidade(p))); }

    @Override
    public Optional<Pagamento> buscarPorId(UUID id) { return jpa.findById(id).map(this::paraDominio); }

    @Override
    public Optional<Pagamento> buscarPorOsId(UUID osId) { return jpa.findByOsId(osId).map(this::paraDominio); }

    @Override
    public Optional<Pagamento> buscarPorMpPaymentId(String mp) { return jpa.findByMpPaymentId(mp).map(this::paraDominio); }

    private PagamentoJpaEntity paraEntidade(Pagamento p) {
      PagamentoJpaEntity e = new PagamentoJpaEntity();
      e.setId(p.getId()); e.setOrcamentoId(p.getOrcamentoId()); e.setOsId(p.getOsId());
      e.setValor(p.getValor()); e.setMpPaymentId(p.getMpPaymentId());
      e.setStatus(p.getStatus()); e.setCriadoEm(p.getCriadoEm()); e.setAtualizadoEm(p.getAtualizadoEm());
      return e;
    }

    private Pagamento paraDominio(PagamentoJpaEntity e) {
      return new Pagamento(e.getId(), e.getOrcamentoId(), e.getOsId(), e.getValor(),
              e.getMpPaymentId(), e.getStatus(), e.getCriadoEm(), e.getAtualizadoEm());
    }
  }
}
