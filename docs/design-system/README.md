# Design System — Vestis

Este documento apresenta a especificação canônica do **Design System do Vestis**, consolidada a partir do pacote oficial de design (`Vestis_Pacote_de_Telas.pdf`). Todos os componentes, estilos visuais e layouts desenvolvidos no projeto seguem rigorosamente os padrões aqui definidos.

---

## 1. Identidade da Marca e Logotipo

- **Nome Comercial:** `vestis` (sempre em minúsculas, tipografia sans-serif robusta, peso semibold/bold, sem serifa).
- **Símbolo:** Naipe de paus (**♣** / *clubs suit*) desenhado em **traçado *outline* minimalista**, cantos externos suavemente arredondados e base contínua com alargamento sutil.
- **Disposição Oficial:** Layout vertical — símbolo de paus centralizado acima da marca tipográfica nominativa.
- **Ativos Vetoriais:**
  - `docs/assets/vestis-logo.svg`: aplicação escura para superfícies claras.
  - `docs/assets/vestis-logo-light.svg`: aplicação clara para superfícies escuras (barra lateral marrom).

---

## 2. Paleta de Cores Oficial

As cores do Vestis transmitem sofisticação, calor e elegância, ideais para o segmento de locação de trajes, vestidos e vestuário de festa.

| Variável CSS | Hexadecimal | Papel Semântico | Uso no Sistema |
|---|---|---|---|
| `--color-background` | `#F7F4F0` | Fundo geral | Fundo de páginas, telas de login e área de conteúdo |
| `--color-surface` | `#FFFCF8` | Superfície | Cartões, formulários, modais e cabeçalhos |
| `--color-primary` | `#B46B64` | Cor primária (Terracota) | Botões principais, chamadas de ação e destaques |
| `--color-primary-hover` | `#9E5852` | Primária escurecida | Estado hover e ativo de botões primários |
| `--color-secondary` | `#A1646C` | Secundária (Rosé) | Acentos secundários, tags especiais e detalhes finos |
| `--color-sidebar` | `#60342F` | Marrom estrutural nobre | Barra lateral administrativa fixa e banners de destaque |
| `--color-sidebar-active` | `#874941` | Terracota escuro | Item ativo ou hover na barra lateral administrativa |
| `--color-text` | `#39231F` | Texto primário | Títulos, textos corridos e valores de controle |
| `--color-text-muted` | `#7A6662` | Texto secundário | Subtítulos, legendas e textos auxiliares |
| `--color-text-light` | `#9B8A86` | Texto terciário | Placeholders e rodapés discretos |
| `--color-border` | `#E6D6CE` | Borda padrão | Divisões de seções, contorno de cartões e inputs |
| `--color-border-subtle` | `#F0E5DF` | Borda sutil | Separadores ultrafinos de listas |

### Badges de Estado e Tags Semânticas
- **Sucesso / Ativo / Disponível:** Fundo `#E8F4EC`, texto `#2E6B4F`
- **Atenção / Higienização:** Fundo `#FEF6E7`, texto `#9B6814`
- **Informativo / Reservado:** Fundo `#EBF2F8`, texto `#3C6186`
- **Neutro / Em cadastro:** Fundo `#EFE9E4`, texto `#6E5C58`

---

## 3. Tipografia e Escala

- **Títulos e Destaques:** `DM Serif Display`, serif (fallback: `Noto Serif Display`, `Georgia`, serif)
- **Interface e Formulários:** `Inter`, sans-serif (fallback: `Segoe UI`, `Arial`, sans-serif)

| Nível Hierárquico | Tamanho | Peso | Família |
|---|---|---|---|
| Título Principal de Página (H1) | `36px` | Normal (400) | DM Serif Display |
| Título do Hero Institucional | `42px` | Normal (400) | DM Serif Display |
| Título de Seção (H2) | `20px` | Bold (700) | Inter |
| Subtítulos e Chamadas | `16px` | Normal (400) | Inter |
| Corpo de Texto e Formulários | `14px` | Regular (400) / Medium (500) | Inter |
| Textos Auxiliares e Badges | `12px` | Medium (500) / Semi-bold (600) | Inter |

---

## 4. Medidas Canônicas e Espaçamentos

Consolidadas a partir do manual de medidas compartilhadas:

- **Viewport de Referência:** `1440 × 900 px`
- **Menu Lateral Fixo (Sidebar):** `220px` de largura fixa (`--sidebar-width: 220px;`)
- **Cabeçalho Superior Fixo (Header):** `72px` de altura fixa (`--header-height: 72px;`)
- **Padding da Área de Conteúdo:** `32px` (`--content-padding: 32px;`)
- **Espaço entre Seções:** `24px` (`--section-gap: 24px;`)
- **Espaço entre Campos de Formulário:** `16px` (`--field-gap: 16px;`)
- **Altura de Controles (Inputs e Botões):** `44px` (`--control-height: 44px;`)
- **Raio de Arredondamento de Cartões:** `14px` (`--card-radius: 14px;`)
- **Padding Interno de Cartões:** `20px` (`--card-padding: 20px;`)
- **Proporção Fotográfica de Peças:** Proporção `4:5` com enquadramento `object-fit: contain`
- **Tamanho Padrão de Ícones:** `20px` (`--icon-size: 20px;`)

---

## 5. Estrutura de Componentes

### 5.1. Barra Lateral Administrativa (`admin-sidebar`)
- Fixa à esquerda com `220px` de largura e cor `--color-sidebar` (`#60342F`).
- Logotipo oficial `vestis` branco no topo centralizado.
- Lista vertical de módulos com ícones de `20px`.
- Item ativo com fundo terracota escuro (`#874941`) e raio de `8px`.
- Rodapé com formulário de encerramento de sessão (`POST /logout` com proteção CSRF).

### 5.2. Cabeçalho Superior (`admin-header`)
- Fixo no topo com `72px` de altura e cor `--color-surface` (`#FFFCF8`).
- Lado esquerdo: Nome da loja atual resolvido dinamicamente pelo `CurrentStoreService` e badge `Loja Ativa`.
- Lado direito: Sino de notificações, avatar circular com iniciais e identificação do funcionário logado.

### 5.3. Botões e Controles de Entrada
- **Botão Primário (`.btn-vestis-primary`):** Altura `44px`, cor de fundo `--color-primary` (`#B46B64`), texto branco, cantos com `8px` de raio.
- **Botão Secundário / Contorno (`.btn-vestis-outline`):** Altura `44px`, borda de 1px com cor `--color-border`, texto `--color-text`.
- **Inputs de Texto (`.vestis-input`):** Altura `44px`, fundo `--color-surface`, borda de 1px com cor `--color-border`, suporte a ícones embutidos de 20px à esquerda e foco com anel suave terracota.

### 5.4. Cartões e Empty States
- **Cartão Padrão (`.app-card`):** Fundo `--color-surface` (`#FFFCF8`), raio de `14px`, padding de `20px` e borda sutil `--color-border`.
- **Stat Cards:** Cartões compactos com ícone temático, valor em destaque (28px a 32px) e legenda descritiva.
- **Empty States (`.empty-state`):** Área centralizada com ícone suave, mensagem didática e botão de ação direta, mantendo a tela acolhedora antes do cadastro dos primeiros registros.
