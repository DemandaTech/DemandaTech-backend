-- DemandaTech - Criação da estrutura do banco
-- Ordem: drop_banco.sql -> criacao_banco.sql -> populacao_banco.sql
-- Status, prioridade e perfil são ENUMs no Java, gravados como texto.

SET client_encoding = 'UTF8';

BEGIN;

CREATE SCHEMA IF NOT EXISTS demanda_tech;

CREATE SEQUENCE demanda_tech.funcionarios_matricula_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE demanda_tech.departamentos_id_departamento_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE demanda_tech.tipos_de_demanda_id_tipo_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE demanda_tech.demandas_id_demanda_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE demanda_tech.estorno_demanda_id_estorno_seq START WITH 1 INCREMENT BY 1;

-- funcionarios e departamentos apontam um para o outro: as FKs entre eles entram depois das tabelas
CREATE TABLE demanda_tech.funcionarios (
    matricula       BIGINT       PRIMARY KEY DEFAULT nextval('demanda_tech.funcionarios_matricula_seq'),
    nome            VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    senha           VARCHAR(60)  NOT NULL,
    perfil          VARCHAR(20)  NOT NULL DEFAULT 'USUARIO',
    id_departamento BIGINT       NOT NULL,
    CONSTRAINT ck_funcionarios_perfil CHECK (perfil IN ('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO', 'USUARIO'))
);

CREATE TABLE demanda_tech.departamentos (
    id_departamento BIGINT      PRIMARY KEY DEFAULT nextval('demanda_tech.departamentos_id_departamento_seq'),
    descricao       VARCHAR(50) NOT NULL,
    gerente         BIGINT
);

ALTER TABLE demanda_tech.funcionarios
    ADD CONSTRAINT fk_func_depto FOREIGN KEY (id_departamento) REFERENCES demanda_tech.departamentos(id_departamento)
    DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE demanda_tech.departamentos
    ADD CONSTRAINT fk_depto_gerente FOREIGN KEY (gerente) REFERENCES demanda_tech.funcionarios(matricula);

CREATE TABLE demanda_tech.tipos_de_demanda (
    id_tipo         BIGINT      PRIMARY KEY DEFAULT nextval('demanda_tech.tipos_de_demanda_id_tipo_seq'),
    descricao       VARCHAR(50) NOT NULL,
    id_departamento BIGINT      NOT NULL REFERENCES demanda_tech.departamentos(id_departamento)
);

CREATE TABLE demanda_tech.demandas (
    id_demanda  BIGINT       PRIMARY KEY DEFAULT nextval('demanda_tech.demandas_id_demanda_seq'),
    titulo      VARCHAR(30)  NOT NULL,
    data        DATE         NOT NULL,
    hora        TIME         NOT NULL,
    descricao   VARCHAR(250),
    justificativa VARCHAR(250),
    prioridade  VARCHAR(20)  NOT NULL DEFAULT 'BAIXA',
    id_tipo     BIGINT       NOT NULL REFERENCES demanda_tech.tipos_de_demanda(id_tipo),
    responsavel BIGINT       REFERENCES demanda_tech.funcionarios(matricula),
    criador     BIGINT       NOT NULL REFERENCES demanda_tech.funcionarios(matricula),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ABERTA',
    CONSTRAINT ck_demandas_prioridade CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA', 'URGENTE')),
    CONSTRAINT ck_demandas_status CHECK (status IN ('ABERTA', 'AGUARDANDO', 'RESOLVIDA', 'CANCELADA'))
);

CREATE TABLE demanda_tech.estorno_demanda (
    id_estorno     BIGINT       PRIMARY KEY DEFAULT nextval('demanda_tech.estorno_demanda_id_estorno_seq'),
    descricao      VARCHAR(50)  NOT NULL,
    data           DATE         NOT NULL,
    hora           TIME         NOT NULL,
    id_demanda     BIGINT       NOT NULL REFERENCES demanda_tech.demandas(id_demanda),
    registrado_por BIGINT       NOT NULL REFERENCES demanda_tech.funcionarios(matricula)
);

CREATE TABLE demanda_tech.funcionarios_envolvidos (
    matricula_funcionario BIGINT NOT NULL REFERENCES demanda_tech.funcionarios(matricula),
    id_demanda            BIGINT NOT NULL REFERENCES demanda_tech.demandas(id_demanda),
    PRIMARY KEY (matricula_funcionario, id_demanda)
);

ALTER SEQUENCE demanda_tech.funcionarios_matricula_seq OWNED BY demanda_tech.funcionarios.matricula;
ALTER SEQUENCE demanda_tech.departamentos_id_departamento_seq OWNED BY demanda_tech.departamentos.id_departamento;
ALTER SEQUENCE demanda_tech.tipos_de_demanda_id_tipo_seq OWNED BY demanda_tech.tipos_de_demanda.id_tipo;
ALTER SEQUENCE demanda_tech.demandas_id_demanda_seq OWNED BY demanda_tech.demandas.id_demanda;
ALTER SEQUENCE demanda_tech.estorno_demanda_id_estorno_seq OWNED BY demanda_tech.estorno_demanda.id_estorno;

COMMIT;
