package br.com.fiap.siase.infrastructure.config;

import br.com.fiap.siase.application.usecase.*;
import br.com.fiap.siase.domain.port.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public GerarOrcamentoUC gerarOrcamentoUC(OrcamentoRepositoryPort orcRepo,
                                              PagamentoGatewayPort gateway,
                                              BillingEventPublisherPort pub) {
        return new GerarOrcamentoUC(orcRepo, gateway, pub);
    }

    @Bean
    public ProcessarWebhookMercadoPagoUC processarWebhookUC(PagamentoRepositoryPort pagRepo,
                                                              OrcamentoRepositoryPort orcRepo,
                                                              PagamentoGatewayPort gateway,
                                                              BillingEventPublisherPort pub) {
        return new ProcessarWebhookMercadoPagoUC(pagRepo, orcRepo, gateway, pub);
    }

    @Bean
    public CancelarOrcamentoUC cancelarOrcamentoUC(OrcamentoRepositoryPort orcRepo,
                                                    PagamentoRepositoryPort pagRepo) {
        return new CancelarOrcamentoUC(orcRepo, pagRepo);
    }

    @Bean
    public ConsultarBillingUC consultarBillingUC(OrcamentoRepositoryPort orcRepo,
                                                  PagamentoRepositoryPort pagRepo) {
        return new ConsultarBillingUC(orcRepo, pagRepo);
    }
}
