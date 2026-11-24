-- Rodar uma única vez em bancos criados antes das colunas hora e registrado_por

SET client_encoding = 'UTF8';

BEGIN;

ALTER TABLE demanda_tech.estorno_demanda
    ADD COLUMN hora TIME NOT NULL DEFAULT '00:00:00',
    ADD COLUMN registrado_por BIGINT REFERENCES demanda_tech.funcionarios(matricula);

UPDATE demanda_tech.estorno_demanda SET registrado_por = 1 WHERE registrado_por IS NULL;

ALTER TABLE demanda_tech.estorno_demanda
    ALTER COLUMN registrado_por SET NOT NULL,
    ALTER COLUMN hora DROP DEFAULT;

COMMIT;
