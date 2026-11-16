CREATE TABLE orcamentos (
    id               UUID PRIMARY KEY,
    os_id            UUID          NOT NULL UNIQUE,
    numero_os        VARCHAR(32)   NOT NULL,
    valor            NUMERIC(12,2) NOT NULL,
    status           VARCHAR(32)   NOT NULL,
    preferencia_mp_id VARCHAR(64),
    link_pagamento   VARCHAR(512),
    criado_em        TIMESTAMPTZ   NOT NULL,
    atualizado_em    TIMESTAMPTZ   NOT NULL
);

CREATE TABLE pagamentos (
    id            UUID PRIMARY KEY,
    orcamento_id  UUID          NOT NULL REFERENCES orcamentos(id),
    os_id         UUID          NOT NULL,
    valor         NUMERIC(12,2) NOT NULL,
    mp_payment_id VARCHAR(64)   UNIQUE,
    status        VARCHAR(32)   NOT NULL,
    criado_em     TIMESTAMPTZ   NOT NULL,
    atualizado_em TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_pagamentos_os ON pagamentos (os_id);
