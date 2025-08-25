USE audia;

-- ========================================
-- TABELA USUARIO
-- ========================================
CREATE TABLE usuario (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(20) UNIQUE,
    senha VARCHAR(64),
    tipo ENUM('ADMIN','FONOAUDIOLOGO','SECRETARIA','FINANCEIRO') DEFAULT 'SECRETARIA',
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ========================================
-- TABELA ENDERECO
-- ========================================
CREATE TABLE endereco (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cep CHAR(8) NOT NULL,
    numero VARCHAR(10),
    rua VARCHAR(100),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    estado VARCHAR(100),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ========================================
-- TABELA PACIENTE
-- ========================================
CREATE TABLE paciente (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf CHAR(11) NOT NULL UNIQUE,
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
    cpf CHAR(11) NOT NULL UNIQUE,
    email VARCHAR(120),
    telefone VARCHAR(30),
    tipo ENUM('FONOAUDIOLOGA','SECRETARIA') NOT NULL,
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
-- PAGAMENTO DE ATENDIMENTO
-- ========================================
CREATE TABLE pagamento_atendimento (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    atendimento_id INT NOT NULL,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valor DECIMAL(10,2) NOT NULL,
    metodo_pagamento ENUM('DINHEIRO','PIX','CARTAO') NOT NULL,
    observacoes TEXT,
    usuario VARCHAR(50),

    CONSTRAINT fk_pag_atendimento
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
    codigo_serial VARCHAR(100),
    descricao TEXT,
    preco DECIMAL(10,2) NOT NULL,
    estoque INT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    usuario VARCHAR(50),

    CONSTRAINT fk_produto_tipo FOREIGN KEY (tipo_produto_id)
        REFERENCES tipo_produto(id)
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

    CONSTRAINT fk_mov_estoque_produto FOREIGN KEY (produto_id)
        REFERENCES produto(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ========================================
-- VENDA
-- ========================================
CREATE TABLE venda (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    atendimento_id INT NOT NULL,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valor_total DECIMAL(10,2) DEFAULT 0,
    usuario VARCHAR(50),

    CONSTRAINT fk_venda_atendimento
        FOREIGN KEY (atendimento_id)
        REFERENCES atendimento(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ========================================
-- VENDA_PRODUTO
-- ========================================
CREATE TABLE venda_produto (
    venda_id INT NOT NULL,
    produto_id INT NOT NULL,
    quantidade INT NOT NULL DEFAULT 1,
    preco_unitario DECIMAL(10,2) NOT NULL,
    data_venda DATE DEFAULT CURRENT_DATE,
    garantia_meses INT DEFAULT 0,
    fim_garantia DATE,
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
-- PAGAMENTO DE VENDA
-- ========================================
CREATE TABLE pagamento_venda (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    venda_id INT NOT NULL,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valor DECIMAL(10,2) NOT NULL,
    metodo_pagamento ENUM('DINHEIRO','PIX','CARTAO','BOLETO') NOT NULL,
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

-- ========================================
-- EVOLUCAO DO PACIENTE
-- ========================================
CREATE TABLE evolucao_atendimento (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    atendimento_id INT NOT NULL,
    notas TEXT,
    arquivo VARCHAR(500),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario VARCHAR(50),

    CONSTRAINT fk_evolucao_atendimento FOREIGN KEY (atendimento_id)
        REFERENCES atendimento(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ========================================
-- AGENDA DE PROFISSIONAIS
-- ========================================
CREATE TABLE agenda_profissional (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    profissional_id INT NOT NULL,
    data_hora_inicio DATETIME NOT NULL,
    data_hora_fim DATETIME NOT NULL,
    disponivel TINYINT DEFAULT 1,
    usuario VARCHAR(50),

    CONSTRAINT fk_agenda_profissional FOREIGN KEY (profissional_id)
        REFERENCES profissional(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


