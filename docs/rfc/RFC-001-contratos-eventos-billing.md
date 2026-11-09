# RFC-001 — Contratos de Eventos SQS do Billing Service

**Status:** Aprovado  
**Data:** 2025-01

## Resumo

Define os eventos que o Billing Service consome e produz, e o contrato de integração com o Mercado Pago.

## Eventos Consumidos

### OS_CRIADA (fila: `siase-billing-queue`)
```json
{
  "tipo": "OS_CRIADA",
  "osId": "uuid",
  "osNumero": "OS-20250101-ABC123",
  "clienteId": "uuid",
  "clienteNome": "João Silva",
  "clienteEmail": "joao@email.com",
  "veiculoId": "uuid",
  "total": "350.00",
  "status": "RECEBIDA"
}
```
**Ação:** Gerar orçamento + criar preferência no Mercado Pago.

### OS_CANCELADA (fila: `siase-billing-queue`)
```json
{
  "tipo": "OS_CANCELADA",
  "osId": "uuid",
  "osNumero": "OS-20250101-ABC123",
  "motivo": "Cliente desistiu"
}
```
**Ação:** Cancelar orçamento e pagamento pendentes (compensação Saga).

### ORCAMENTO_RECUSADO (fila: `siase-billing-queue`)
```json
{
  "tipo": "ORCAMENTO_RECUSADO",
  "osId": "uuid",
  "osNumero": "OS-20250101-ABC123"
}
```
**Ação:** Cancelar orçamento.

## Eventos Produzidos

### ORCAMENTO_GERADO (fila: `siase-os-updates-queue`)
```json
{
  "tipo": "ORCAMENTO_GERADO",
  "osNumero": "OS-20250101-ABC123",
  "osId": "uuid"
}
```
**Efeito:** OS Service avança OS para `AGUARDANDO_APROVACAO`.

### PAGAMENTO_CONFIRMADO (fila: `siase-os-updates-queue`)
```json
{
  "tipo": "PAGAMENTO_CONFIRMADO",
  "osNumero": "OS-20250101-ABC123",
  "osId": "uuid",
  "valor": "350.00",
  "formaPagamento": "pix"
}
```
**Efeito:** OS Service avança OS para `ENTREGUE`.

### PAGAMENTO_FALHOU (fila: `siase-os-updates-queue`)
```json
{
  "tipo": "PAGAMENTO_FALHOU",
  "osNumero": "OS-20250101-ABC123",
  "osId": "uuid"
}
```
**Efeito:** OS permanece em `FINALIZADA` aguardando nova tentativa.

## Idempotência

O campo `mercado_pago_payment_id` é UNIQUE no banco. Processar o mesmo webhook duas vezes não cria duplicatas.

## Webhook Mercado Pago

- Endpoint: `POST /api/billing/webhook/mercadopago`
- Público (sem JWT) — validação feita consultando o pagamento diretamente na API do MP
- Apenas notificações com `type == "payment"` são processadas
- Status `approved` confirma o pagamento; demais status são ignorados aguardando aprovação
