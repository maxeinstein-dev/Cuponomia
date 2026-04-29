# 🎟️ Cuponomia — Sistema de Cupons com Regras Dinâmicas

Sistema de cupons de desconto com regras dinâmicas para aplicação no checkout, construído com **Java 25**, **Spring Boot 4.0.6**, seguindo **Clean Architecture (Hexagonal)**, **DDD** e **TDD**.

## 📋 Índice

- [Funcionalidades](#-funcionalidades)
- [Arquitetura](#-arquitetura)
- [Decisões Técnicas](#-decisões-técnicas)
- [Como Rodar](#-como-rodar)
- [API Endpoints](#-api-endpoints)
- [Dados de Exemplo](#-dados-de-exemplo)
- [Exemplos de Uso](#-exemplos-de-uso)
- [Testes](#-testes)
- [Estrutura do Projeto](#-estrutura-do-projeto)

---

## ✨ Funcionalidades

### 🏷️ Gerenciamento de Cupons

- **Criar cupom** com código único (3–30 caracteres alfanuméricos), tipo e valor de desconto:
  - **FIXED** — desconto de valor fixo em reais (ex: R$ 20,00). Limitado ao total do pedido (nunca gera valor negativo).
  - **PERCENTAGE** — desconto percentual (1% a 100%). Calculado sobre o total do pedido com precisão de 2 casas decimais.
- **Listar cupons** com filtro opcional por status (`active=true` / `active=false`).
- **Buscar cupom** pelo código único.
- **Desativar cupom** via PATCH, impedindo seu uso em novos checkouts sem excluí-lo do banco.

### 📏 Regras Dinâmicas e Composáveis

As regras são implementadas com o **Specification Pattern**: cada regra é uma classe independente. Um cupom pode ter zero ou mais regras combinadas. Todas são avaliadas e os erros são acumulados — a API retorna **todos os motivos de rejeição** de uma vez, não apenas o primeiro.

| Regra | Descrição |
|---|---|
| **Valor mínimo do pedido** | O pedido deve atingir um valor mínimo em reais para o cupom ser válido. |
| **Expiração por data** | O cupom só é válido até uma data/hora específica. Após esse momento, é recusado automaticamente. |
| **Uso único por cliente** | Cada `clientId` só pode usar o cupom uma vez. Tentativas de reuso são bloqueadas na aplicação **e** por constraint `UNIQUE` no banco de dados. |
| **Limite máximo de usos** | O cupom tem um número máximo de utilizações totais. Ao atingir o limite, passa a ser recusado para qualquer cliente. |

### 🛒 Checkout com Feedback Detalhado

O endpoint de checkout **não lança erros 4xx** para cupons inválidos — retorna sempre `200 OK` com o campo `valid` indicando o resultado. Isso permite que o front-end exiba mensagens amigáveis sem tratar exceções.

```json
// Cupom válido
{
  "valid": true,
  "couponCode": "MAX50",
  "originalTotal": 16000.00,
  "discountApplied": 8000.00,
  "finalTotal": 8000.00,
  "message": "Cupom aplicado com sucesso! Você economizou R$ 8000.00",
  "errors": []
}

// Cupom inválido (todos os erros retornados juntos)
{
  "valid": false,
  "couponCode": "EXPIRADO20",
  "originalTotal": 50.00,
  "discountApplied": 0,
  "finalTotal": 50.00,
  "message": "Falha na validação do cupom",
  "errors": [
    "O cupom expirou em 2025-01-01T00:00",
    "Pedido abaixo do valor mínimo de R$ 100,00"
  ]
}
```

### 🔒 Segurança Contra Reuso Inválido

Proteção em duas camadas para cupons de uso único:
1. **Camada de aplicação:** consulta o histórico de uso antes de calcular o desconto.
2. **Camada de banco:** constraint `UNIQUE (coupon_id, client_id)` impede gravações duplicadas mesmo em cenários de alta concorrência (race conditions).

### 📋 Auditoria de Uso

Cada uso de cupom é registrado com: `couponCode`, `clientId`, `orderTotal`, `discountApplied` e `usedAt`. Isso garante rastreabilidade completa para fins de auditoria e analytics.

### 📖 API REST Documentada

Documentação interativa disponível via **Swagger UI** em `/swagger-ui.html`. Todos os endpoints possuem descrições, exemplos de request/response e códigos de status documentados.

### 🌱 Dados Iniciais (Seed)

No perfil `dev`, a aplicação inicializa automaticamente com cupons de exemplo cobrindo todos os cenários: desconto fixo, percentual, expirado, inativo, com e sem regras.

---

## 🏗️ Arquitetura

O projeto segue **Arquitetura Hexagonal (Ports & Adapters)** com separação clara de camadas:

```
┌─────────────────────────────────────────────────────┐
│                   interfaces/                       │
│          Controllers REST (thin)                    │
│          GlobalExceptionHandler                     │
├─────────────────────────────────────────────────────┤
│                  application/                       │
│          Use Cases (orquestração)                   │
│          DTOs, Mappers                              │
├─────────────────────────────────────────────────────┤
│                    domain/                          │
│          Entidades ricas, Value Objects             │
│          Regras (Specification Pattern)             │
│          Ports (interfaces de repositório)          │
├─────────────────────────────────────────────────────┤
│                infrastructure/                      │
│          JPA Entities + Adapters                    │
│          Spring Data Repositories                   │
│          Configurações                              │
└─────────────────────────────────────────────────────┘
```

**Princípio fundamental:** O domínio **nunca** depende de frameworks. As interfaces de repositório (Ports) são definidas no domínio; as implementações (Adapters) vivem na infraestrutura.

---

## 🧠 Decisões Técnicas

### UUID para IDs

IDs sequenciais (Long) permitiriam ataques de enumeração — um atacante poderia adivinhar IDs de cupons válidos. UUID elimina esse vetor de ataque e permite geração sem dependência do banco.

### Strategy Pattern para tipos de desconto

Novos tipos de desconto (frete grátis, desconto progressivo) podem ser adicionados criando apenas uma nova classe, sem alterar código existente (Open/Closed Principle).

### Specification Pattern para regras

Cada regra é uma classe independente que implementa `CouponRule`. Regras são compostas via `ValidationResult.combine()`, acumulando todas as violações para feedback claro ao usuário. Adicionar uma nova regra requer zero alteração em código existente.

### Entidades JPA separadas do domínio

`CouponEntity` (JPA) e `Coupon` (domínio) são classes distintas. O Adapter faz a conversão. Isso evita anotações JPA no domínio, mantendo-o puro e testável.

### Entidades ricas (não anêmicas)

A entidade `Coupon` encapsula lógica de negócio: cálculo de desconto, validação de regras, e gerenciamento de lifecycle. Controllers são thin — apenas delegam para use cases.

### Segurança contra reuso

- **Aplicação:** `SingleUsePerClientRule` + `CheckoutContext` enriquecido com dados de uso
- **Banco:** Constraint UNIQUE em `(coupon_id, client_id)` na tabela `coupon_usages`
- **Concorrência:** `@Version` (locking otimista) + `@Transactional` nos use cases
- **Tratamento:** `DataIntegrityViolationException` convertida em resposta 409

---

## 🚀 Como Rodar

### Pré-requisitos

- Java 25+
- Maven 3.9+ (ou use o wrapper `./mvnw`)

### Local (Profile: dev)

```bash
./mvnw spring-boot:run
```

A aplicação inicia em `http://localhost:8080` com:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **H2 Console:** http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:cuponomia_dev`)

> 💡 Ao iniciar, **7 cupons de exemplo** são carregados automaticamente via `data.sql` para testes rápidos.

### Docker

```bash
docker compose up --build
```

Acesse em `http://localhost:8080`.

### Testes

```bash
./mvnw test
```

---

## 📡 API Endpoints

| Método | Path | Descrição |
|--------|------|-----------|
| `POST` | `/api/v1/coupons` | Criar cupom |
| `GET`  | `/api/v1/coupons` | Listar cupons (filtro `?active=true/false`) |
| `GET`  | `/api/v1/coupons/{code}` | Buscar cupom por código |
| `PATCH`| `/api/v1/coupons/{code}/deactivate` | Desativar cupom |
| `POST` | `/api/v1/checkout/apply-coupon` | Aplicar cupom no checkout |

---

## 📦 Dados de Exemplo

Ao iniciar o projeto (profile `dev`), os seguintes cupons são criados automaticamente:

| Código | Tipo | Valor | Cenário |
|--------|------|-------|---------|
| `BEMVINDO25` | Fixo | R$ 25 | Boas-vindas, uso único por cliente |
| `VERAO15` | Percentual | 15% | Todas as regras ativas |
| `PROMO10` | Fixo | R$ 10 | Sem regras extras |
| `MEGA50` | Percentual | 50% | Limite de 50 usos, valor mínimo R$ 200 |
| `EXPIRADO20` | Fixo | R$ 20 | Expirado (demonstra validação) |
| `INATIVO30` | Percentual | 30% | Desativado (demonstra validação) |
| `MAX50` | Percentual | 50% | 🎯 Easter Egg — veja abaixo! |

### 🎯 Easter Egg: Cupom MAX50

> 🚀 Use o cupom **MAX50** e tenha **50% de desconto** na sua contratação!

Cupom especial com desconto de 50%. Teste no Swagger com:
- **Cupom:** `MAX50`
- **Cliente:** `Taller`
- **Valor mínimo do pedido:** `16000.00`

---

## 💡 Exemplos de Uso

### Criar cupom com desconto fixo

```bash
curl -X POST http://localhost:8080/api/v1/coupons \
  -H "Content-Type: application/json" \
  -d '{
    "code": "ECONOMIZE20",
    "description": "Desconto de R$ 20 em compras acima de R$ 100",
    "discountType": "FIXED",
    "discountValue": 20.00,
    "rules": {
      "minimumOrderValue": 100.00,
      "singleUsePerClient": true
    }
  }'
```

### Criar cupom percentual com todas as regras

```bash
curl -X POST http://localhost:8080/api/v1/coupons \
  -H "Content-Type: application/json" \
  -d '{
    "code": "VERAO25",
    "description": "25% de desconto no verão",
    "discountType": "PERCENTAGE",
    "discountValue": 25,
    "rules": {
      "minimumOrderValue": 50.00,
      "expiresAt": "2027-12-31T23:59:59",
      "singleUsePerClient": true,
      "maxUsages": 1000
    }
  }'
```

### Aplicar cupom no checkout

```bash
curl -X POST http://localhost:8080/api/v1/checkout/apply-coupon \
  -H "Content-Type: application/json" \
  -d '{
    "couponCode": "ECONOMIZE20",
    "clientId": "cliente-123",
    "orderTotal": 150.00
  }'
```

**Resposta de sucesso:**
```json
{
  "valid": true,
  "couponCode": "ECONOMIZE20",
  "originalTotal": 150.00,
  "discountApplied": 20.00,
  "finalTotal": 130.00,
  "message": "Cupom aplicado com sucesso! Você economizou R$ 20.00",
  "errors": []
}
```

**Resposta de falha (valor mínimo):**
```json
{
  "valid": false,
  "couponCode": "ECONOMIZE20",
  "originalTotal": 50.00,
  "discountApplied": 0,
  "finalTotal": 50.00,
  "message": "Falha na validação do cupom",
  "errors": [
    "O valor do pedido R$ 50.00 está abaixo do mínimo exigido de R$ 100.00"
  ]
}
```

**Resposta de falha (cupom já utilizado):**
```json
{
  "valid": false,
  "couponCode": "ECONOMIZE20",
  "originalTotal": 150.00,
  "discountApplied": 0,
  "finalTotal": 150.00,
  "message": "Falha na validação do cupom",
  "errors": [
    "O cliente 'cliente-123' já utilizou o cupom 'ECONOMIZE20'"
  ]
}
```

### Listar cupons ativos

```bash
curl http://localhost:8080/api/v1/coupons?active=true
```

### Buscar cupom por código

```bash
curl http://localhost:8080/api/v1/coupons/MAX50
```

### Desativar cupom

```bash
curl -X PATCH http://localhost:8080/api/v1/coupons/ECONOMIZE20/deactivate
```

---

## 🧪 Testes

O projeto conta com **70 testes** (unitários e de integração) cobrindo:

| Tipo | Escopo | Ferramenta |
|------|--------|------------|
| Unitário | Value Objects (CouponCode, ValidationResult) | JUnit 5 |
| Unitário | Entidade Coupon (invariantes, cálculo, validação) | JUnit 5 |
| Unitário | Regras (MinOrder, Expiration, SingleUse, MaxUsage) | JUnit 5 |
| Unitário | Use Cases (Create, Apply) | JUnit 5 + Mockito |
| Integração | API REST completa (fluxo criar→aplicar→verificar) | SpringBootTest + RestClient |

```bash
# Rodar todos os testes
./mvnw test

# Rodar com relatório detalhado
./mvnw test -Dsurefire.useFile=false
```

---

## 📁 Estrutura do Projeto

```
src/main/java/br/com/maxsueleinstein/cuponomia/
├── CuponomiaApplication.java
├── domain/
│   ├── model/          # Entidades ricas e Value Objects
│   ├── rule/           # Regras de validação (Specification Pattern)
│   ├── exception/      # Exceções de domínio
│   └── repository/     # Ports (interfaces)
├── application/
│   ├── usecase/        # Casos de uso
│   ├── dto/            # DTOs de request/response
│   └── mapper/         # Conversão Domain ↔ DTO
├── infrastructure/
│   ├── persistence/    # JPA Entities, Repositories, Adapters
│   └── config/         # Configurações (OpenAPI)
└── interfaces/
    └── rest/           # Controllers REST + Exception Handler

src/main/resources/
├── application.yml          # Configuração geral
├── application-dev.yml      # Profile dev (H2 + seed data)
├── application-test.yml     # Profile test (isolado)
└── data.sql                 # Dados iniciais de exemplo (7 cupons)
```

---

## 🛠️ Tecnologias

- **Java 25** + **Spring Boot 4.0.6**
- **Spring Data JPA** + **H2 Database** (Dev/Test) e **PostgreSQL** (Produção/Docker)
- **Bean Validation** (Jakarta)
- **SpringDoc OpenAPI** (Swagger)
- **JUnit 5** + **Mockito**
- **Docker** + **Docker Compose**

---

## 📄 Licença

Projeto desenvolvido como desafio técnico.
