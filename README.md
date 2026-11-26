# DemandaTech - Backend

API REST do **DemandaTech**, sistema interno de chamados de TI. 
Funcionários abrem demandas, os técnicos de TI atendem e mudam o status, quem abriu pode registrar estornos,
e gerentes e administradores acompanham tudo por painéis e relatórios.

Feito com **Java 17**, **Spring Boot 4**, **Spring Security com JWT**, **JPA/Hibernate** e **PostgreSQL**. Os clientes são o app Android (projeto `DemandaTech-Mobile`) e o front web.

---

## 1. O que instalar

| Ferramenta | Para quê |
|---|---|
| **JDK 17** ou mais novo | Rodar o backend |
| **PostgreSQL** (com o pgAdmin) | Banco de dados |
| **Android Studio** (opcional) | Rodar o app Android |

---

## 2. Criar o banco

O arquivo **`src/main/resources/static/backup_demandas.sql`** cria tudo de uma vez: apaga o schema `demanda_tech` se ele existir, cria as tabelas e insere os dados de teste.

---

## 3. Configurar o backend

As configurações ficam em `src/main/resources/application.properties`:

````
spring.application.name=DemandaTech

spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=123
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.properties.hibernate.default_schema=demanda_tech
spring.jpa.hibernate.ddl-auto=validate
# spring.jpa.show-sql=true

logging.level.org.hibernate.orm.jdbc.error=OFF

//deixei a chave, pois é somente fim educativo
jwt.secret=${JWT_SECRET:demandatech-chave-somente-para-desenvolvimento}
app.cors.origens=http://localhost:5173,http://localhost:3000
````
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
---

## 5. Rodar o app Android

1. Deixe o backend rodando (passo 4).
2. Abra o projeto **DemandaTech-Mobile** no Android Studio e rode no emulador.
3. Na primeira vez, o app pede **acesso à rede local**: permita.
4. Entre com um dos usuários de teste.

---

## 6. Usuários de teste

O login é feito com a **matrícula** e a senha.

| Perfil | Matrícula | Senha | O que pode fazer                                             |
|---|---|---|--------------------------------------------------------------|
| Administradora | `20260001` | `admin123` | Tudo: cadastros, relatórios, visão geral                     |
| Gerente | `20260015` | `senha123` | Cadastrar funcionários e visão geral do próprio departamento |
| Técnico de TI | `20260002` | `senha123` | Atender e mudar o status das demandas dele                   |
| Usuário | `20260016` | `senha123` | Abrir demandas e registrar estornos                          |

Regras principais:
- **Toda demanda é de TI.** Ao criar, o backend escolhe sozinho o técnico com **menos demandas pendentes** (abertas ou aguardando); em empate, o de menor matrícula.
- **Só quem criou** edita ou exclui a demanda.
- **Só o técnico responsável** muda o status: `ABERTA → AGUARDANDO → RESOLVIDA` (ou `CANCELADA`). O status nunca volta.
- **Estorno**: só quem criou, e só depois de a demanda estar `RESOLVIDA`.

---

Desenvolvido por Ana Luiza Elert e Lucas Nass Budal Arins
