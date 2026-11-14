# language: pt
Funcionalidade: Billing no Saga coreografado
  Como microsserviço de orçamentos e pagamentos
  Quero reagir aos eventos da OS e publicar atualizações
  Para completar o fluxo do Saga com o Mercado Pago

  Cenário: OS criada gera orçamento com QR Code Pix
    Dado um evento "OS_CRIADA" recebido
    Quando o billing processa o evento
    Então um orçamento é gerado com QR Code Pix Mercado Pago
    E o evento "ORCAMENTO_GERADO" é publicado para o os-service

  Cenário: Pagamento aprovado no Mercado Pago confirma a OS
    Dado um orçamento existente para a OS
    Quando o webhook do Mercado Pago confirma o pagamento
    Então o pagamento fica "CONFIRMADO"
    E o evento "PAGAMENTO_CONFIRMADO" é publicado para o os-service

  Cenário: Cancelamento reembolsa pagamento confirmado
    Dado um orçamento pago para a OS
    Quando o evento "OS_CANCELADA" é recebido
    Então o pagamento é reembolsado no Mercado Pago
    E o evento "PAGAMENTO_CANCELADO" é publicado para o os-service
