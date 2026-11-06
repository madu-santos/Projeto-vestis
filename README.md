<div align="center">
  <img src="docs/assets/vestis-logo.svg" alt="Vestis Logo" width="130" />
  <h1>Vestis — Gestão inteligente para lojas de locação</h1>
  <p><strong>Plataforma SaaS multi-tenant para gestão de acervos, controle de locações e vitrines digitais para lojas de aluguel de roupas e alta costura.</strong></p>
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

## 📸 Demonstração Visual das Telas

<div align="center">
  <img src="docs/screenshots/visao-geral.png" alt="Visão Geral do Vestis" width="900" />
</div>

<br>

### ✅ Telas Já Implementadas

| Tela 01 — Site Institucional (`GET /`) | Tela 02 — Login Administrativo (`GET /login`) |
| :---: | :---: |
| <img src="docs/screenshots/institutional/site-institucional.png" width="440" /> | <img src="docs/screenshots/admin/login.png" width="440" /> |

<div align="center">
  <h4>Tela 03 — Dashboard Administrativo (<code>GET /admin</code>)</h4>
  <img src="docs/screenshots/admin/dashboard.png" width="800" />
</div>

---

### ⏳ Protótipos das Próximas Fases (Telas Planejadas)

<details>
<summary><strong>👉 Clique aqui para ver os protótipos do Painel Administrativo (Telas 04 a 09)</strong></summary>
<br>

| Tela 04 — Catálogo de Peças | Tela 05 — Cadastro de Nova Peça |
| :---: | :---: |
| <img src="docs/screenshots/admin/catalogo.png" width="420" /> | <img src="docs/screenshots/admin/cadastro-peca.png" width="420" /> |

| Tela 06 — Gestão de Solicitações | Tela 07 — Locações e Agenda |
| :---: | :---: |
| <img src="docs/screenshots/admin/solicitacoes.png" width="420" /> | <img src="docs/screenshots/admin/locacoes-agenda.png" width="420" /> |

| Tela 08 — Base de Clientes | Tela 09 — Configurações da Loja |
| :---: | :---: |
| <img src="docs/screenshots/admin/clientes.png" width="420" /> | <img src="docs/screenshots/admin/configuracoes.png" width="420" /> |

</details>

<details>
<summary><strong>👉 Clique aqui para ver os protótipos da Vitrine Pública da Loja (Telas 10 a 13)</strong></summary>
<br>

| Tela 10 — Vitrine Digital (`/lojas/{slug}`) | Tela 11 — Detalhes da Peça |
| :---: | :---: |
| <img src="docs/screenshots/public-store/vitrine.png" width="420" /> | <img src="docs/screenshots/public-store/detalhe-peca.png" width="420" /> |

| Tela 12 — Solicitação de Prova | Tela 13 — Confirmação do Envio |
| :---: | :---: |
| <img src="docs/screenshots/public-store/solicitar-prova.png" width="420" /> | <img src="docs/screenshots/public-store/confirmacao.png" width="420" /> |

</details>

---

## 📖 O que é o Vestis?

O **Vestis** é um SaaS multi-tenant focado nas necessidades reais de **lojas de aluguel de roupas e alta costura** (vestidos de festa, ternos, trajes a rigor e fantasias). 

Diferente de um e-commerce comum de venda, o aluguel exige acompanhar a **disponibilidade temporal de cada peça física**, intervalos de higienização e consertos, além de agendamentos de provas presenciais. O Vestis substitui planilhas e anotações manuais por uma gestão centralizada e profissional.

### Os 3 Ambientes da Plataforma
1. **Site Institucional (`/`):** Apresentação do software e captação de novas lojas parceiras.
2. **Painel Administrativo (`/admin`):** Acesso exclusivo dos funcionários de cada loja para gerenciar acervo, locações e clientes.
3. **Vitrine Pública (`/lojas/{slug}`):** Catálogo online exclusivo da loja para clientes finais consultarem peças e solicitarem provas sem precisar de cadastro prévio.

---

## 🔄 Regras de Negócio em Destaque

