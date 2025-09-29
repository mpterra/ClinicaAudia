DROP DATABASE IF EXISTS audia;
CREATE DATABASE audia;
USE audia;

-- ========================================
-- TABELA USUARIO
-- ========================================
CREATE TABLE usuario (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(20) UNIQUE,
    senha VARCHAR(64),
    ativo TINYINT NOT NULL DEFAULT 1,
    tipo ENUM('ADMIN','FONOAUDIOLOGO','SECRETARIA','FINANCEIRO') DEFAULT 'SECRETARIA',
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    usuario VARCHAR(50)
);

ALTER TABLE usuario
MODIFY COLUMN login VARCHAR(20) 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_0900_as_cs 
NOT NULL;

-- ========================================
-- TABELA ENDERECO
-- ========================================
CREATE TABLE endereco (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cep CHAR(9) NOT NULL,
    numero VARCHAR(10),
    rua VARCHAR(100),
    complemento VARCHAR(150),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    estado CHAR(2),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    usuario VARCHAR(50)
);

-- ========================================
-- TABELA PACIENTE
-- ========================================
CREATE TABLE paciente (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    sexo ENUM('M','F') NOT NULL,
    cpf CHAR(14) NOT NULL UNIQUE,
    telefone VARCHAR(30),
    email VARCHAR(60),
    data_nascimento DATE,
    id_endereco INT,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    usuario VARCHAR(50),
    CONSTRAINT fk_paciente_endereco
        FOREIGN KEY (id_endereco)
        REFERENCES endereco(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- ========================================
-- TABELA DOCUMENTO_PACIENTE
-- ========================================
CREATE TABLE documento_paciente (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    paciente_id INT NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    caminho_arquivo VARCHAR(500) NOT NULL,
    tipo_arquivo VARCHAR(50) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario VARCHAR(50),
    CONSTRAINT fk_doc_paciente
        FOREIGN KEY (paciente_id)
        REFERENCES paciente(id)
);

-- ========================================
-- TABELA PROFISSIONAL
-- ========================================
CREATE TABLE profissional (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    sexo ENUM('M','F') NOT NULL,
    cpf CHAR(14) NOT NULL UNIQUE,
    email VARCHAR(120),
    telefone VARCHAR(30),
    data_nascimento DATE,
    tipo ENUM('FONOAUDIOLOGA','SECRETARIA', 'FINANCEIRO') NOT NULL,
    id_endereco INT,
    ativo TINYINT NOT NULL DEFAULT 1,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    usuario VARCHAR(50),
    CONSTRAINT fk_profissional_endereco
        FOREIGN KEY (id_endereco)
        REFERENCES endereco(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- ========================================
-- TABELA DOCUMENTO_PROFISSIONAL
-- ========================================
CREATE TABLE documento_profissional (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    profissional_id INT NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    caminho_arquivo VARCHAR(500) NOT NULL,
    tipo_arquivo VARCHAR(50) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario VARCHAR(50),
    CONSTRAINT fk_doc_profissional
        FOREIGN KEY (profissional_id)
        REFERENCES profissional(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

ALTER TABLE usuario
ADD COLUMN profissional_id INT NULL,
ADD CONSTRAINT fk_usuario_profissional FOREIGN KEY (profissional_id) REFERENCES profissional(id);

-- ========================================
-- ESCALA FIXA DE PROFISSIONAIS
-- ========================================
CREATE TABLE escala_profissional (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    profissional_id INT NOT NULL,
    dia_semana TINYINT NOT NULL, -- 1=segunda ... 7=domingo
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    disponivel TINYINT DEFAULT 1,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    usuario VARCHAR(50),
    CONSTRAINT fk_escala_profissional
        FOREIGN KEY (profissional_id)
        REFERENCES profissional(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ========================================
-- TABELA ATENDIMENTO
-- ========================================
CREATE TABLE atendimento (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    paciente_id INT NOT NULL,
    profissional_id INT NOT NULL,
    data_hora DATETIME NOT NULL,
    duracao_min INT NOT NULL DEFAULT 30,
    tipo ENUM('AVALIACAO','RETORNO','REGULAGEM','EXAME','REUNIAO','PESSOAL') NOT NULL,
    situacao ENUM('AGENDADO','REALIZADO','FALTOU','CANCELADO') NOT NULL DEFAULT 'AGENDADO',
    notas TEXT,
    valor DECIMAL(10,2) DEFAULT 0,
    status_pagamento ENUM('PENDENTE','PARCIAL','PAGO') DEFAULT 'PENDENTE',
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    usuario VARCHAR(50),
    CONSTRAINT fk_at_paciente
        FOREIGN KEY (paciente_id)
        REFERENCES paciente(id),
    CONSTRAINT fk_at_profissional
        FOREIGN KEY (profissional_id)
        REFERENCES profissional(id)
);

-- ========================================
-- VALORES DE ATENDIMENTO POR TIPO
-- ========================================
CREATE TABLE valor_atendimento (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    profissional_id INT NOT NULL,
    tipo ENUM('AVALIACAO','RETORNO','REGULAGEM','EXAME') NOT NULL,
    valor DECIMAL(10,2) NOT NULL DEFAULT 0,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    usuario VARCHAR(50),
    CONSTRAINT fk_valores_profissional
        FOREIGN KEY (profissional_id)
        REFERENCES profissional(id),
    CONSTRAINT unq_profissional_tipo UNIQUE (profissional_id, tipo)
);


-- ========================================
-- TABELA DOCUMENTO_ATENDIMENTO
-- ========================================
CREATE TABLE documento_atendimento (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    atendimento_id INT NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    caminho_arquivo VARCHAR(500) NOT NULL,
    tipo_arquivo VARCHAR(50) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario VARCHAR(50),
    CONSTRAINT fk_doc_atendimento
        FOREIGN KEY (atendimento_id)
        REFERENCES atendimento(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ========================================
-- TIPO DE PRODUTO
-- ========================================
CREATE TABLE tipo_produto (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    usuario VARCHAR(50)
);

-- ========================================
-- PRODUTO
-- ========================================
CREATE TABLE produto (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tipo_produto_id INT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    codigo VARCHAR(100),
    descricao TEXT,
    garantia_meses INT DEFAULT 0,
    preco_venda DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT 'Preço de venda padrão para o produto, usado na tela de vendas',
    preco_custo DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT 'Preço de custo médio ou último, atualizado a partir de compras para cálculo de lucro',
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    usuario VARCHAR(50),
    CONSTRAINT fk_produto_tipo
        FOREIGN KEY (tipo_produto_id)
        REFERENCES tipo_produto(id)
);

-- ========================================
-- ESTOQUE DE PRODUTO
-- ========================================
CREATE TABLE estoque (
    produto_id INT NOT NULL PRIMARY KEY,
    quantidade INT NOT NULL DEFAULT 0,
    estoque_minimo INT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    usuario VARCHAR(50),
    CONSTRAINT fk_estoque_produto
        FOREIGN KEY (produto_id)
        REFERENCES produto(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ========================================
-- MOVIMENTO DE ESTOQUE
-- ========================================
CREATE TABLE movimento_estoque (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    produto_id INT NOT NULL,
    quantidade INT NOT NULL,
    tipo ENUM('ENTRADA','SAIDA','AJUSTE') NOT NULL,
    observacoes TEXT,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario VARCHAR(50),
    CONSTRAINT fk_mov_estoque_produto
        FOREIGN KEY (produto_id)
        REFERENCES produto(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ========================================
-- MÓDULO DE COMPRAS
-- ========================================
CREATE TABLE compra (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fornecedor VARCHAR(120),
    data_compra TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario VARCHAR(50)
);

CREATE TABLE compra_produto (
    compra_id INT NOT NULL,
    produto_id INT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    PRIMARY KEY(compra_id, produto_id),
    CONSTRAINT fk_compra_produto
        FOREIGN KEY (compra_id)
        REFERENCES compra(id),
    CONSTRAINT fk_produto_compra
        FOREIGN KEY (produto_id)
        REFERENCES produto(id)
);

-- Tabela para registrar pagamentos de compras (à vista ou parceladas)
CREATE TABLE pagamento_compra (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    compra_id INT NOT NULL,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_vencimento DATE NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    metodo_pagamento ENUM('DINHEIRO','PIX','DEBITO', 'CREDITO','BOLETO') NOT NULL,
    parcela INT DEFAULT 1,
    total_parcelas INT DEFAULT 1,
    status ENUM('PAGO','PENDENTE') DEFAULT 'PENDENTE',
    observacoes TEXT,
    usuario VARCHAR(50),
    CONSTRAINT fk_pag_compra
        FOREIGN KEY (compra_id)
        REFERENCES compra(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);



-- ========================================
-- TRIGGER PARA ATUALIZAR PRECO_CUSTO
-- ========================================
DELIMITER //
CREATE TRIGGER trg_atualiza_preco_custo AFTER INSERT ON compra_produto
FOR EACH ROW
BEGIN
    DECLARE total_quantidade INT;
    DECLARE total_valor DECIMAL(10,2);
    
    SELECT SUM(quantidade), SUM(quantidade * preco_unitario)
    INTO total_quantidade, total_valor
    FROM compra_produto
    WHERE produto_id = NEW.produto_id;
    
    UPDATE produto
    SET preco_custo = total_valor / total_quantidade
    WHERE id = NEW.produto_id;
END //
DELIMITER ;

-- ========================================
-- MÓDULO DE DESPESAS
-- ========================================

CREATE TABLE despesa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    categoria ENUM(
        'PESSOAL',          -- salários, encargos, pró-labore
        'OPERACIONAL',      -- aluguel, luz, água, internet
        'ADMINISTRATIVA',   -- material escritório, sistemas
        'VARIAVEL',         -- motoboy, pequenas compras
        'IMPOSTOS',         -- tributos, taxas
        'OUTROS'            -- algo que não se encaixe
    ) NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    forma_pagamento ENUM('DINHEIRO', 'DEBITO', 'CREDITO', 'PIX', 'TRANSFERENCIA', 'BOLETO') NOT NULL,
    data_vencimento DATE NOT NULL,
    data_pagamento DATE DEFAULT NULL,
    status ENUM('PENDENTE', 'PAGO', 'CANCELADO') DEFAULT 'PENDENTE',
    usuario VARCHAR(100) NOT NULL, -- quem lançou
    dataHora TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- auditoria
);


-- ========================================
-- TABELA ORCAMENTO
-- ========================================
CREATE TABLE orcamento (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    paciente_id INT NULL,
    profissional_id INT NULL,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valor_total DECIMAL(10,2) DEFAULT 0,
    observacoes TEXT,
    usuario VARCHAR(50),
    CONSTRAINT fk_orcamento_paciente
        FOREIGN KEY (paciente_id)
        REFERENCES paciente(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_orcamento_profissional
        FOREIGN KEY (profissional_id)
        REFERENCES profissional(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- ========================================
-- TABELA ORCAMENTO_PRODUTO
-- ========================================
CREATE TABLE orcamento_produto (
    orcamento_id INT NOT NULL,
    produto_id INT NOT NULL,
    quantidade INT NOT NULL DEFAULT 1,
    preco_unitario DECIMAL(10,2) NOT NULL,
    data_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (orcamento_id, produto_id),
    CONSTRAINT fk_orcprod_orcamento
        FOREIGN KEY (orcamento_id)
        REFERENCES orcamento(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_orcprod_produto
        FOREIGN KEY (produto_id)
        REFERENCES produto(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- ========================================
-- MÓDULO DE VENDAS
-- ========================================
CREATE TABLE venda (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    atendimento_id INT NULL,
    paciente_id INT NULL,
    orcamento_id INT NULL,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valor_total DECIMAL(10,2) DEFAULT 0,
    usuario VARCHAR(50),
    CONSTRAINT fk_venda_atendimento
        FOREIGN KEY (atendimento_id)
        REFERENCES atendimento(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_venda_orcamento
        FOREIGN KEY (orcamento_id)
        REFERENCES orcamento(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_venda_paciente
        FOREIGN KEY (paciente_id)
        REFERENCES paciente(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

CREATE TABLE venda_produto (
    venda_id INT NOT NULL,
    produto_id INT NOT NULL,
    quantidade INT NOT NULL DEFAULT 1,
    preco_unitario DECIMAL(10,2) NOT NULL,
    data_venda TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    garantia_meses INT DEFAULT 0,
    fim_garantia DATE,
    codigo_serial VARCHAR(100) UNIQUE,
    PRIMARY KEY (venda_id, produto_id),
    CONSTRAINT fk_vendaprod_venda
        FOREIGN KEY (venda_id)
        REFERENCES venda(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_vendaprod_produto
        FOREIGN KEY (produto_id)
        REFERENCES produto(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- ========================================
-- MÓDULO DE PAGAMENTOS
-- ========================================
CREATE TABLE pagamento_venda (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    venda_id INT NOT NULL,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valor DECIMAL(10,2) NOT NULL,
    metodo_pagamento ENUM('DINHEIRO','PIX','DEBITO','CREDITO','BOLETO') NOT NULL,
    parcela INT DEFAULT 1,
    total_parcelas INT DEFAULT 1,
    observacoes TEXT,
    usuario VARCHAR(50),
    CONSTRAINT fk_pag_venda
        FOREIGN KEY (venda_id)
        REFERENCES venda(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE pagamento_atendimento (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    atendimento_id INT NOT NULL,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valor DECIMAL(10,2) NOT NULL,
    metodo_pagamento ENUM('DINHEIRO','PIX','DEBITO','CREDITO') NOT NULL,
    observacoes TEXT,
    usuario VARCHAR(50),
    CONSTRAINT fk_pag_atendimento
        FOREIGN KEY (atendimento_id)
        REFERENCES atendimento(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ========================================
-- MÓDULO DE CAIXA
-- ========================================
CREATE TABLE caixa (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    data_abertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento DATETIME NULL,
    saldo_inicial_dinheiro DECIMAL(10,2) NOT NULL DEFAULT 0,
    saldo_inicial_debito DECIMAL(10,2) NOT NULL DEFAULT 0,
    saldo_inicial_credito DECIMAL(10,2) NOT NULL DEFAULT 0,
    saldo_inicial_pix DECIMAL(10,2) NOT NULL DEFAULT 0,
    saldo_final_dinheiro DECIMAL(10,2) DEFAULT 0,
    saldo_final_debito DECIMAL(10,2) DEFAULT 0,
    saldo_final_credito DECIMAL(10,2) DEFAULT 0,
    saldo_final_pix DECIMAL(10,2) DEFAULT 0,
    fechado TINYINT NOT NULL DEFAULT 0,
    observacoes TEXT,
    usuario VARCHAR(50)
);

CREATE TABLE caixa_movimento (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    caixa_id INT NULL,
    tipo ENUM('ENTRADA','SAIDA') NOT NULL,
    origem ENUM('PAGAMENTO_ATENDIMENTO','PAGAMENTO_VENDA','DESPESA','AJUSTE','OUTRO') NOT NULL,
    pagamento_atendimento_id INT NULL UNIQUE,
    pagamento_venda_id INT NULL UNIQUE,
    forma_pagamento ENUM('DINHEIRO','PIX','DEBITO','CREDITO','BOLETO') NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    descricao VARCHAR(255),
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario VARCHAR(50),
    CONSTRAINT fk_cxmov_caixa
        FOREIGN KEY (caixa_id)
        REFERENCES caixa(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_cxmov_pag_at
        FOREIGN KEY (pagamento_atendimento_id)
        REFERENCES pagamento_atendimento(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_cxmov_pag_venda
        FOREIGN KEY (pagamento_venda_id)
        REFERENCES pagamento_venda(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- ==============================================

SELECT * FROM caixa;
SELECT * FROM caixa_movimento;
SELECT * FROM atendimento;
