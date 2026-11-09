# siase-billing-service

> SIASE — Fase 4 | Microsserviço de Orçamentos e Pagamentos

## Descrição

Responsável por gerar orçamentos a partir de OS criadas, integrar com o **Mercado Pago** para processamento de pagamentos e publicar eventos de confirmação via **AWS SQS** (Saga Coreografado).

## Responsabilidades

- Consumir evento `OS_CRIADA` → gerar orçamento + preferência Mercado Pago
- Receber webhook do Mercado Pago → confirmar pagamento
- Publicar `PAGAMENTO_CONFIRMADO` → OS Service avança para ENTREGUE
- Compensação: cancelar orçamento/pagamento ao receber `OS_CANCELADA`

## Fluxo de Pagamento

```
OS_CRIADA (SQS) → GerarOrcamentoUC → Mercado Pago API
                                           │
                                    link de pagamento
                                           │
                                    cliente paga
                                           │
                              Mercado Pago Webhook
                                           │
                              ProcessarWebhookUC
                                           │
                              PAGAMENTO_CONFIRMADO (SQS)
                                           │
                              OS Service → ENTREGUE
```

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.4 |
| PostgreSQL | 16 (RDS) |
| Mercado Pago SDK | 2.1.24 |
| AWS SQS | SDK v2 |
| Cucumber | 7.15 (BDD) |
| JaCoCo | 80% mínimo |

## Endpoints

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| GET | `/api/ordens/{osId}/orcamento` | JWT | Consultar orçamento |
| GET | `/api/ordens/{osId}/pagamento` | JWT | Consultar pagamento |
| GET | `/api/pagamentos/{id}` | JWT | Pagamento por ID |
| POST | `/api/billing/webhook/mercadopago` | — | Webhook MP |

## Variáveis de Ambiente

| Variável | Descrição |
|---|---|
| `DB_HOST` | Host RDS PostgreSQL |
| `DB_NAME` | `siase_billing_db` |
| `MERCADOPAGO_ACCESS_TOKEN` | Token de acesso MP |
| `BILLING_PUBLIC_URL` | URL pública para webhook MP |
| `SQS_BILLING_URL` | Fila de entrada (OS_CRIADA) |
| `SQS_OS_UPDATES_URL` | Fila de saída (PAGAMENTO_CONFIRMADO) |

## Testes

```bash
mvn clean verify
open target/site/jacoco/index.html
open target/cucumber-reports/report.html
```

## Documentação

- [ADR-001 — Microsserviços e Saga](docs/adr/ADR-001-billing-saga.md)
- [RFC-001 — Integração Mercado Pago](docs/rfc/RFC-001-mercado-pago.md)