- **Solicitação não é reserva:** O cliente escolhe uma data e pede para experimentar. Isso manifesta interesse, mas **não bloqueia** o calendário da peça.
- **Locação oficial bloqueia a agenda:** O período só fica indisponível para outros clientes quando a loja confirma o contrato presencialmente e registra a locação oficial no painel.
- **Ciclo operacional da peça:** A peça transita por estados independentes (`Disponível`, `Locada`, `Em higienização`, `Em manutenção`), mantendo o controle interno da loja sempre em dia.

---

## 🏛 Arquitetura e Tecnologias

- **Padrão:** Monólito modular multi-tenant com banco compartilhado.
- **Isolamento de Dados:** Cada loja é isolada logicamente pelo identificador `store_id`.
- **Segurança Contínua:** Spring Security com `AuthorizationManager` customizado que valida em tempo real se o estabelecimento do usuário continua ativo no banco de dados.

| Camada | Tecnologias Utilizadas |
|---|---|
| **Backend** | Java 21 LTS, Spring Boot 4.1.2, Spring MVC, Spring Data JPA, Hibernate 7 |
| **Segurança** | Spring Security 7.0, BCrypt, CSRF Protection |
| **Banco de Dados** | PostgreSQL 17 (Docker), versionamento com Flyway |
| **Frontend** | Thymeleaf, HTML5, CSS3 (Design System próprio), Bootstrap 5.3.3 local |
| **Testes** | JUnit 5, Mockito, Spring Security Test (**98 testes automatizados aprovados**) |

---

## 🚀 Como Executar Localmente

### Pré-requisitos
- **Java 21** instalado
- **Docker Desktop** ativo

### Passo a passo rápido (PowerShell):

```powershell
# 1. Clonar o projeto
git clone https://github.com/madu-santos/Projeto-vestis.git
cd Projeto-vestis

# 2. Criar e configurar o .env a partir do modelo
Copy-Item .env.example .env
# Abra o .env e preencha as variáveis locais (STORE_NAME, ADMIN_USERNAME, ADMIN_PASSWORD com 12+ chars)

# 3. Subir o PostgreSQL
docker compose up -d

# 4. Executar a aplicação
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\temurin-21.0.13"  # ajuste para seu JDK 21 caso necessário
.\mvnw.cmd spring-boot:run
```

Após iniciar, acesse no navegador:
- 🌐 **Institucional:** [http://localhost:8080/](http://localhost:8080/)
- 🔑 **Login:** [http://localhost:8080/login](http://localhost:8080/login)
- 📊 **Painel Admin:** [http://localhost:8080/admin](http://localhost:8080/admin)

Para rodar a suíte completa de testes:
```powershell
.\mvnw.cmd test
```

---

## 📊 Status do Desenvolvimento

| Módulo / Recurso | Status | Descrição |
|---|:---:|---|
| **Fundação Técnica & Multi-Tenancy** | ✅ Pronto | Spring Boot 4, Flyway, PostgreSQL 17 e isolamento por `store_id`. |
| **Segurança & Sessões** | ✅ Pronto | Autenticação administrativa com validação dinâmica de loja ativa. |
| **Site Institucional (Tela 01)** | ✅ Pronto | Página de apresentação da plataforma e diferenciais. |
| **Login Administrativo (Tela 02)** | ✅ Pronto | Formulário seguro com 44px, ícones e proteção CSRF. |
| **Dashboard Administrativo (Tela 03)** | ✅ Pronto | Sidebar 220px, header 72px, métricas e empty states. |
| **Design System Consolidado** | ✅ Pronto | Cores oficiais, tipografia e medidas canônicas documentadas. |
| **Catálogo & Cadastro de Peças (Telas 04 e 05)** | ⏳ Planejado | Próxima fase: acervo, upload de fotos 4:5 e medidas. |
| **Solicitações & Locações (Telas 06 e 07)** | ⏳ Planejado | Próxima fase: triagem de pedidos, agenda e contratos. |
| **Clientes & Configurações (Telas 08 e 09)** | ⏳ Planejado | Próxima fase: base de clientes e dados da loja. |
| **Vitrine Pública (Telas 10 a 13)** | ⏳ Planejado | Próxima fase: catálogo aberto por slug e solicitação de prova. |

---

<div align="center">
  <p>Desenvolvido por <strong>Maria Eduarda Santos</strong></p>
  <p><a href="https://github.com/madu-santos">github.com/madu-santos</a></p>
</div>
