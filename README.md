<div align="center">
  <img src="docs/assets/vestis-logo.svg" alt="Vestis Logo" width="130" />
  <h1>Vestis — Gestão inteligente para lojas de locação</h1>
  <p><strong>Plataforma SaaS multi-tenant para gestão operacional, controle de acervos, locações e vitrines digitais para lojas de aluguel de vestuário e trajes.</strong></p>
  <p>
    <img src="https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=flat&logo=openjdk&logoColor=white" alt="Java 21" />
    <img src="https://img.shields.io/badge/Spring%20Boot-4.1.2-6DB33F?style=flat&logo=springboot&logoColor=white" alt="Spring Boot 4.1.2" />
    <img src="https://img.shields.io/badge/Spring%20Security-7.0-6DB33F?style=flat&logo=springsecurity&logoColor=white" alt="Spring Security" />
    <img src="https://img.shields.io/badge/PostgreSQL-17-4169E1?style=flat&logo=postgresql&logoColor=white" alt="PostgreSQL 17" />
    <img src="https://img.shields.io/badge/Thymeleaf-HTML5%2FCSS3-005F0F?style=flat&logo=thymeleaf&logoColor=white" alt="Thymeleaf" />
    <img src="https://img.shields.io/badge/Status-Em%20desenvolvimento-B46B64" alt="Status do Projeto" />
  </p>
</div>

---

## 📌 Sumário

