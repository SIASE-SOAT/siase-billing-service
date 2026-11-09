CREATE TABLE IF NOT EXISTS orcamentos (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    os_id                       VARCHAR(36)    NOT NULL UNIQUE,
    os_numero                   VARCHAR(30)    NOT NULL,
    cliente_id                  VARCHAR(36)    NOT NULL,
    cliente_nome                VARCHAR(255)   NOT NULL,
    cliente_email               VARCHAR(255),
    valor                       NUMERIC(10,2)  NOT NULL,
    status                      VARCHAR(20)    NOT NULL DEFAULT 'PENDENTE',
    link_pagamento              VARCHAR(500),
    mercado_pago_preference_id  VARCHAR(100),
    criado_em                   TIMESTAMP      NOT NULL DEFAULT NOW(),
    atualizado_em               TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_status_orc CHECK (status IN ('PENDENTE','APROVADO','RECUSADO','CANCELADO'))
);

CREATE TABLE IF NOT EXISTS pagamentos (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    os_id                       VARCHAR(36)    NOT NULL,
    os_numero                   VARCHAR(30)    NOT NULL,
    cliente_email               VARCHAR(255),
    valor                       NUMERIC(10,2)  NOT NULL,
    forma_pagamento             VARCHAR(50),
    mercado_pago_payment_id     VARCHAR(100)   UNIQUE,
    status                      VARCHAR(20)    NOT NULL DEFAULT 'PENDENTE',
    data_pagamento              TIMESTAMP,
    criado_em                   TIMESTAMP      NOT NULL DEFAULT NOW(),
    atualizado_em               TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_status_pag CHECK (status IN ('PENDENTE','PAGO','FALHOU','CANCELADO'))
);

CREATE INDEX IF NOT EXISTS idx_orcamentos_os_id ON orcamentos(os_id);
CREATE INDEX IF NOT EXISTS idx_pagamentos_os_id ON pagamentos(os_id);
CREATE INDEX IF NOT EXISTS idx_pagamentos_mp_id ON pagamentos(mercado_pago_payment_id);
