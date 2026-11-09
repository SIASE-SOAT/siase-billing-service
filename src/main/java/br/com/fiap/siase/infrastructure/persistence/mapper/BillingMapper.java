package br.com.fiap.siase.infrastructure.persistence.mapper;

import br.com.fiap.siase.domain.model.Orcamento;
import br.com.fiap.siase.domain.model.Pagamento;
import br.com.fiap.siase.infrastructure.persistence.entity.OrcamentoEntity;
import br.com.fiap.siase.infrastructure.persistence.entity.PagamentoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BillingMapper {
    Orcamento toDomain(OrcamentoEntity e);
    OrcamentoEntity toEntity(Orcamento d);
    Pagamento toDomain(PagamentoEntity e);
    PagamentoEntity toEntity(Pagamento d);
}
