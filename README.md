# Cuponomia Microservices

Cuponomia e um sistema de cupons de desconto dividido em microservicos. Ele separa a escrita da gestao de cupons do caminho de checkout, usa Kafka para propagacao de eventos e mantem uma projecao local de dados para deixar a validacao mais rapida e resiliente.

## O Que O Projeto Faz

O projeto permite criar, listar, buscar e desativar cupons em um servico de gestao, enquanto um segundo servico valida e aplica o cupom no checkout.

Fluxo principal:

1. O `coupon-management-service` recebe a requisicao de criacao ou desativacao de cupom.
2. O cupom e persistido no banco do servico de gestao.
3. Depois do commit da transacao, o servico publica um evento no Kafka.
4. O `coupon-validation-service` consome esse evento e atualiza sua projecao local.
5. O checkout consulta o servico de validacao para aplicar o cupom.

Essa separacao permite que a validacao continue funcionando com a ultima projecao recebida, mesmo se o servico de gestao estiver indisponivel por um periodo curto.

## Estrutura Da Solucao

| Modulo                      | Responsabilidade                                                                                                     |
| --------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| `coupon-management-service` | API de administracao de cupons, regras de negocio, persistencia, publicacao de eventos Kafka e documentacao Swagger. |
| `coupon-validation-service` | API de checkout, validacao de cupom, consumo de eventos Kafka, persistencia local e documentacao Swagger.            |
| `coupon-contracts`          | Contratos compartilhados de eventos entre os servicos.                                                               |

O projeto segue uma organizacao em camadas dentro de cada servico:

- `interfaces` para controllers REST e handlers de erro.
- `application` para casos de uso, DTOs, mappers e portas de aplicacao.
- `domain` para entidades, regras e excecoes de negocio.
- `infrastructure` para JPA, Kafka, configuracao e adaptadores externos.

## Como A Solucao Esta Montada

```text
.
|-- coupon-contracts
|   `-- src/main/java/.../contracts/event
|-- coupon-management-service
|   |-- src/main/java/.../application
|   |-- src/main/java/.../domain
|   |-- src/main/java/.../infrastructure
|   `-- src/main/java/.../interfaces
|-- coupon-validation-service
|   |-- src/main/java/.../application
|   |-- src/main/java/.../domain
|   |-- src/main/java/.../infrastructure
|   `-- src/main/java/.../interfaces
|-- scripts
|   `-- run-affected-tests.ps1
|-- .githooks
|   `-- pre-commit
|-- docker-compose.yml
|-- Dockerfile
`-- pom.xml
```

## Stack

- Java 25
- Spring Boot 4.0.6
- Maven com multi-module build
- Kafka para eventos
- Postgres por servico
- H2 para testes locais quando aplicavel

## Como Rodar

Subir toda a stack:

```bash
docker compose up --build
```

Servicos expostos:

- Management Swagger: http://localhost:8081/swagger-ui.html
- Validation Swagger: http://localhost:8082/swagger-ui.html
- Kafka: `localhost:9092`
- Postgres gestao: `localhost:5432`, database `coupon_management`
- Postgres validacao: `localhost:5433`, database `coupon_validation`

Rodar todos os testes:

```bash
./mvnw test
```

Rodar um modulo localmente:

```bash
./mvnw -pl coupon-management-service -am spring-boot:run
./mvnw -pl coupon-validation-service -am spring-boot:run
```

## Fluxo De Testes Antes Do Commit

O repositorio usa um hook de pre-commit para bloquear o commit ate os testes afetados passarem.

### Como habilitar

Depois de clonar o repositorio, configure o Git para usar os hooks versionados:

```bash
git config core.hooksPath .githooks
```

No Windows, voce tambem pode usar:

```powershell
./scripts/install-git-hooks.ps1
```

### O Que O Hook Faz

O arquivo `.githooks/pre-commit` chama `scripts/run-affected-tests.ps1 -Staged` antes de aceitar o commit.

Esse script:

- identifica apenas os arquivos staged;
- roda somente os testes dos modulos impactados;
- executa a suite completa se houver mudanca no `pom.xml` raiz ou em artefatos de build;
- ignora mudancas apenas de documentacao, scripts ou comentarios em arquivos Java.

Em outras palavras, se voce mudar apenas um comentario em um arquivo, o hook nao precisa rodar todos os testes do projeto.

## Endpoints

Gestao de cupons (`coupon-management-service`, porta `8081`):

| Metodo  | Path                                | Descricao       |
| ------- | ----------------------------------- | --------------- |
| `POST`  | `/api/v1/coupons`                   | Criar cupom     |
| `GET`   | `/api/v1/coupons`                   | Listar cupons   |
| `GET`   | `/api/v1/coupons/{code}`            | Buscar cupom    |
| `PATCH` | `/api/v1/coupons/{code}/deactivate` | Desativar cupom |

Validacao de checkout (`coupon-validation-service`, porta `8082`):

| Metodo | Path                            | Descricao               |
| ------ | ------------------------------- | ----------------------- |
| `POST` | `/api/v1/checkout/apply-coupon` | Validar e aplicar cupom |

## Exemplo De Uso

Criando um cupom no servico de gestao:

```bash
curl -X POST http://localhost:8081/api/v1/coupons \
  -H "Content-Type: application/json" \
  -d '{
    "code": "SAVE20",
    "description": "Save R$ 20",
    "discountType": "FIXED",
    "discountValue": 20.00,
    "rules": {
      "minimumOrderValue": 100.00,
      "singleUsePerClient": true
    }
  }'
```

Depois que o evento for consumido, valide no checkout:

```bash
curl -X POST http://localhost:8082/api/v1/checkout/apply-coupon \
  -H "Content-Type: application/json" \
  -d '{
    "couponCode": "SAVE20",
    "clientId": "client-1",
    "orderTotal": 150.00
  }'
```

## Pontos De Estudo

- Banco por servico: cada microsservico possui seu proprio banco.
- Comunicacao assincrona: gestao nao chama validacao por HTTP.
- Projecao local: validacao le os cupons do seu proprio banco.
- Escala independente: e possivel subir mais replicas apenas do `coupon-validation-service`.
- Resiliencia parcial: se a gestao estiver fora, o checkout ainda pode validar cupons ja projetados.
- Consistencia eventual: um cupom recem-criado pode levar alguns instantes ate aparecer na validacao.
