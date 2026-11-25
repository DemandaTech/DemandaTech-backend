# DemandaTech - Backend

API REST do **DemandaTech**, sistema interno de chamados de TI. Funcionários abrem demandas, os técnicos de TI atendem e mudam o status, quem abriu pode registrar estornos, e gerentes e administradores acompanham tudo por painéis e relatórios.

Feito com **Java 17**, **Spring Boot 4**, **Spring Security com JWT**, **JPA/Hibernate** e **PostgreSQL**. Os clientes são o app Android (projeto `DemandaTech-Mobile`) e o front web.

---

## 1. O que instalar

| Ferramenta | Para quê |
|---|---|
| **JDK 17** ou mais novo | Rodar o backend |
| **PostgreSQL** (com o pgAdmin) | Banco de dados |
| **IntelliJ IDEA** (opcional) | Rodar e editar o backend |
| **Postman** (opcional) | Testar a API |
| **Android Studio** (opcional) | Rodar o app Android |

Não precisa instalar o Maven: o projeto já traz o Maven Wrapper (`mvnw`).

---

## 2. Criar o banco

O arquivo **`src/main/resources/static/backup_demandas.sql`** cria tudo de uma vez: apaga o schema `demanda_tech` se ele existir, cria as tabelas e insere os dados de teste. Pode ser rodado quantas vezes quiser para voltar ao estado inicial.

> ⚠️ Ele **apaga todos os dados** do schema `demanda_tech`.

**Pelo pgAdmin**
1. Na árvore da esquerda, clique com o botão direito no banco **postgres** e escolha **Query Tool**.
   Abra o Query Tool a partir do **banco**, não de uma tabela (a opção *View/Edit Data* de uma tabela não executa scripts).
2. Cole o conteúdo inteiro do `backup_demandas.sql` e aperte **F5**.
3. A aba **Messages** deve terminar com `COMMIT` e `Query returned successfully`.
4. Para ver as tabelas na árvore, clique com o botão direito em **Schemas** e escolha **Refresh**.

**Pelo terminal**
```bash
psql -U postgres -d postgres -f src/main/resources/static/backup_demandas.sql
```

**Para conferir**
```sql
SELECT 'departamentos' AS tabela, COUNT(*) FROM demanda_tech.departamentos
UNION ALL SELECT 'funcionarios', COUNT(*) FROM demanda_tech.funcionarios
UNION ALL SELECT 'tipos_de_demanda', COUNT(*) FROM demanda_tech.tipos_de_demanda
UNION ALL SELECT 'demandas', COUNT(*) FROM demanda_tech.demandas
UNION ALL SELECT 'estorno_demanda', COUNT(*) FROM demanda_tech.estorno_demanda;
```
O esperado é 5 departamentos, 30 funcionários, 10 tipos, 30 demandas e 30 estornos.

---

## 3. Configurar o backend

As configurações ficam em `src/main/resources/application.properties`:

| Propriedade | Valor padrão | Quando mudar |
|---|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/postgres` | Se o banco não for o `postgres` local |
| `spring.datasource.username` | `postgres` | Se o seu usuário do PostgreSQL for outro |
| `spring.datasource.password` | `123` | Coloque a senha do **seu** PostgreSQL |
| `jwt.secret` | lida da variável `JWT_SECRET` | Em produção, defina `JWT_SECRET` com uma chave forte. Localmente funciona sem configurar nada |
| `app.cors.origens` | `http://localhost:5173,http://localhost:3000` | Endereços do front web que podem chamar a API |

O backend usa `ddl-auto=validate`: ele **não cria tabelas**, só confere se o banco está igual às entidades. Se o banco não foi criado (passo 2), a aplicação não sobe.

---

## 4. Rodar o backend

**Pelo terminal**, na pasta do projeto:
```bash
./mvnw spring-boot:run
```
No Windows (PowerShell ou CMD), use `.\mvnw spring-boot:run`.

**Pelo IntelliJ**: abra a pasta do projeto, espere o Maven baixar as dependências e rode a classe `src/main/java/br/udesc/edu/demandatech/DemandaTechApplication.java`.

O backend sobe em **http://localhost:8080**. Quando aparecer `Started DemandaTechApplication` no console, teste:
```bash
curl http://localhost:8080/publico/departamentos
```
Deve voltar a lista dos 5 departamentos.

> O console **não** mostra usuário e senha gerados pelo Spring: o login usa os funcionários do banco (próximo passo).

---

## 5. Usuários de teste

O login é feito com a **matrícula** e a senha.

| Perfil | Matrícula | Senha | O que pode fazer |
|---|---|---|---|
| Administradora (Ana) | `20260001` | `admin123` | Tudo: cadastros, relatórios, visão geral |
| Gerente (Mariana, Financeiro) | `20260015` | `senha123` | Funcionários e visão geral do próprio departamento |
| Técnico de TI (Bruno) | `20260002` | `senha123` | Atender e mudar o status das demandas dele |
| Usuário (Otávio, Financeiro) | `20260016` | `senha123` | Abrir demandas e registrar estornos |

