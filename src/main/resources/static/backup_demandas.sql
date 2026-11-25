-- Arquivo de Backup - DemandaTech (PostgreSQL)
-- Recria o schema demanda_tech do zero (estrutura + dados de teste, somente demandas de TI) em uma única transação.
-- ATENÇÃO: apaga o schema demanda_tech existente e todos os seus dados.
-- Restaurar: psql -U postgres -d postgres -f backup_demandas.sql  (ou rodar no Query Tool do pgAdmin, no banco postgres)
--
-- Logins de teste (senha gravada com BCrypt):
--   20260001 ADMINISTRADOR (admin123) | gerentes 20260010 (admin123), 20260015, 20260021, 20260026
--   técnicos de TI (FUNCIONARIO) 20260002-20260009 | demais USUARIO
--   Senha de todos os outros: senha123

ROLLBACK;

SET client_encoding = 'UTF8';

BEGIN;

DROP SCHEMA IF EXISTS demanda_tech CASCADE;
CREATE SCHEMA demanda_tech;

CREATE SEQUENCE demanda_tech.funcionarios_matricula_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE demanda_tech.departamentos_id_departamento_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE demanda_tech.tipos_de_demanda_id_tipo_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE demanda_tech.demandas_id_demanda_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE demanda_tech.estorno_demanda_id_estorno_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE demanda_tech.departamentos (
    id_departamento BIGINT      PRIMARY KEY DEFAULT nextval('demanda_tech.departamentos_id_departamento_seq'),
    descricao       VARCHAR(50) NOT NULL,
    gerente         BIGINT
);

CREATE TABLE demanda_tech.funcionarios (
    matricula       BIGINT       PRIMARY KEY DEFAULT nextval('demanda_tech.funcionarios_matricula_seq'),
    nome            VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    senha           VARCHAR(60)  NOT NULL,
    perfil          VARCHAR(20)  NOT NULL DEFAULT 'USUARIO',
    id_departamento BIGINT       NOT NULL,
    CONSTRAINT ck_funcionarios_perfil CHECK (perfil IN ('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO', 'USUARIO')),
    CONSTRAINT fk_funcionarios_departamento FOREIGN KEY (id_departamento)
        REFERENCES demanda_tech.departamentos(id_departamento) DEFERRABLE INITIALLY DEFERRED
);

CREATE UNIQUE INDEX uq_funcionarios_email ON demanda_tech.funcionarios (LOWER(email));

ALTER TABLE demanda_tech.departamentos
    ADD CONSTRAINT fk_departamentos_gerente FOREIGN KEY (gerente)
        REFERENCES demanda_tech.funcionarios(matricula);

CREATE TABLE demanda_tech.tipos_de_demanda (
    id_tipo         BIGINT      PRIMARY KEY DEFAULT nextval('demanda_tech.tipos_de_demanda_id_tipo_seq'),
    descricao       VARCHAR(50) NOT NULL,
    id_departamento BIGINT      NOT NULL,
    CONSTRAINT fk_tipos_departamento FOREIGN KEY (id_departamento)
        REFERENCES demanda_tech.departamentos(id_departamento)
);

CREATE TABLE demanda_tech.demandas (
    id_demanda    BIGINT       PRIMARY KEY DEFAULT nextval('demanda_tech.demandas_id_demanda_seq'),
    titulo        VARCHAR(30)  NOT NULL,
    data          DATE         NOT NULL,
    hora          TIME         NOT NULL,
    descricao     VARCHAR(250),
    justificativa VARCHAR(250),
    prioridade    VARCHAR(20)  NOT NULL DEFAULT 'BAIXA',
    id_tipo       BIGINT       NOT NULL,
    responsavel   BIGINT,
    criador       BIGINT       NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ABERTA',
    CONSTRAINT ck_demandas_prioridade CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA', 'URGENTE')),
    CONSTRAINT ck_demandas_status CHECK (status IN ('ABERTA', 'AGUARDANDO', 'RESOLVIDA', 'CANCELADA')),
    CONSTRAINT fk_demandas_tipo FOREIGN KEY (id_tipo)
        REFERENCES demanda_tech.tipos_de_demanda(id_tipo),
    CONSTRAINT fk_demandas_responsavel FOREIGN KEY (responsavel)
        REFERENCES demanda_tech.funcionarios(matricula),
    CONSTRAINT fk_demandas_criador FOREIGN KEY (criador)
        REFERENCES demanda_tech.funcionarios(matricula)
);

