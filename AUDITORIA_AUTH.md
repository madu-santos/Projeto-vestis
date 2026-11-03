# Relatório de Auditoria Técnica — Módulo de Autenticação (Auth)

**Data da Auditoria:** 31 de Outubro de 2026  
**Sistema:** clothing-rental (Nome técnico provisório)  
**Tecnologias:** Java 21, Spring Boot 4.1.2, Spring Security 7, Spring Data JPA, PostgreSQL 17, Flyway, Thymeleaf, JUnit 5, Mockito.  
**Escopo:** Inspeção completa do código-fonte, arquitetura, segurança, configurações e testes do módulo `auth`.

---

## 1. Resumo da Auditoria

O módulo de autenticação foi construído com alto padrão de qualidade, aderente aos padrões do **Spring Boot 4.1.2** e **Spring Security 7**. A separação de responsabilidades segue com precisão o princípio de monólito modular: entidades não vazam para a camada de segurança, credenciais são tratadas com algoritmos criptográficos robustos (BCrypt), a proteção CSRF está ativa em todos os pontos críticos e a suíte de testes (36 casos) cobre cenários fundamentais de autenticação, autorização e persistência.

Foram identificados pontos de atenção preventivos — principalmente relacionados a convenções de rotas que impactarão os futuros módulos (`catalog`, `rentalrequest`, etc.) e ao tratamento de concorrência/normalização de nomes de usuário.

---

## 2. Pontos Aprovados

### Segurança
- **Isolamento de Credenciais:** As senhas nunca trafegam ou repousam em texto puro. O armazenamento na tabela `staff_accounts` utiliza exclusivamente hash gerado pelo `BCryptPasswordEncoder` (comprimento padrão de 60 caracteres com prefixo `$2a$10$`).
- **Não Exposição de Dados Sensíveis:** Nem a entidade `StaffAccount` nem os controllers/templates expõem o campo `passwordHash` para a interface pública ou em logs.
- **Proteção CSRF Rigorosa:** Formulários utilizam `th:action`, garantindo a injeção do token CSRF. Testes comprovam que submissões de `POST /login` e `POST /logout` sem token são barradas com `HTTP 403 Forbidden`.
- **Logout Seguro:** Implementado obrigatoriamente via requisição HTTP `POST` com invalidação explícita de sessão (`session.isInvalid() == true`) e remoção do cookie `JSESSIONID`.
- **Prevenção de Ataques de Enumeração de Usuários:** A página `login.html` exibe a mensagem genérica *"Usuário ou senha inválidos"*, sem distinguir se a conta inexiste ou se a senha foi digitada incorretamente.
- **Isolamento do Perfil `dev`:** O `DevAdminAccountInitializer` é anotado com `@Profile("dev")`. O teste de integração verifica explicitamente que o bean não é carregado no perfil `test`.
- **Validação de Senha em Bytes:** `StaffAccountService` valida o teto de 72 bytes da senha, prevenindo o truncamento silencioso inerente à especificação do algoritmo BCrypt.

### Arquitetura e Engenharia de Software
- **Desacoplamento de Domínio:** A entidade JPA `StaffAccount` não implementa `UserDetails`. A adaptação para o contrato do Spring Security é feita unicamente pelo serviço `StaffAccountDetailsService`.
- **Controllers Enxutos:** `LoginController` e `AdminHomeController` limitam-se a retornar o nome das views lógicas, sem regras de negócio ou lógica de validação.
- **Controle de Banco via Flyway:** A criação da tabela `staff_accounts` é governada pelo script `V1__create_staff_accounts.sql`, enquanto o Hibernate opera com `ddl-auto: validate`.
- **Transacionalidade Correta:** Métodos de leitura usam `@Transactional(readOnly = true)`, e operações de escrita usam `@Transactional`.

---

## 3. Problemas Críticos

> **Nenhum problema crítico foi encontrado.** A aplicação não possui vulnerabilidades de execução remota, injeção de SQL, vazamento direto de senhas em logs ou desativação acidental de CSRF/autenticação.

---

## 4. Problemas de Menor Gravidade

### [MÉDIO] Risco de Exposição de Métodos HTTP em Rotas Públicas Futuras
- **Arquivo:** `src/main/java/com/clothingrental/auth/config/SecurityConfig.java`
- **Linhas:** 23 a 25 e 37
- **Descrição:** O array `PUBLIC_PATHS` libera `/catalog/**`, `/garments/**` e `/rental-requests/**` para **qualquer método HTTP** (`GET`, `POST`, `PUT`, `DELETE`).
- **Impacto:** Quando os módulos `catalog` e `rentalrequest` forem implementados, se um desenvolvedor criar uma rota como `POST /catalog/delete` ou `PUT /garments/1/edit` fora do prefixo `/admin/**`, o Spring Security permitirá o acesso anônimo sem exigir login.
- **Ação sugerida:** Restringir a liberação das rotas públicas por método HTTP (ex.: `HttpMethod.GET` para catálogo) ou estabelecer como convenção arquitetural mandatória que **todas** as rotas de mutação administrativa fiquem estritamente abaixo de `/admin/**` (ex.: `/admin/catalog/**`, `/admin/garments/**`).

