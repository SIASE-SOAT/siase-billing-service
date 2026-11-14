# ADR-001 — Saga coreografado e integração Mercado Pago

## Status
Aceito

## Decisão
O billing participa do **Saga coreografado via SQS** (ver ADR-001 do
os-service): consome `billing-queue` e publica atualizações em
`os-updates-queue`. Pagamentos são processados pelo **Mercado Pago** via
**Pix com QR Code dinâmico** (`POST /v1/payments` com `payment_method_id=pix`,
retornando `qr_code` — Pix copia e cola — e `qr_code_base64`) e webhook de
confirmação.

## Compensação
- `OS_CANCELADA` ou `ORCAMENTO_RECUSADO` → cancela o orçamento; se o pagamento
  já foi confirmado, emite reembolso no MP e publica `PAGAMENTO_CANCELADO`.

## Pontos de desenho
- **Idempotência**: `OS_CRIADA` duplicado não gera segundo orçamento
  (constraint única em `os_id`); webhook reenviado não confirma duas vezes
  (lookup por `mp_payment_id`).
- **external_reference = osId**: o pagamento Pix carrega o osId, que volta no
  pagamento consultado — é como o webhook localiza o orçamento certo.
- **Idempotência na criação**: header `X-Idempotency-Key = orcamentoId` evita
  cobrança duplicada em reenvios.
- **Mock em dev**: `MERCADOPAGO_MOCK=true` simula pagamento/consulta, o que
  permite demonstrar o fluxo completo sem credenciais (desligado em prod).
- **Ressalva de segurança**: o webhook aceita POST público (sem JWT) — em
  produção deve-se validar a assinatura `x-signature` do Mercado Pago
  (HMAC com o secret do webhook) antes de processar. Registrado como
  melhoria no backlog.