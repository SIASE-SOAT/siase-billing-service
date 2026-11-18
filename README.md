# siase-billing-service — SIASE Fase 4

Microsserviço de **Orçamentos e Pagamentos**: gera orçamento a partir de OS
criada, integra com **Mercado Pago** para pagamento via **Pix com QR Code
dinâmico** e confirma o pagamento via webhook. Participa do **Saga
coreografado via AWS SQS** (ver `docs/adr/ADR-001`).

## Stack

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | LTS |
| Spring Boot | 3.2.4 | Web + JPA + Security (JWT resource server) |
| PostgreSQL | 16 (RDS) | Banco próprio (`siase_billing_db`) |
| AWS SQS | AWS SDK v2 | Mensageria do Saga |
| Mercado Pago | API REST | Preferência de pagamento + webhook + reembolso |
| Flyway | — | Migrations |
| Cucumber | 7.15 | BDD |
| JaCoCo | 0.8.11 | Cobertura mínima 80% |

## Fluxo de pagamento

```
OS_CRIADA (SQS) → GerarOrcamentoUC → POST /v1/payments (Pix)
                                        │
                              qr_code + qr_code_base64
                              (Pix copia e cola + QR dinâmico)
                                        │
                              cliente paga via Pix
                                        │
                              webhook MP → ProcessarWebhookUC
                                        │
                              PAGAMENTO_CONFIRMADO (SQS) → OS: ENTREGUE
```

Compensação: `OS_CANCELADA`/`ORCAMENTO_RECUSADO` → cancela orçamento e
reembolsa pagamento confirmado → `PAGAMENTO_CANCELADO`.

## Filas SQS

| Fila | Direção | Eventos |
|---|---|---|
| `siase-*-billing-queue` | consome | OS_CRIADA, OS_CANCELADA, ORCAMENTO_RECUSADO |
| `siase-*-os-updates-queue` | produz | ORCAMENTO_GERADO, PAGAMENTO_CONFIRMADO, PAGAMENTO_CANCELADO |

## Endpoints

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| GET | `/api/ordens/{osId}/orcamento` | JWT | Orçamento da OS |
| GET | `/api/ordens/{osId}/pagamento` | JWT | Pagamento da OS |
| GET | `/api/pagamentos/{id}` | JWT | Pagamento por ID |
| POST | `/api/billing/webhook/mercadopago` | — | Webhook MP (IPN + formato novo) |

Swagger UI: `/api/swagger-ui.html`

## Variáveis de ambiente

`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`,
`AWS_REGION`, `SQS_BILLING_URL`, `SQS_OS_UPDATES_URL`,
`MERCADOPAGO_ACCESS_TOKEN`, `MERCADOPAGO_MOCK` (true em dev),
`MERCADOPAGO_PAYER_EMAIL` (email do pagador para a API Pix),
`BILLING_PUBLIC_URL` (URL pública para o webhook MP), `ENVIRONMENT`.

## Porta

**8082** (management/actuator em `/actuator`)

## Testes

```bash
mvn clean verify
# Cobertura: target/site/jacoco/index.html (gate 80%)
# BDD:       target/cucumber-reports/report.html
```

## Documentação

- [ADR-001 — Saga Coreografado e integração Mercado Pago](docs/adr/ADR-001-billing-saga.md)
- [RFC-001 — Contratos de Eventos SQS do Billing Service](docs/rfc/RFC-001-contratos-eventos-billing.md)
- [Diagramas de Sequência](docs/diagramas-sequencia.md)