Outros gerentes: `20260010` (senha `admin123`), `20260021`, `20260026`. Técnicos de TI: `20260002` a `20260009`. Os demais são usuários comuns, todos com a senha `senha123`.

Regras principais:
- **Toda demanda é de TI.** Ao criar, o backend escolhe sozinho o técnico com **menos demandas pendentes** (abertas ou aguardando); em empate, o de menor matrícula.
- **Só quem criou** edita ou exclui a demanda.
- **Só o técnico responsável** muda o status: `ABERTA → AGUARDANDO → RESOLVIDA` (ou `CANCELADA`). O status nunca volta.
- **Estorno**: só quem criou, e só depois de a demanda estar `RESOLVIDA`.

---

## 6. Testar a API (Postman)

1. Faça o login: **POST** `http://localhost:8080/login`, Body → raw → JSON:
   ```json
   { "matricula": 20260001, "senha": "admin123" }
   ```
2. Copie o campo `token` da resposta.
3. Nas outras requisições, em **Authorization**, escolha **Bearer Token** e cole o token.

O token vale **30 minutos**; quando começar a voltar 401, faça login de novo. Os erros vêm sempre no formato `{"status": 403, "erro": "mensagem"}`.

Principais rotas:

| Área | Rotas |
|---|---|
| Públicas | `POST /login`, `POST /logout`, `POST /registrar`, `GET /publico/departamentos` |
| Conta | `GET /me` |
| Demandas | `GET /demanda` (filtros `status`, `prioridade`, `idTipo`, `search`, `page`, `size`), `GET /demanda/{id}`, `GET /demanda/resumo`, `GET /demanda/criar` (funcionários para envolver), `POST /demanda/criar`, `PUT /demanda/{id}`, `PATCH /demanda/{id}/status`, `DELETE /demanda/{id}` |
| Estornos | `GET /estornos` (`search`, `idDemanda`, `ano`, `mes`), `GET /estornos/{id}`, `POST /estornos`, `PUT /estornos/{id}`, `DELETE /estornos/{id}` |
| Cadastros | `/cadastros/funcionarios`, `/cadastros/departamentos`, `/cadastros/tipos-demanda` (GET com `search`, GET por id, POST, PUT, DELETE) |
| Painéis | `GET /visao-geral` (administrador e gerente), `GET /relatorios` (administrador; filtros `inicio`, `fim`, `idDepartamento`) |

Exemplo para criar uma demanda (logado como usuário):
```json
{
  "titulo": "Mouse sem clique",
  "descricao": "O botão esquerdo parou de funcionar.",
  "prioridade": "MEDIA",
  "tipo": { "idTipo": 2 },
  "usuariosEnvolvidos": [ { "matricula": 20260030 } ]
}
```

---

## 7. Rodar o app Android

1. Deixe o backend rodando (passo 4).
2. Abra o projeto **DemandaTech-Mobile** no Android Studio e rode no emulador.
3. Na primeira vez, o app pede **acesso à rede local**: permita. No Android 17 essa permissão é obrigatória para o app alcançar o backend.
4. Entre com um dos usuários de teste.

O app acessa o backend em `http://10.0.2.2:8080`, que é o seu computador visto de dentro do emulador. Para usar num **celular físico** (na mesma rede Wi-Fi do computador), gere o app informando o IP do computador:
```bash
./gradlew assembleDebug -PapiUrl=http://192.168.0.10:8080
```

---

## 8. Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| Backend não sobe, erro de *schema validation* ou tabela inexistente | O banco não foi criado. Rode o `backup_demandas.sql` (passo 2) |
| `relation "funcionarios_matricula_seq" already exists` ao criar o banco | Script antigo rodado por cima do banco existente. Use o `backup_demandas.sql`, que já apaga tudo antes de criar |
| `current transaction is aborted` no pgAdmin | Rode só `ROLLBACK;` e execute o script de novo |
| `relation "demanda_tech.demandas" does not exist` no pgAdmin | O Query Tool está aberto a partir de uma tabela ou em outro banco. Abra pelo banco **postgres** |
| Tabelas não aparecem na árvore do pgAdmin | Botão direito em **Schemas** → **Refresh** |
| `Port 8080 was already in use` | Já existe um backend rodando. Feche o outro ou mude `server.port` |
| 401 em todas as requisições | Token ausente ou expirado (30 min). Faça login de novo |
| 403 | O perfil logado não tem permissão para a ação (veja as regras do passo 5) |
| Requisições "logadas" mesmo sem token no Postman | O login grava um cookie `JWT` que o Postman reenvia. Apague em **Cookies** ou use sempre o Bearer Token |
| App mostra "Não foi possível conectar ao servidor" | Backend parado, ou a permissão de rede local foi negada (libere em Configurações → Apps → DemandaTech → Permissões) |
| Testes do Postman falham no status das demandas | O banco já tem demandas criadas em testes anteriores e outro técnico virou responsável. Recrie o banco com o `backup_demandas.sql` |

---

Desenvolvido para fins acadêmicos - UDESC.
