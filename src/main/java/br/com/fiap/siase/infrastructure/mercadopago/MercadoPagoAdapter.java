package br.com.fiap.siase.infrastructure.mercadopago;

import br.com.fiap.siase.domain.port.PagamentoGatewayPort;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class MercadoPagoAdapter implements PagamentoGatewayPort {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.success-url}")
    private String successUrl;

    @Value("${mercadopago.failure-url}")
    private String failureUrl;

    @Value("${mercadopago.notification-url}")
    private String notificationUrl;

    @Override
    public PreferenciaResult criarPreferencia(String osNumero, String clienteEmail,
                                               String clienteNome, BigDecimal valor) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            var item = PreferenceItemRequest.builder()
                    .title("Ordem de Serviço " + osNumero)
                    .quantity(1)
                    .unitPrice(valor)
                    .build();

            var payer = PreferencePayerRequest.builder()
                    .email(clienteEmail)
                    .build();

            var backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .failure(failureUrl)
                    .build();

            var request = PreferenceRequest.builder()
                    .items(List.of(item))
                    .payer(payer)
                    .backUrls(backUrls)
                    .notificationUrl(notificationUrl)
                    .externalReference(osNumero)
                    .autoReturn("approved")
                    .build();

            var client = new PreferenceClient();
            var preference = client.create(request);

            log.info("Preferência MP criada para OS {}: {}", osNumero, preference.getId());
            return new PreferenciaResult(preference.getId(), preference.getInitPoint());

        } catch (Exception e) {
            log.error("Erro ao criar preferência no Mercado Pago para OS {}: {}", osNumero, e.getMessage(), e);
            throw new RuntimeException("Falha na integração com Mercado Pago: " + e.getMessage(), e);
        }
    }

    @Override
    public WebhookResult consultarPagamento(String paymentId) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            var client = new PaymentClient();
            var payment = client.get(Long.parseLong(paymentId));

            String formaPagamento = payment.getPaymentMethodId() != null
                    ? payment.getPaymentMethodId() : "desconhecido";
            String externalReference = payment.getExternalReference() != null
                    ? payment.getExternalReference() : "";

            return new WebhookResult(
                    paymentId,
                    payment.getStatus(),
                    payment.getTransactionAmount(),
                    formaPagamento,
                    externalReference
            );
        } catch (Exception e) {
            log.error("Erro ao consultar pagamento {} no Mercado Pago: {}", paymentId, e.getMessage(), e);
            throw new RuntimeException("Falha ao consultar pagamento no Mercado Pago: " + e.getMessage(), e);
        }
    }
}
