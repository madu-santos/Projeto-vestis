# Vestis (clothing-rental)

> Plataforma SaaS para gerenciamento e vitrine de lojas de aluguel de roupas (vestidos de festa, trajes de gala, fantasias) e estabelecimentos com logística semelhante.

O Vestis é estruturado em três frentes:
1. **Site institucional da plataforma:** apresentação do produto e captação de novas lojas (`vestis.com.br`).
2. **Vitrines públicas das lojas:** cada estabelecimento possui seu catálogo e vitrine identificados por um slug amigável (ex.: `/lojas/aurora`, `/lojas/elegance`).
3. **Painel administrativo isolado:** área protegida (`/admin`) onde os funcionários operam exclusivamente os dados da loja à qual pertencem.

## Arquitetura Multi-Tenant

- **Modelo:** Aplicação única, banco de dados PostgreSQL compartilhado e isolamento lógico por identificador da loja (`store_id`).
- **Resolução de Loja:**
  - **Pública:** via slug na URL (`/lojas/{slug}`) através de `StoreService.getActiveStoreBySlug(slug)`.
  - **Administrativa:** identificada automaticamente na autenticação. O `StaffAccountPrincipal` armazena o `storeId`, disponibilizado via `CurrentStoreService.getCurrentStoreId()`.
- **Regra de Isolamento:** Todas as tabelas de negócio (`garment`, `customer`, `rentalrequest`, `reservation`) devem conter a coluna `store_id NOT NULL` e chave estrangeira para `stores(id)`. Toda consulta administrativa deve filtrar por `store_id`.

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

Copie o modelo e defina os parâmetros locais:

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
| `STORE_NAME` | Nome da primeira loja (dev) | `Loja de Demonstracao` |
| `STORE_SLUG` | Slug da primeira loja (dev) | `loja-demo` |
| `ADMIN_USERNAME` | Usuário do 1º administrador (dev) | — (obrigatória na 1ª subida se sem conta) |
| `ADMIN_PASSWORD` | Senha do 1º admin (mínimo 12 chars) | — (obrigatória na 1ª subida se sem conta) |

### 2. Inicialização de Loja e Administrador (Perfil dev)

Na primeira inicialização em desenvolvimento:
1. Caso nenhuma loja exista no banco de dados, o inicializador `DevAdminAccountInitializer` cria automaticamente a primeira loja com os valores de `STORE_NAME` e `STORE_SLUG`.
2. Em seguida, se nenhuma conta administrativa existir, cria o primeiro funcionário associado a essa loja com `ADMIN_USERNAME` e `ADMIN_PASSWORD`.
3. A senha precisa conter **no mínimo 12 caracteres** (hash BCrypt).
4. Se a loja ou administrador já existirem, não há sobrescrita de dados.

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

- **Área pública** liberada:
  - `GET /` (futura institucional), `/login`, `/css/**`, `/js/**`, `/images/**`.
  - `GET /lojas/**` (vitrines públicas das lojas).
  - `POST /lojas/*/solicitacoes` (solicitações de interesse de clientes, protegido com CSRF).
  - `GET /catalog/**`, `/garments/**`, `/rental-requests/**` (rotas legadas da fundação).
- **Área administrativa**: `/admin/**` (exige autenticação com role `ADMIN`).
- Formulário de login: `/login` (redireciona para `/admin` após login bem-sucedido).
- Logout: via formulário `POST /logout` (com token CSRF).

### 5. Rodar os testes

Os testes de fundação e multi-tenant precisam do PostgreSQL em execução (passo 3):

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
├── shared/          # código transversal (exceções, configurações comuns)
└── tenant/          # multi-tenancy (lojas, identificação da loja atual)


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
