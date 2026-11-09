# Diagramas de Sequência — siase-billing-service

---

## 1. Geração de Orçamento (evento OS_CRIADA)

```
SQS billing-queue   SqsBillingConsumer   GerarOrcamentoUC   MercadoPagoAdapter   OrcamentoRepo   SQS os-updates
        │                   │                    │                   │                  │               │
        │── mensagem ───────►│                    │                   │                  │               │
        │                   │── executar() ──────►│                   │                  │               │
        │                   │                    │── findByOsId ─────────────────────────►│               │
        │                   │                    │◄── Optional.empty ─────────────────────│               │
        │                   │                    │── criarPreferencia ►│                  │               │
        │                   │                    │◄── {preferenceId,  │                  │               │
        │                   │                    │     linkPagamento} │                  │               │
        │                   │                    │── save(orcamento) ─────────────────────►│               │
        │                   │                    │◄── orcamento salvo ─────────────────────│               │
        │                   │                    │── publicarOrcamentoGerado ──────────────────────────────►│
        │                   │◄── orcamento ──────│                   │                  │               │
        │                   │── deleteMessage ───►│                   │                  │               │
```

---

## 2. Idempotência — OS_CRIADA duplicada

```
SQS billing-queue   SqsBillingConsumer   GerarOrcamentoUC   OrcamentoRepo
        │                   │                    │                  │
        │── mensagem ───────►│                    │                  │
        │                   │── executar() ──────►│                  │
        │                   │                    │── findByOsId ────►│
        │                   │                    │◄── Optional.of() ─│  (já existe)
        │                   │                    │── retorna existente│
        │                   │◄── orcamento ──────│                  │
        │                   │── deleteMessage ───►│                  │
        │                   │  (sem chamar MP,    │                  │
        │                   │   sem publicar      │                  │
        │                   │   evento)           │                  │
```

---

## 3. Confirmação de Pagamento via Webhook Mercado Pago

```
Mercado Pago   BillingController   ProcessarWebhookUC   MercadoPagoAdapter   PagamentoRepo   SQS os-updates
      │                │                   │                   │                   │               │
      │── POST /webhook►│                   │                   │                  │               │
      │                │── executar(id) ───►│                   │                  │               │
      │                │                   │── findByMpId ─────────────────────────►│               │
      │                │                   │◄── Optional.empty ─────────────────────│               │
      │                │                   │── consultarPagamento ►│                │               │
      │                │                   │◄── {status:"approved"}│                │               │
      │                │                   │── pagamento.confirmar()│                │               │
      │                │                   │── save(pagamento) ─────────────────────►│               │
      │                │                   │── publicarPagamentoConfirmado ──────────────────────────►│
      │                │◄── 200 OK ────────│                   │                  │               │
      │◄── 200 OK ─────│                   │                   │                  │               │
```

---

## 4. Compensação — OS_CANCELADA

```
SQS billing-queue   SqsBillingConsumer   CancelarOrcamentoUC   OrcamentoRepo   PagamentoRepo
        │                   │                    │                   │               │
        │── OS_CANCELADA ───►│                    │                   │               │
        │                   │── executar() ──────►│                   │               │
        │                   │                    │── findByOsId ─────►│               │
        │                   │                    │◄── orcamento ──────│               │
        │                   │                    │── orc.cancelar()   │               │
        │                   │                    │── save(orc) ───────►│               │
        │                   │                    │── findByOsId ──────────────────────►│
        │                   │                    │◄── pagamento ──────────────────────│
        │                   │                    │── pag.cancelar()   │               │
        │                   │                    │── save(pag) ───────────────────────►│
        │                   │◄── void ───────────│                   │               │
        │                   │── deleteMessage ───►│                   │               │
```
