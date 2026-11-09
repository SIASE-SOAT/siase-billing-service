# ADR-001 — Integração com Mercado Pago e Webhook

**Status:** Aceito  
**Data:** 2025-01

## Decisão

Usar o **Mercado Pago SDK Java** para criar preferências de pagamento e processar webhooks.

## Fluxo de Webhook

O Mercado Pago envia notificações POST para `/api/billing/webhook/mercadopago` com o `payment_id`. O serviço consulta o status via API e confirma o pagamento apenas quando `status == "approved"`.

## Idempotência

O campo `mercado_pago_payment_id` é UNIQUE no banco. Processar o mesmo webhook duas vezes não cria duplicatas.

## Segurança

O endpoint de webhook é público (sem JWT) pois o Mercado Pago não envia tokens. A validação é feita consultando o pagamento diretamente na API do MP com o access token.
