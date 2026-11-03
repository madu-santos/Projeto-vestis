# clothing-rental

> Nome técnico provisório. O nome comercial e a identidade visual ainda não foram definidos.

Sistema web de gestão para lojas de aluguel de roupas (vestidos de festa, trajes de gala, fantasias)
e produtos com logística semelhante. Possui uma **área pública** (vitrine e solicitações de interesse)
e uma **área administrativa** (peças, solicitações, locações e estado operacional das peças).

**Estado atual:** apenas a fundação técnica. Nenhuma funcionalidade de negócio foi implementada.

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 21 (LTS) |
| Spring Boot | 4.1.2 (Spring MVC, Thymeleaf, Data JPA, Security, Validation) |
| Banco de dados | PostgreSQL 17 (via Docker) |
| Migrações | Flyway |
| Build | Maven (via Maven Wrapper — não é preciso instalar o Maven) |
| Testes | JUnit Jupiter, Mockito, AssertJ |

## Pré-requisitos

- **JDK 21** (ex.: Eclipse Temurin 21). No IntelliJ: *File → Project Structure → SDKs → Download JDK*.
- **Docker Desktop** em execução (inclui o Docker Compose).

## Como executar

### 1. Configurar as variáveis de ambiente

Copie o modelo e defina uma senha para o banco local:

```powershell
Copy-Item .env.example .env
```

O arquivo `.env` é lido pelo Docker Compose e pela aplicação (perfil `dev`). Ele **não** é versionado.

| Variável | Finalidade | Padrão |
|---|---|---|
| `DB_HOST` | Host do PostgreSQL | `localhost` |
| `DB_PORT` | Porta do PostgreSQL | `5432` |
| `DB_NAME` | Nome do banco | `clothing_rental` |
| `DB_USERNAME` | Usuário do banco | — (obrigatória) |
| `DB_PASSWORD` | Senha do banco | — (obrigatória) |
| `SERVER_PORT` | Porta HTTP da aplicação | `8080` |
| `ADMIN_USERNAME` | Usuário do 1º administrador (dev) | — (obrigatória na 1ª subida se sem conta) |
| `ADMIN_PASSWORD` | Senha do 1º admin (mínimo 12 chars) | — (obrigatória na 1ª subida se sem conta) |

### 2. Primeiro Administrador (Perfil dev)

Na primeira inicialização em desenvolvimento, caso nenhuma conta administrativa exista no banco de dados, o inicializador `DevAdminAccountInitializer` cria automaticamente o primeiro administrador com os valores de `ADMIN_USERNAME` e `ADMIN_PASSWORD` definidos no seu arquivo `.env`:

- A senha precisa conter **no mínimo 12 caracteres**.
- O hash é gerado com **BCrypt** e persistido na tabela `staff_accounts`.
- Se a conta já existir no banco, as variáveis são ignoradas (não haverá sobrescrita).
- Se não houver conta e as variáveis estiverem vazias, a aplicação falha na inicialização informando a necessidade de configurá-las.

### 3. Subir o banco de dados

```powershell
docker compose up -d
docker compose ps        # aguarde o status "healthy"
```

### 4. Rodar a aplicação

**Pelo IntelliJ:** execute a classe `ClothingRentalApplication` (SDK do projeto: Java 21).

**Pelo terminal (PowerShell):** o Maven Wrapper usa o Java apontado por `JAVA_HOME`.
Se o `JAVA_HOME` não estiver configurado no sistema, defina-o apenas para a sessão atual:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\temurin-21.0.13"   # ajuste para o seu JDK 21
.\mvnw.cmd spring-boot:run
```

A aplicação sobe em <http://localhost:8080>.

- **Área pública** liberada: `/`, `/login`, `/catalog/**`, `/garments/**`, `/rental-requests/**`, `/css/**`, `/js/**`, `/images/**`.
- **Área administrativa**: `/admin/**` (exige autenticação com role `ADMIN`).
- Formulário de login: `/login` (redireciona para `/admin` após login bem-sucedido).
- Logout: via formulário `POST /logout` (com token CSRF).

### 5. Rodar os testes

Os testes de fundação precisam do PostgreSQL em execução (passo 3):

```powershell
.\mvnw.cmd test
```

### 6. Parar o banco

```powershell
docker compose down        # para o container (os dados são mantidos no volume)
docker compose down -v     # para e APAGA os dados do banco
```

## Perfis

| Perfil | Uso | Arquivo |
|---|---|---|
| `dev` (padrão) | Desenvolvimento local: lê o `.env`, mostra o SQL no log, sem cache de templates | `application-dev.yml` |

Configurações comuns a todos os perfis ficam em `application.yml`. Para outro perfil, use
`SPRING_PROFILES_ACTIVE=<perfil>`.

## Banco de dados e migrações

- O esquema é controlado **exclusivamente pelo Flyway**. O Hibernate está em modo `validate`:
  ele confere as entidades com o banco, mas nunca cria ou altera tabelas.
- Migrações ficam em `src/main/resources/db/migration`, com nomes no padrão
  `V<número>__<descricao>.sql` (ex.: `V1__create_catalog_tables.sql`).
- Uma migração já aplicada **nunca deve ser editada**; crie sempre uma nova.

## Estrutura do projeto

Monólito modular: cada pacote de primeiro nível é um módulo de negócio.

```
src/main/java/com/clothingrental/
├── ClothingRentalApplication.java   # ponto de entrada
├── auth/            # login do painel administrativo
├── catalog/         # peças e estado operacional
├── customer/        # clientes que efetivamente alugam
├── rentalrequest/   # solicitações de interesse vindas da vitrine
├── reservation/     # locações oficiais e verificação de disponibilidade
└── shared/          # código transversal (exceções, configurações comuns)

src/main/resources/
├── application.yml          # configuração comum
├── application-dev.yml      # perfil de desenvolvimento
├── db/migration/            # scripts do Flyway
├── templates/               # páginas Thymeleaf
└── static/                  # CSS, JS e imagens
```

Dentro de cada módulo, as camadas (`controller`, `service`, `repository`, `model`, `dto`)
são criadas somente quando necessárias. A responsabilidade de cada módulo está descrita
no respectivo `package-info.java`.