1. [Sobre o Projeto](#-sobre-o-projeto)
2. [Como Funciona](#-como-funciona)
3. [Funcionalidades Principais](#-funcionalidades-principais)
4. [Demonstração Visual (Telas Implementadas)](#-demonstração-visual-telas-implementadas)
5. [Arquitetura do Sistema](#-arquitetura-do-sistema)
6. [Tecnologias Utilizadas](#-tecnologias-utilizadas)
7. [Regras de Negócio e Fluxo de Locação](#-regras-de-negócio-e-fluxo-de-locação)
8. [Como Executar Localmente](#-como-executar-localmente)
9. [Estado Atual do Projeto](#-estado-atual-do-projeto)
10. [Protótipos Visuais (Fases Planejadas)](#-protótipos-visuais-fases-planejadas)

---

## 📖 Sobre o Projeto

O **Vestis** é uma plataforma SaaS multi-tenant desenvolvida para resolver as dores operacionais de estabelecimentos que trabalham com **aluguel de vestuário** — como trajes a rigor, ternos, vestidos de festa, figurinos e fantasias —, com base arquitetural projetada para permitir expansões futuras a outros segmentos de locação física com dinâmica semelhante.

Diferente de um comércio eletrônico tradicional de venda definitiva, o segmento de locação lida com itens que retornam ao estoque e exigem:
- Acompanhamento de **disponibilidade individual por data ou período**;
- Controle de etapas internas de **conferência, higienização e manutenção**;
- Fluxo de **agendamento de provas presenciais** antes da contratação;
- Substituição de anotações manuais e planilhas dispersas por uma **operação centralizada e segura**.

---

## 🏢 Como Funciona

A plataforma integra três ambientes com papéis e acessos bem definidos:

```
┌─────────────────────────────────────────────────────────────────┐
│                 Site Institucional (Rota: /)                    │
│    Apresentação do SaaS para captação de novos estabelecimentos │
└────────────────┬───────────────────────────────┬────────────────┘
                 │                               │
                 ▼                               ▼
┌─────────────────────────────────┐   ┌───────────────────────────┐
│   Painel Administrativo (/admin)│   │ Vitrine Pública da Loja   │
│   Acesso restrito de cada loja  │   │ Rota: /lojas/{slug}       │
│   Gestão de acervo e locações   │   │ Catálogo aberto ao cliente│
└─────────────────────────────────┘   └───────────────────────────┘
```

1. **Site Institucional (`/`):** Canal público da plataforma que apresenta os recursos do Vestis, lojas demonstrativas e agendamento de contato para novos lojistas.
2. **Painel Administrativo (`/admin`):** Ambiente seguro e isolado onde funcionários e gerentes operam exclusivamente as informações da loja à qual pertencem.
3. **Vitrine Pública (`/lojas/{slug}`):** Endereço digital exclusivo de cada loja (ex.: `/lojas/aurora`), onde o cliente final navega pelas peças disponíveis e solicita provas sem necessidade de cadastro antecipado.

---

## ⚡ Funcionalidades Principais

As funcionalidades do Vestis são organizadas por áreas de negócio:

- 👗 **Catálogo & Peças:** Cadastro detalhado de itens com fotos em proporção 4:5, especificações de cor, tamanho, tecido, valor e estado de conservação.
- 📨 **Solicitações de Prova:** Recepção e triagem de pedidos de interesse enviados por clientes na vitrine digital para datas específicas.
- 📅 **Locações & Agenda:** Calendário de reservas confirmadas, registro de retiradas, devoluções e prazos de higienização.
- 👥 **Base de Clientes:** Registro de clientes atendidos, histórico de aluguéis e canais de contato direto.
- ⚙️ **Gestão da Loja:** Configurações cadastrais do estabelecimento, parâmetros operacionais e gestão de funcionários.

---

## 📸 Demonstração Visual (Telas Implementadas)

> **Nota:** As telas abaixo são **capturas reais da aplicação**, correspondentes aos módulos que já foram desenvolvidos, testados e integrados ao backend:

### 1. Site Institucional (`GET /`)
*Página inicial da plataforma com apresentação do SaaS, diferenciais de mercado, demonstração do sistema e apresentação das lojas modelo.*

<div align="center">
  <img src="docs/screenshots/institutional/site-institucional.png" alt="Tela 01 - Site Institucional" width="850" />
</div>

<br>

### 2. Login Administrativo (`GET /login`)
*Acesso restrito aos colaboradores das lojas cadastradas, com formulário padronizado no Design System, ícones embutidos e proteção nativa contra CSRF.*

<div align="center">
  <img src="docs/screenshots/admin/login.png" alt="Tela 02 - Login Administrativo" width="460" />
</div>

<br>

### 3. Dashboard Administrativo (`GET /admin`)
*Painel principal do estabelecimento com barra lateral fixa de 220px, cabeçalho de 72px com identificação dinâmica da loja ativa, 4 stat cards operacionais zerados e empty states acolhedores.*

<div align="center">
  <img src="docs/screenshots/admin/dashboard.png" alt="Tela 03 - Dashboard Administrativo" width="850" />
</div>

---

## 🏛 Arquitetura do Sistema

O projeto adota a arquitetura de **Monólito Modular Multi-Tenant**:

- **Aplicação Única:** Um único deploy Spring Boot atende a todos os estabelecimentos com facilidade de evolução e baixo custo de infraestrutura.
- **Isolamento de Dados por Loja:** Banco de dados PostgreSQL compartilhado onde todas as entidades de negócio possuem a chave estrangeira `store_id`, garantindo isolamento lógico estrito.
- **Segurança Contínua em Tempo Real:** O componente `AdminStoreActiveAuthorizationManager` intercepta todas as requisições em `/admin/**`, checando dinamicamente no banco se a loja vinculada ao funcionário autenticado continua ativa antes de autorizar qualquer ação.
- **Resolução de Loja por Slug:** As vitrines públicas identificam o estabelecimento a partir da URL amigável (`/lojas/{slug}`).

### Estrutura dos Módulos Java (`com.clothingrental`)

```
src/main/java/com/clothingrental/
├── auth/            # [Implementado] Autenticação administrativa, sessões e segurança
├── tenant/          # [Implementado] Multi-tenancy, ciclo de vida de lojas e loja ativa
├── institutional/   # [Implementado] Controladores e visões do site público institucional
├── shared/          # [Implementado] Configurações transversais e design system
├── catalog/         # [Planejado] Acervo de peças, fotografias e estados operacionais
├── customer/        # [Planejado] Cadastro de clientes e histórico de locações
├── rentalrequest/   # [Planejado] Solicitações de prova recebidas pela vitrine
└── reservation/     # [Planejado] Locações oficiais e bloqueio de agenda
```

---

## 🛠 Tecnologias Utilizadas

| Camada | Tecnologia | Descrição e Papel no Projeto |
|---|---|---|
| **Linguagem** | **Java 21 (LTS)** | Linguagem base com recursos modernos de produtividade e tipagem forte |
| **Framework** | **Spring Boot 4.1.2** | Núcleo da aplicação (Spring MVC, Data JPA, Security e Validation) |
| **Segurança** | **Spring Security 7.0** | Autenticação com BCrypt, proteção CSRF e autorização dinâmica multi-tenant |
| **Banco de Dados** | **PostgreSQL 17** | Banco relacional containerizado via Docker |
| **Migrações** | **Flyway** | Versionamento e evolução estrutural do esquema do banco de dados |
| **Templates** | **Thymeleaf** | Renderização server-side moderna com layouts e fragmentos reutilizáveis |
| **Frontend** | **HTML5 + CSS3 + JS** | Design System próprio, responsivo e estilizado com Bootstrap 5.3.3 local |
| **Testes** | **JUnit 5 + Mockito** | **98 testes automatizados** cobrindo autenticação, multi-tenant e rotas |

---

## 🔄 Regras de Negócio e Fluxo de Locação

O fluxo de atendimento foi concebido para espelhar a dinâmica real de lojas de aluguel:

1. **Navegação na Vitrine:** O cliente acessa a vitrine pública da loja (`/lojas/{slug}`) e escolhe a peça desejada.
2. **Envio da Solicitação:** O cliente informa a data pretendida do evento e seus dados de contato (nome, telefone e e-mail).
3. **Triagem pela Loja:** A equipe do estabelecimento recebe o pedido no painel e analisa a agenda da peça.
4. **Agendamento de Prova:** O funcionário contata o cliente para agendar a prova presencial de caimento.
5. **Confirmação e Locação:** Somente após o cliente experimentar a peça e aprovar o aluguel, a loja registra a **locação oficial** no sistema.

> ⚠️ **Princípio Fundamental:** Uma solicitação de interesse **não equivale a uma locação confirmada** e **não bloqueia automaticamente o calendário**. Somente a locação oficial formalizada pela loja bloqueia as datas no acervo.

---

## 🚀 Como Executar Localmente

### Pré-requisitos
- **Java 21** instalado (ex.: Eclipse Temurin 21)
- **Docker Desktop** ativo

### Passo a passo rápido (PowerShell):

```powershell
# 1. Clonar o repositório
git clone https://github.com/madu-santos/Projeto-vestis.git
cd Projeto-vestis

# 2. Criar e configurar o .env a partir do modelo
Copy-Item .env.example .env
# Abra o .env e defina suas credenciais locais (ADMIN_PASSWORD com 12+ caracteres)

# 3. Subir o container do PostgreSQL
docker compose up -d

# 4. Executar a aplicação
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\temurin-21.0.13"  # ajuste caso seu JDK 21 esteja em outro caminho
.\mvnw.cmd spring-boot:run
```

Após o início, acesse no navegador:
- 🌐 **Site Institucional:** [http://localhost:8080/](http://localhost:8080/)
- 🔑 **Login Administrativo:** [http://localhost:8080/login](http://localhost:8080/login)
- 📊 **Painel Administrativo:** [http://localhost:8080/admin](http://localhost:8080/admin)

Para rodar todos os testes automatizados:
```powershell
.\mvnw.cmd test
```

---

## 📊 Estado Atual do Projeto

| Módulo / Funcionalidade | Estado | Situação Atual |
|---|:---:|---|
| **Fundação Técnica & Configurações** | ✅ **Implementado** | Spring Boot 4.1.2, Java 21, Flyway e Docker Compose |
| **Arquitetura Multi-Tenant** | ✅ **Implementado** | Isolamento por `store_id` e identificação pública por slug |
| **Autenticação & Controle de Sessões** | ✅ **Implementado** | Spring Security 7, hash BCrypt e autorização dinâmica de loja ativa |
| **Site Institucional (Tela 01)** | ✅ **Implementado** | Posicionamento comercial, apresentação e vitrines parceiras |
| **Login Administrativo (Tela 02)** | ✅ **Implementado** | Layout de 44px, ícones vetoriais, proteção CSRF e alertas |
| **Dashboard Administrativo (Tela 03)** | ✅ **Implementado** | Sidebar de 220px, header de 72px, contadores neutros e empty states |
| **Design System Consolidado** | ✅ **Implementado** | Paleta terracota/marrom nobre, tipografia e medidas canônicas |
| **Catálogo & Cadastro de Peças (Telas 04 e 05)** | ⏳ *Planejado* | Gestão de peças, upload de fotografias e medidas |
| **Solicitações de Prova (Telas 06 e 07)** | ⏳ *Planejado* | Triagem de pedidos e fluxo de contato da loja |
| **Locações & Agenda (Tela 08)** | ⏳ *Planejado* | Calendário de retiradas, devoluções e contratos |
| **Gestão de Clientes (Tela 09)** | ⏳ *Planejado* | Base de clientes e histórico de locações anteriores |
| **Configurações da Loja (Tela 10)** | ⏳ *Planejado* | Horários, dados do estabelecimento e parâmetros operacionais |
| **Vitrine Pública da Loja (Telas 11 a 13)** | ⏳ *Planejado* | Catálogo online e formulário de solicitação de prova |

---

## 🎨 Protótipos Visuais (Fases Planejadas)

> 💡 **Aviso de Transparência:** As telas desta seção representam **protótipos visuais aprovados** que definem o projeto de interface e a experiência de usuário para as próximas fases de implementação. Não representam funcionalidades prontas nesta versão.

<div align="center">
  <img src="docs/screenshots/visao-geral.png" alt="Visão Geral dos Protótipos" width="900" />
</div>

<br>

<details>
<summary><strong>👉 Clique para expandir os protótipos do Painel Administrativo (Telas 04 a 09)</strong></summary>
<br>

| Tela 04 — Catálogo de Peças | Tela 05 — Cadastro de Nova Peça |
| :---: | :---: |
| <img src="docs/screenshots/admin/catalogo.png" width="420" alt="Protótipo do Catálogo" /><br><em>Protótipo: Gestão do acervo com filtros</em> | <img src="docs/screenshots/admin/cadastro-peca.png" width="420" alt="Protótipo de Cadastro de Peça" /><br><em>Protótipo: Formulário com fotos na proporção 4:5</em> |

| Tela 06 — Gestão de Solicitações | Tela 07 — Locações e Agenda |
| :---: | :---: |
| <img src="docs/screenshots/admin/solicitacoes.png" width="420" alt="Protótipo de Solicitações" /><br><em>Protótipo: Triagem de solicitações recebidas</em> | <img src="docs/screenshots/admin/locacoes-agenda.png" width="420" alt="Protótipo de Locações e Agenda" /><br><em>Protótipo: Calendário de retiradas e devoluções</em> |

| Tela 08 — Base de Clientes | Tela 09 — Configurações da Loja |
| :---: | :---: |
| <img src="docs/screenshots/admin/clientes.png" width="420" alt="Protótipo de Clientes" /><br><em>Protótipo: Listagem e histórico de clientes</em> | <img src="docs/screenshots/admin/configuracoes.png" width="420" alt="Protótipo de Configurações" /><br><em>Protótipo: Parâmetros e dados da loja</em> |

</details>

<details>
<summary><strong>👉 Clique para expandir os protótipos da Vitrine Pública da Loja (Telas 10 a 13)</strong></summary>
<br>

| Tela 10 — Vitrine Digital (`/lojas/{slug}`) | Tela 11 — Detalhes da Peça |
| :---: | :---: |
| <img src="docs/screenshots/public-store/vitrine.png" width="420" alt="Protótipo da Vitrine Digital" /><br><em>Protótipo: Catálogo aberto para clientes finais</em> | <img src="docs/screenshots/public-store/detalhe-peca.png" width="420" alt="Protótipo de Detalhes da Peça" /><br><em>Protótipo: Ficha técnica, medidas e fotos</em> |

| Tela 12 — Solicitação de Prova | Tela 13 — Confirmação do Envio |
| :---: | :---: |
| <img src="docs/screenshots/public-store/solicitar-prova.png" width="420" alt="Protótipo de Solicitação de Prova" /><br><em>Protótipo: Formulário de interesse e data</em> | <img src="docs/screenshots/public-store/confirmacao.png" width="420" alt="Protótipo de Confirmação" /><br><em>Protótipo: Protocolo e orientações de contato</em> |

</details>

---

<div align="center">
  <p>Desenvolvido por <strong>Maria Eduarda Santos</strong></p>
  <p><a href="https://github.com/madu-santos">github.com/madu-santos</a></p>
</div>
