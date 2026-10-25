-- ============================================================
-- Mercado Express - 3º Checkpoint DevOps (Pipelines CI/CD)
-- DDL das tabelas - Azure SQL Database (SQL Server / T-SQL)
-- ------------------------------------------------------------
-- Relacionamentos:
--   tb_fornecedor (1) ---- (N) tds_sec_mvc_tb_mercado   [produtos]
--   tds_users_mercado (1) ---- (N) tb_pedido
--   tb_pedido (1) ---- (N) tb_item_pedido (N) ---- (1) produtos
--
-- Execute no Query editor do Banco (Portal Azure) ANTES do 1º deploy.
-- ============================================================

IF OBJECT_ID('tb_email', 'U')               IS NOT NULL DROP TABLE tb_email;
IF OBJECT_ID('tb_item_pedido', 'U')         IS NOT NULL DROP TABLE tb_item_pedido;
IF OBJECT_ID('tb_pedido', 'U')              IS NOT NULL DROP TABLE tb_pedido;
IF OBJECT_ID('tds_sec_mvc_tb_mercado', 'U') IS NOT NULL DROP TABLE tds_sec_mvc_tb_mercado;
IF OBJECT_ID('tb_fornecedor', 'U')          IS NOT NULL DROP TABLE tb_fornecedor;
IF OBJECT_ID('tds_users_mercado', 'U')      IS NOT NULL DROP TABLE tds_users_mercado;
GO

-- Usuários (login do sistema)
CREATE TABLE tds_users_mercado (
    id         BIGINT IDENTITY(1,1) NOT NULL,
    nome       NVARCHAR(100) NOT NULL,
    email      NVARCHAR(120) NOT NULL,
    senha      NVARCHAR(255) NOT NULL,
    role       VARCHAR(20)   NOT NULL,
    criado_em  DATETIME2(6)  NULL,
    CONSTRAINT pk_users_mercado PRIMARY KEY (id),
    CONSTRAINT uk_users_mercado_email UNIQUE (email),
    CONSTRAINT ck_users_mercado_role CHECK (role IN ('ADMIN', 'CLIENTE'))
);
GO

-- Fornecedores (tabela 1 do relacionamento 1:N)
CREATE TABLE tb_fornecedor (
    id        BIGINT IDENTITY(1,1) NOT NULL,
    nome      NVARCHAR(100) NOT NULL,
    cnpj      VARCHAR(14)   NOT NULL,
    email     NVARCHAR(120) NULL,
    telefone  VARCHAR(20)   NULL,
    cidade    NVARCHAR(60)  NOT NULL,
    CONSTRAINT pk_fornecedor PRIMARY KEY (id),
    CONSTRAINT uk_fornecedor_cnpj UNIQUE (cnpj)
);
GO

-- Produtos (tabela N do relacionamento 1:N com fornecedor)
CREATE TABLE tds_sec_mvc_tb_mercado (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    nome           NVARCHAR(100)  NOT NULL,
    categoria      NVARCHAR(50)   NOT NULL,
    descricao      NVARCHAR(255)  NULL,
    preco_custo    NUMERIC(10,2)  NOT NULL,
    preco_venda    NUMERIC(10,2)  NOT NULL,
    estoque        INT            NOT NULL,
    unidade        NVARCHAR(10)   NOT NULL,
    fornecedor_id  BIGINT         NULL,
    CONSTRAINT pk_produto PRIMARY KEY (id),
    CONSTRAINT fk_produto_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES tb_fornecedor (id)
);
GO

-- Pedidos
CREATE TABLE tb_pedido (
    id                BIGINT IDENTITY(1,1) NOT NULL,
    usuario_id        BIGINT         NOT NULL,
    valor_total       NUMERIC(12,2)  NOT NULL,
    custo_total       NUMERIC(12,2)  NOT NULL,
    status            VARCHAR(30)    NOT NULL,
    conta_banco       NVARCHAR(20)   NULL,
    codigo_transacao  NVARCHAR(60)   NULL,
    mensagem_banco    NVARCHAR(255)  NULL,
    criado_em         DATETIME2(6)   NULL,
    atualizado_em     DATETIME2(6)   NULL,
    CONSTRAINT pk_pedido PRIMARY KEY (id),
    CONSTRAINT fk_pedido_usuario FOREIGN KEY (usuario_id) REFERENCES tds_users_mercado (id)
);
GO

-- Itens do pedido
CREATE TABLE tb_item_pedido (
    id              BIGINT IDENTITY(1,1) NOT NULL,
    pedido_id       BIGINT         NOT NULL,
    produto_id      BIGINT         NOT NULL,
    quantidade      INT            NOT NULL,
    preco_unitario  NUMERIC(10,2)  NOT NULL,
    custo_unitario  NUMERIC(10,2)  NOT NULL,
    CONSTRAINT pk_item_pedido PRIMARY KEY (id),
    CONSTRAINT fk_item_pedido_pedido  FOREIGN KEY (pedido_id)  REFERENCES tb_pedido (id),
    CONSTRAINT fk_item_pedido_produto FOREIGN KEY (produto_id) REFERENCES tds_sec_mvc_tb_mercado (id)
);
GO

-- Controle de e-mails enviados
CREATE TABLE tb_email (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    pedido_id   BIGINT         NULL,
    email_from  NVARCHAR(120)  NULL,
    email_to    NVARCHAR(120)  NOT NULL,
    subject     NVARCHAR(255)  NOT NULL,
    text        NVARCHAR(MAX)  NULL,
    send_date   DATETIME2(6)   NULL,
    status      VARCHAR(20)    NULL,
    erro        NVARCHAR(500)  NULL,
    CONSTRAINT pk_email PRIMARY KEY (id)
);
GO

-- Conferência: tabelas criadas
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' ORDER BY TABLE_NAME;