CREATE TABLE demanda_tech.estorno_demanda (
    id_estorno     BIGINT      PRIMARY KEY DEFAULT nextval('demanda_tech.estorno_demanda_id_estorno_seq'),
    descricao      VARCHAR(50) NOT NULL,
    data           DATE        NOT NULL,
    hora           TIME        NOT NULL,
    id_demanda     BIGINT      NOT NULL,
    registrado_por BIGINT      NOT NULL,
    CONSTRAINT fk_estorno_demanda FOREIGN KEY (id_demanda)
        REFERENCES demanda_tech.demandas(id_demanda) ON DELETE CASCADE,
    CONSTRAINT fk_estorno_registrado_por FOREIGN KEY (registrado_por)
        REFERENCES demanda_tech.funcionarios(matricula)
);

CREATE TABLE demanda_tech.funcionarios_envolvidos (
    matricula_funcionario BIGINT NOT NULL,
    id_demanda            BIGINT NOT NULL,
    PRIMARY KEY (matricula_funcionario, id_demanda),
    CONSTRAINT fk_envolvidos_funcionario FOREIGN KEY (matricula_funcionario)
        REFERENCES demanda_tech.funcionarios(matricula),
    CONSTRAINT fk_envolvidos_demanda FOREIGN KEY (id_demanda)
        REFERENCES demanda_tech.demandas(id_demanda) ON DELETE CASCADE
);

ALTER SEQUENCE demanda_tech.funcionarios_matricula_seq OWNED BY demanda_tech.funcionarios.matricula;
ALTER SEQUENCE demanda_tech.departamentos_id_departamento_seq OWNED BY demanda_tech.departamentos.id_departamento;
ALTER SEQUENCE demanda_tech.tipos_de_demanda_id_tipo_seq OWNED BY demanda_tech.tipos_de_demanda.id_tipo;
ALTER SEQUENCE demanda_tech.demandas_id_demanda_seq OWNED BY demanda_tech.demandas.id_demanda;
ALTER SEQUENCE demanda_tech.estorno_demanda_id_estorno_seq OWNED BY demanda_tech.estorno_demanda.id_estorno;

INSERT INTO demanda_tech.departamentos (id_departamento, descricao, gerente) VALUES
(1, 'Tecnologia da Informação', NULL),
(2, 'Recursos Humanos', NULL),
(3, 'Financeiro', NULL),
(4, 'Comercial', NULL),
(5, 'Operações', NULL);

