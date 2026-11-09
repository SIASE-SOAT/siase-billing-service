# language: pt
Funcionalidade: Fluxo de Orçamento e Pagamento
  Como sistema de billing da oficina
  Quero gerenciar orçamentos e pagamentos das OS
  Para garantir que o fluxo financeiro funcione corretamente

  Cenário: Gerar orçamento ao receber evento OS_CRIADA
    Dado que não existe orçamento para a OS "OS-20250101-TEST01"
    Quando o evento "OS_CRIADA" for recebido para a OS "OS-20250101-TEST01" com valor "350.00"
    Então um orçamento deve ser criado com status "PENDENTE"
    E o link de pagamento deve estar preenchido
    E o evento "ORCAMENTO_GERADO" deve ser publicado

  Cenário: Idempotência — não gerar orçamento duplicado
    Dado que já existe orçamento para a OS "OS-20250101-TEST02"
    Quando o evento "OS_CRIADA" for recebido novamente para a OS "OS-20250101-TEST02" com valor "350.00"
    Então apenas um orçamento deve existir para a OS

  Cenário: Confirmar pagamento via webhook Mercado Pago
    Dado que existe um orçamento pendente para a OS "OS-20250101-TEST03"
    Quando o webhook do Mercado Pago notificar pagamento aprovado "MP-PAY-001"
    Então o pagamento deve ser registrado com status "PAGO"
    E o evento "PAGAMENTO_CONFIRMADO" deve ser publicado

  Cenário: Cancelar orçamento quando OS for cancelada
    Dado que existe um orçamento pendente para a OS "OS-20250101-TEST04"
    Quando o evento "OS_CANCELADA" for recebido para a OS "OS-20250101-TEST04"
    Então o orçamento deve ter status "CANCELADO"

  Cenário: Cancelar pagamento pendente quando OS for cancelada
    Dado que existe um pagamento pendente para a OS "OS-20250101-TEST05"
    Quando o evento "OS_CANCELADA" for recebido para a OS "OS-20250101-TEST05"
    Então o pagamento deve ter status "CANCELADO"

  Cenário: Pagamento com status não aprovado não deve ser confirmado
    Dado que existe um orçamento pendente para a OS "OS-20250101-TEST06"
    Quando o webhook do Mercado Pago notificar pagamento com status "pending" "MP-PAY-002"
    Então nenhum pagamento confirmado deve ser registrado
    E o evento "PAGAMENTO_CONFIRMADO" não deve ser publicado