---

### [BAIXO] Ausência de Normalização de Caixa Alta/Baixa no Username
- **Arquivo:** `src/main/java/com/clothingrental/auth/service/StaffAccountService.java`
- **Método:** `validateUsername(String username)`
- **Descrição:** O método executa `username.trim()`, mas não normaliza o texto para minúsculas (`toLowerCase(Locale.ROOT)`).
- **Impacto:** No PostgreSQL, campos `VARCHAR` com restrição `UNIQUE` são sensíveis a maiúsculas/minúsculas (*case-sensitive*). Usuários `Admin` e `admin` seriam tratados como duas contas diferentes no banco, o que pode causar confusão operacional na loja.
- **Ação sugerida:** Normalizar o username com `trimmed.toLowerCase(Locale.ROOT)` no momento do cadastro e na consulta.

---

### [BAIXO] Falta de Tratamento de Exceção de Unicidade em Nível de Serviço
- **Arquivo:** `src/main/java/com/clothingrental/auth/service/StaffAccountService.java`
- **Método:** `createAccount(String username, String rawPassword)`
- **Descrição:** Não há verificação prévia (`findByUsername`) ou captura de `DataIntegrityViolationException`.
- **Impacto:** Se houver tentativa de criar um usuário que já existe, uma exceção genérica de banco é lançada diretamente para a pilha de execução. No MVP atual (onde apenas o inicializador roda no startup), isso não causa problemas ao usuário final, mas será relevante quando houver telas de gestão de usuários.
- **Ação sugerida:** Lançar uma exceção de domínio clara (ex.: `UsernameAlreadyExistsException`).

---

## 5. Melhorias Recomendadas

1. **Mensagens de Validação de Senha Específicas:**
   No arquivo `StaffAccountService.java`, adicionar validação de complexidade básica (ex.: conter ao menos uma letra e um número), mantendo o requisito já existente de 12 caracteres.
2. **Auditoria de Entidade:**
   No arquivo `StaffAccount.java`, adicionar futuramente campos de auditoria como `lastLoginAt` para controle operacional da loja.
3. **Cabeçalhos de Segurança (CSP):**
   No arquivo `SecurityConfig.java`, adicionar configuração de *Content Security Policy* quando integrarmos os recursos estáticos e CDN do Bootstrap 5.

---

## 6. Testes que Deveriam ser Acrescentados

Embora a cobertura atual de 36 testes seja ampla e sólida, os seguintes cenários complementares enriqueceriam ainda mais a suíte de testes:

1. **Tentativa de criação de conta com Username duplicado:**
   - Testar o comportamento de `StaffAccountRepository` ao violar a constraint `uk_staff_accounts_username`.
2. **Login com usuário contendo caracteres especiais ou espaços laterais:**
   - Garantir que o login funcione de forma previsível caso o usuário digite espaços acidentais antes ou depois do nome de usuário.
3. **Acesso anônimo à rota pública `/error`:**
   - Garantir que o tratamento de erro do Spring MVC não gere loop de redirecionamento para `/login`.
4. **Verificação da rota `POST /login` sem credenciais (vazio):**
   - Validar que requisição POST direta sem parâmetros redireciona para `/login?error` sem quebrar o servidor com 500.

---

## 7. Verificação de Integração com Módulos Futuros

| Módulo Futuro | Integração com o Módulo Auth | Situação |
|---|---|:---:|
| **`catalog`** | Vitrine pública (`/catalog/**`, `/garments/**`) já está liberada no `SecurityConfig`. O cadastro e edição de peças deve ser mapeado sob `/admin/catalog/**`. | **Pronto** |
| **`rentalrequest`**| Envio público de interesse (`/rental-requests/**`) já está liberado. A gestão, aprovação e agendamento de provas devem residir sob `/admin/rental-requests/**`. | **Pronto** |
| **`reservation`** | Operações de reserva e locação oficial são de uso exclusivo dos funcionários, ficando naturalmente sob `/admin/reservations/**`. | **Pronto** |
| **`customer`** | Dados de clientes cadastrados no momento da locação oficial serão geridos exclusivamente pelos operadores sob `/admin/customers/**`. | **Pronto** |

---

## 8. Conclusão sobre a Qualidade do Módulo

O módulo `auth` está **Aprovado**. A base técnica é consistente, segue os princípios SOLID, não possui código morto ou dependências desnecessárias, e fornece proteção segura para as rotas administrativas sem impedir a navegação da futura área pública.

Os pontos levantados são de severidade média e baixa (recomendações preventivas de boa prática) e não impedem o avanço do desenvolvimento.