INSERT INTO demanda_tech.funcionarios (matricula, nome, email, senha, perfil, id_departamento) VALUES
(20260001, 'Ana Souza', 'ana.souza@demandatech.com.br', '$2a$10$uidWUcTax4d53Dn3JqlTvO/Nm4/pKqXpM2aL/0boEg7cvohOq7rDe', 'ADMINISTRADOR', 1),
(20260002, 'Bruno Costa', 'bruno.costa@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'FUNCIONARIO', 1),
(20260003, 'Carla Lima', 'carla.lima@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'FUNCIONARIO', 1),
(20260004, 'Diego Alves', 'diego.alves@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'FUNCIONARIO', 1),
(20260005, 'Elisa Martins', 'elisa.martins@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'FUNCIONARIO', 1),
(20260006, 'Felipe Rocha', 'felipe.rocha@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'FUNCIONARIO', 1),
(20260007, 'Gabriela Dias', 'gabriela.dias@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'FUNCIONARIO', 1),
(20260008, 'Henrique Melo', 'henrique.melo@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'FUNCIONARIO', 1),
(20260009, 'Isabela Nunes', 'isabela.nunes@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'FUNCIONARIO', 1),
(20260010, 'Juliana Ramos', 'juliana.ramos@demandatech.com.br', '$2a$10$uidWUcTax4d53Dn3JqlTvO/Nm4/pKqXpM2aL/0boEg7cvohOq7rDe', 'GERENTE', 2),
(20260011, 'João Pedro Silva', 'joao.silva@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 2),
(20260012, 'Larissa Moreira', 'larissa.moreira@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 2),
(20260013, 'Marcos Vieira', 'marcos.vieira@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 2),
(20260014, 'Natália Freitas', 'natalia.freitas@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 2),
(20260015, 'Mariana Almeida', 'mariana.almeida@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'GERENTE', 3),
(20260016, 'Otávio Pereira', 'otavio.pereira@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 3),
(20260017, 'Patrícia Gomes', 'patricia.gomes@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 3),
(20260018, 'Ricardo Barbosa', 'ricardo.barbosa@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 3),
(20260019, 'Sabrina Teixeira', 'sabrina.teixeira@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 3),
(20260020, 'Thiago Cardoso', 'thiago.cardoso@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 3),
(20260021, 'Pedro Santos', 'pedro.santos@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'GERENTE', 4),
(20260022, 'Beatriz Mota', 'beatriz.mota@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 4),
(20260023, 'Caio Ferreira', 'caio.ferreira@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 4),
(20260024, 'Daniela Rocha', 'daniela.rocha@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 4),
(20260025, 'Eduardo Pinto', 'eduardo.pinto@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 4),
(20260026, 'Rafael Lima', 'rafael.lima@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'GERENTE', 5),
(20260027, 'Fernanda Castro', 'fernanda.castro@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 5),
(20260028, 'Gustavo Ribeiro', 'gustavo.ribeiro@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 5),
(20260029, 'Helena Duarte', 'helena.duarte@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 5),
(20260030, 'Igor Monteiro', 'igor.monteiro@demandatech.com.br', '$2a$10$OekGoSdMB8lxPkDT37Kqs.ljmJIcmUJLteiswlWEYXTLwIhzxC0U.', 'USUARIO', 5);

UPDATE demanda_tech.departamentos SET gerente = 20260001 WHERE id_departamento = 1;
UPDATE demanda_tech.departamentos SET gerente = 20260010 WHERE id_departamento = 2;
UPDATE demanda_tech.departamentos SET gerente = 20260015 WHERE id_departamento = 3;
UPDATE demanda_tech.departamentos SET gerente = 20260021 WHERE id_departamento = 4;
UPDATE demanda_tech.departamentos SET gerente = 20260026 WHERE id_departamento = 5;

INSERT INTO demanda_tech.tipos_de_demanda (id_tipo, descricao, id_departamento) VALUES
(1, 'Rede / VPN', 1),
(2, 'Hardware', 1),
(3, 'Software', 1),
(4, 'Acesso a sistemas', 1),
(5, 'E-mail', 1),
(6, 'Impressoras e scanners', 1),
(7, 'Telefonia', 1),
(8, 'Backup e arquivos', 1),
(9, 'Segurança da informação', 1),
(10, 'Equipamentos e periféricos', 1);

INSERT INTO demanda_tech.demandas (id_demanda, titulo, data, hora, descricao, justificativa, prioridade, id_tipo, responsavel, criador, status) VALUES
(1, 'Notebook não liga', '2026-10-06', '08:40:00', 'O notebook não liga mesmo conectado à tomada desde a manhã.', 'Fonte do notebook substituída e equipamento testado.', 'ALTA', 2, 20260002, 20260016, 'RESOLVIDA'),
(2, 'Acesso ao ERP financeiro', '2026-10-07', '09:15:00', 'Preciso de acesso ao módulo de contas a pagar do ERP.', 'Perfil de contas a pagar liberado no ERP.', 'MEDIA', 4, 20260002, 20260017, 'RESOLVIDA'),
(3, 'Ramal sem sinal', '2026-10-08', '14:05:00', 'O ramal 2140 do financeiro está mudo desde o início da tarde.', 'Porta do switch reconfigurada e ramal reativado.', 'MEDIA', 7, 20260003, 20260022, 'RESOLVIDA'),
(4, 'Instalação do Office', '2026-10-09', '10:30:00', 'Instalar o pacote Office na estação nova do comercial.', 'Office instalado e ativado na estação.', 'BAIXA', 3, 20260003, 20260023, 'RESOLVIDA'),
(5, 'Projetor da sala 2', '2026-10-12', '11:00:00', 'O projetor da sala de reuniões 2 não exibe imagem do notebook.', 'Cabo HDMI trocado e projetor configurado.', 'MEDIA', 10, 20260004, 20260012, 'RESOLVIDA'),
(6, 'Senha do e-mail expirada', '2026-10-13', '16:20:00', 'Minha senha do e-mail expirou e não consigo redefinir pelo portal.', 'Senha redefinida e acesso testado com o usuário.', 'ALTA', 5, 20260005, 20260028, 'RESOLVIDA'),
(7, 'VPN não conecta', '2026-10-14', '08:05:00', 'A VPN retorna erro de autenticação no acesso remoto.', 'Certificado da VPN renovado na máquina do usuário.', 'URGENTE', 1, 20260004, 20260015, 'RESOLVIDA'),
(8, 'Backup da pasta fiscal', '2026-10-15', '13:45:00', 'Arquivos da pasta fiscal sumiram e precisam ser restaurados.', 'Arquivos restaurados a partir do backup da noite anterior.', 'URGENTE', 8, 20260006, 20260026, 'RESOLVIDA'),
(9, 'Notebook para estagiário', '2026-10-16', '15:10:00', 'Preparar um notebook para o estagiário que começa na segunda.', NULL, 'MEDIA', 10, 20260007, 20260021, 'CANCELADA'),
(10, 'Impressora sem conexão', '2026-10-19', '09:50:00', 'A impressora do 2º andar não aparece na rede.', 'Impressora recebeu IP fixo e foi adicionada de novo.', 'MEDIA', 6, 20260005, 20260014, 'RESOLVIDA'),
(11, 'E-mail suspeito recebido', '2026-10-20', '10:00:00', 'Recebi um e-mail pedindo meus dados bancários com link estranho.', 'Remetente bloqueado e mensagem removida das caixas.', 'ALTA', 9, 20260008, 20260020, 'RESOLVIDA'),
(12, 'Mouse sem fio extra', '2026-10-21', '17:30:00', 'Solicito um mouse sem fio extra para uso na sala de reuniões.', NULL, 'BAIXA', 10, 20260009, 20260019, 'CANCELADA'),
(13, 'Acesso ao CRM', '2026-10-22', '08:55:00', 'Novo vendedor precisa de acesso ao CRM.', 'Usuário criado no CRM com perfil de vendedor.', 'MEDIA', 4, 20260006, 20260024, 'RESOLVIDA'),
(14, 'Monitor piscando', '2026-10-26', '14:40:00', 'O monitor secundário pisca constantemente.', NULL, 'BAIXA', 2, 20260007, 20260029, 'AGUARDANDO'),
(15, 'Erro no sistema de folha', '2026-10-27', '09:20:00', 'O sistema de folha fecha sozinho ao gerar o holerite.', NULL, 'ALTA', 3, 20260008, 20260030, 'AGUARDANDO'),
(16, 'Atualizar antivírus', '2026-10-28', '11:35:00', 'O antivírus indica que a licença expirou.', 'Licença do antivírus renovada e base atualizada.', 'MEDIA', 9, 20260008, 20260018, 'RESOLVIDA'),
(17, 'Wi-Fi instável', '2026-10-29', '15:00:00', 'O Wi-Fi cai várias vezes ao dia na ala comercial.', NULL, 'ALTA', 1, 20260009, 20260025, 'ABERTA'),
(18, 'Licença do Power BI', '2026-10-30', '10:10:00', 'Preciso de uma licença do Power BI para os relatórios do RH.', NULL, 'BAIXA', 3, 20260003, 20260011, 'AGUARDANDO'),
(19, 'Permissão de pasta', '2026-11-03', '10:12:00', 'Preciso de acesso de escrita na pasta do financeiro.', NULL, 'MEDIA', 8, 20260006, 20260015, 'ABERTA'),
(20, 'Troca de teclado', '2026-11-03', '13:25:00', 'Teclado com várias teclas travando.', 'Teclado substituído por um novo.', 'BAIXA', 2, 20260002, 20260027, 'RESOLVIDA'),
(21, 'Acesso ao sistema de RH', '2026-11-04', '09:30:00', 'Acesso ao portal de benefícios para a nova analista.', NULL, 'MEDIA', 4, 20260003, 20260010, 'AGUARDANDO'),
(22, 'Instalar certificado', '2026-11-04', '15:25:00', 'Instalar o certificado digital A1 da empresa.', 'Certificado A1 instalado e testado no navegador.', 'ALTA', 3, 20260004, 20260016, 'RESOLVIDA'),
(23, 'Falha no e-mail', '2026-11-05', '08:10:00', 'Os e-mails externos não estão chegando desde ontem.', NULL, 'URGENTE', 5, 20260005, 20260021, 'ABERTA'),
(24, 'VPN acesso bloqueado', '2026-11-05', '09:42:00', 'A equipe do financeiro não acessa a VPN desde as 9h.', NULL, 'ALTA', 1, 20260006, 20260015, 'AGUARDANDO'),
(25, 'Arquivo excluído da rede', '2026-11-06', '14:15:00', 'Uma planilha de inventário foi excluída da pasta compartilhada.', NULL, 'MEDIA', 8, 20260004, 20260009, 'AGUARDANDO'),
(26, 'Telefone IP sem áudio', '2026-11-09', '08:30:00', 'O telefone IP da recepção do RH não tem áudio nas ligações.', NULL, 'ALTA', 7, 20260009, 20260013, 'ABERTA'),
(27, 'Scanner não digitaliza', '2026-11-10', '11:50:00', 'O scanner do comercial trava ao digitalizar contratos.', NULL, 'MEDIA', 6, 20260005, 20260023, 'ABERTA'),
(28, 'Kit para novo analista', '2026-11-11', '16:05:00', 'Preparar notebook, headset e acessos para o analista novo do RH.', NULL, 'MEDIA', 10, 20260007, 20260001, 'ABERTA'),
(29, 'Computador lento', '2026-11-12', '09:00:00', 'O computador demora mais de 10 minutos para iniciar.', NULL, 'MEDIA', 2, 20260007, 20260022, 'ABERTA'),
(30, 'Acesso ao GitLab', '2026-11-13', '10:45:00', 'Novo desenvolvedor precisa de acesso ao repositório.', NULL, 'BAIXA', 4, 20260008, 20260002, 'ABERTA');

INSERT INTO demanda_tech.funcionarios_envolvidos (matricula_funcionario, id_demanda) VALUES
(20260017, 1),
(20260020, 3),
(20260024, 3),
(20260029, 6),
(20260019, 7),
(20260017, 7),
(20260018, 7),
(20260024, 9),
(20260012, 10),
(20260013, 10),
(20260023, 13),
(20260025, 13),
(20260028, 15),
(20260029, 15),
(20260024, 17),
(20260023, 17),
(20260022, 17),
(20260017, 19),
(20260019, 19),
(20260013, 21),
(20260012, 21),
(20260025, 23),
(20260024, 23),
(20260018, 24),
(20260021, 24),
(20260010, 24),
(20260012, 26),
(20260021, 27),
(20260010, 28),
(20260009, 28);

INSERT INTO demanda_tech.estorno_demanda (id_estorno, descricao, data, hora, id_demanda, registrado_por) VALUES
(1, 'Equipamento voltou a desligar após o reparo.', '2026-10-08', '09:10:00', 1, 20260016),
(2, 'Fonte substituída era incompatível com o modelo.', '2026-10-10', '14:00:00', 1, 20260016),
(3, 'Acesso liberado no módulo errado do ERP.', '2026-10-08', '10:20:00', 2, 20260017),
(4, 'Ramal voltou a ficar mudo no dia seguinte.', '2026-10-10', '11:30:00', 3, 20260022),
(5, 'Licença ativada expirou no mesmo dia.', '2026-10-12', '09:00:00', 4, 20260023),
(6, 'Projetor voltou a desligar durante a reunião.', '2026-10-15', '15:40:00', 5, 20260012),
(7, 'Nova senha não funciona no celular.', '2026-10-16', '10:05:00', 6, 20260028),
(8, 'Senha expirou de novo no mesmo dia.', '2026-10-20', '16:30:00', 6, 20260028),
(9, 'Atendimento concluído sem restabelecer a conexão.', '2026-10-15', '10:20:00', 7, 20260015),
(10, 'Erro de autenticação persiste para dois usuários.', '2026-10-17', '08:50:00', 7, 20260015),
(11, 'Backup não incluiu a pasta de notas fiscais.', '2026-10-16', '11:15:00', 8, 20260026),
(12, 'Impressora voltou a sumir da rede após reinício.', '2026-10-21', '13:10:00', 10, 20260014),
(13, 'Remetente suspeito não foi bloqueado.', '2026-10-23', '09:35:00', 11, 20260020),
(14, 'Perfil criado sem permissão de cadastrar clientes.', '2026-10-23', '14:25:00', 13, 20260024),
(15, 'Usuário bloqueado após troca de senha.', '2026-10-27', '10:00:00', 13, 20260024),
(16, 'Acesso sem o módulo de contas a receber.', '2026-10-09', '11:40:00', 2, 20260017),
(17, 'Ramal transferido para a mesa errada.', '2026-10-13', '10:15:00', 3, 20260022),
(18, 'Licença renovada para a máquina errada.', '2026-10-30', '09:25:00', 16, 20260018),
(19, 'Office instalado sem a licença corporativa.', '2026-10-13', '15:20:00', 4, 20260023),
(20, 'Cabo HDMI novo também não exibe imagem.', '2026-10-14', '09:30:00', 5, 20260012),
(21, 'Restauração trouxe arquivos desatualizados.', '2026-10-17', '10:45:00', 8, 20260026),
(22, 'Teclado entregue com o padrão ABNT errado.', '2026-11-04', '16:45:00', 20, 20260027),
(23, 'Impressora falha após a reconfiguração.', '2026-10-23', '08:40:00', 10, 20260014),
(24, 'Certificado instalado apenas no navegador Edge.', '2026-11-05', '15:25:00', 22, 20260016),
(25, 'Senha do certificado não foi informada ao usuário.', '2026-11-06', '09:30:00', 22, 20260016),
(26, 'Outros e-mails do mesmo golpe chegaram.', '2026-10-26', '14:10:00', 11, 20260020),
(27, 'Teclado novo veio com a tecla Ç sem funcionar.', '2026-11-05', '10:30:00', 20, 20260027),
(28, 'Certificado não aparece no sistema da prefeitura.', '2026-11-06', '16:10:00', 22, 20260016),
(29, 'Acesso ao CRM expirou após uma semana.', '2026-10-30', '09:50:00', 13, 20260024),
(30, 'Antivírus bloqueou o sistema de faturamento.', '2026-11-02', '11:00:00', 16, 20260018);

SELECT setval('demanda_tech.funcionarios_matricula_seq', (SELECT MAX(matricula) FROM demanda_tech.funcionarios));
SELECT setval('demanda_tech.departamentos_id_departamento_seq', (SELECT MAX(id_departamento) FROM demanda_tech.departamentos));
SELECT setval('demanda_tech.tipos_de_demanda_id_tipo_seq', (SELECT MAX(id_tipo) FROM demanda_tech.tipos_de_demanda));
SELECT setval('demanda_tech.demandas_id_demanda_seq', (SELECT MAX(id_demanda) FROM demanda_tech.demandas));
SELECT setval('demanda_tech.estorno_demanda_id_estorno_seq', (SELECT MAX(id_estorno) FROM demanda_tech.estorno_demanda));

COMMIT;
