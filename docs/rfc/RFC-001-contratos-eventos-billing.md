# RFC-001 — Contratos de Eventos SQS do Billing Service

**Status:** Aprovado
**Data:** 2025-01

## Resumo

Define os eventos que o Billing Service consome e produz. O envelope de todos os eventos segue o padrão do Saga (ver RFC-001 do os-service): `tipoEvento`, `osId`, `numeroOs`, `payload` (JSON string opcional), `ocorridoEm`.

## Eventos Consumidos (fila: `siase-billing-queue`)

### OS_CRIADA
```json
{
  "tipoEvento": "OS_CRIADA",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "{\"clienteNome\":\"João Silva\",\"veiculo\":\"Fiat Uno\",\"valorOrcamento\":350.00,\"servicos\":[{\"descricao\":\"Troca de óleo\",\"preco\":80.00}]}",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Ação:** Gerar orçamento + criar pagamento Pix no Mercado Pago. Idempotente: `os_id` UNIQUE em `orcamentos`.

### OS_CANCELADA
```json
{
  "tipoEvento": "OS_CANCELADA",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": null,
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Ação:** Cancelar orçamento e pagamento pendentes (compensação Saga). Se pagamento já confirmado, emite reembolso no MP e publica `PAGAMENTO_CANCELADO`.

### ORCAMENTO_RECUSADO
```json
{
  "tipoEvento": "ORCAMENTO_RECUSADO",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": null,
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Ação:** Cancelar orçamento (mesma lógica de compensação do `OS_CANCELADA`).

## Eventos Produzidos (fila: `siase-os-updates-queue`)

### ORCAMENTO_GERADO
```json
{
  "tipoEvento": "ORCAMENTO_GERADO",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "{\"orcamentoId\":\"uuid\",\"valor\":350.00,\"paymentId\":\"pay-mp-123\",\"qrCodePix\":\"00020101...\",\"qrCodePixBase64\":\"bW9ja...\"}",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Efeito:** os-service avança OS para `AGUARDANDO_APROVACAO`.

### PAGAMENTO_CONFIRMADO
```json
{
  "tipoEvento": "PAGAMENTO_CONFIRMADO",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": "{\"pagamentoId\":\"uuid\"}",
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Efeito:** os-service avança OS para `ENTREGUE`.

### PAGAMENTO_CANCELADO
```json
{
  "tipoEvento": "PAGAMENTO_CANCELADO",
  "osId": "uuid",
  "numeroOs": "OS-XXXXXXXX",
  "payload": null,
  "ocorridoEm": "2025-01-01T10:00:00Z"
}
```
**Efeito:** os-service registra no histórico; OS já está `CANCELADA`.

## Idempotência

- `os_id` é UNIQUE na tabela `orcamentos` — `OS_CRIADA` duplicado retorna o orçamento existente sem chamar o MP novamente.
- `mp_payment_id` é UNIQUE na tabela `pagamentos` — webhook reenviado pelo MP é ignorado (lookup por `mpPaymentId` antes de processar).

## Webhook Mercado Pago

- Endpoint: `POST /api/billing/webhook/mercadopago`
- Público (sem JWT) — o MP exige resposta 200 imediata
- Aceita dois formatos: IPN (`?type=payment&id=...`) e novo formato (body JSON com `type` e `data.id`)
- Apenas pagamentos com `status == "approved"` são confirmados; demais são ignorados
- Em produção: validar assinatura `x-signature` do MP (HMAC) antes de processar — registrado como melhoria no backlog
