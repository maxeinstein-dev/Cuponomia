# 📝 Bastidores do Desenvolvimento: Como o Cuponomia nasceu

E aí! Queria bater um papo mais direto sobre como eu desenhei e construí esse sistema. Se você tá lendo isso, provavelmente quer saber não só *o que* eu fiz, mas *como* eu pensei. Vamos nessa?

---

## 🧭 Minha Filosofia: O Domínio vem primeiro, o Framework a gente vê depois

Sabe aquela tentação de abrir o Spring Initializr, gerar o projeto e já sair criando os Controllers e as tabelas do banco? Confesso que é forte, mas eu resisti. 

Para esse desafio, eu fiz o caminho inverso: **foquei primeiro no coração da aplicação (o domínio)**, testei as regras, e só quando tudo fazia sentido eu fui plugar o Spring e o banco de dados.

> A ideia foi: se amanhã alguém quiser trocar o H2 por um PostgreSQL (spoiler: fizemos isso no Docker!) ou mudar o framework, a regra de negócio não quebra.

---

## 🏗️ O Passo a Passo

### Fase 1: Arrumando a casa (Setup)
Antes de escrever código, organizei os `Profiles` do Spring (`dev` pro dia a dia com H2, `test` pra testes rodarem lisos, e `docker` pra rodar com um banco de verdade). Também troquei o `.properties` clássico pelo `.yml` que é bem mais limpo de ler. Pode parecer detalhe, mas ter a casa arrumada desde o dia 0 evita dores de cabeça lá na frente.

### Fase 2: O Domínio (DDD na prática)
Aqui foi onde a mágica começou. Sem pensar em banco de dados:
- Criei os **Value Objects** (`CouponCode`, `ValidationResult`). O `CouponCode`, por exemplo, não deixa ninguém criar um cupom com caracteres bizarros ou espaços.
- Criei a **Entidade** `Coupon`. E ela não é só um "saco de getters e setters". É ela quem calcula o desconto (`applyDiscount()`) e valida as regras. A inteligência fica onde os dados estão.
- As **Interfaces de Repositório (Ports)** nasceram aqui. O domínio sabe que precisa salvar um cupom, mas não faz ideia se é no H2, Postgres ou num arquivo txt. 

### Fase 3: As Regras de Negócio (Specification Pattern)
Ao invés de encher a entidade de `if / else`, eu criei uma interface `CouponRule` e uma classe pra cada regra (Valor Mínimo, Data de Expiração, Uso Único, etc).
- **Por que isso é legal?** Se amanhã o time de marketing quiser uma regra "Válido só às terças", eu crio uma classe nova e não preciso alterar **nenhuma** linha de código antiga (um abraço pro Open/Closed Principle do SOLID!).

### Fase 4: Casos de Uso e DTOs
Criei as classes de Use Case (`ApplyCouponUseCase`, `CreateCouponUseCase`). Elas agem como maestras da orquestra: buscam dados, mandam o domínio trabalhar e salvam o resultado. 
Também criei os DTOs usando os `Records` do Java pra garantir que ninguém altere os dados no meio do caminho.

### Fase 5: Agora sim, o Banco de Dados (JPA)
Aí o Spring Data JPA entrou em cena. Separei as entidades do banco (`CouponEntity`) das do domínio (`Coupon`). 
- **O detalhe de ouro:** Coloquei um `@Version` na entidade pra habilitar o *Optimistic Locking*. Se dois clientes tentarem usar o mesmo cupom na exata mesma fração de segundo, um deles vai ser rejeitado (Erro 409 Conflict) ao invés de corromper os dados.

### Fase 6: Expondo pro Mundo (API REST)
Os Controllers ficaram bem enxutos. Eles só recebem o JSON, jogam pro Use Case e respondem.
Quem faz o trabalho sujo de converter as exceções em códigos HTTP (400, 404, 409, 422) é o meu querido `GlobalExceptionHandler`. 

### Fase 7: Segurança 
Temos um cinto de segurança duplo:
1. A aplicação valida se o cliente já usou o cupom na memória.
2. O banco de dados tem uma constraint `UNIQUE` que impede fisicamente que a combinação (cliente + cupom) se repita. 

### Fase 8: Bateria de Testes
Bati a marca de **70 testes!** Cobrei desde as regras pequenininhas (unitários com Mockito) até o fluxo completo (integração batendo na API REST real). Não tem aquela insegurança de "será que quebrou alguma coisa?". 

### Fase 9: Partiu Produção (Docker)
Deixei um Dockerfile caprichado, usando multi-stage build pra não pesar a imagem. Além disso, no `docker-compose.yml`, subi um **PostgreSQL** de verdade pra simular o ambiente de produção. 

### Fase 10: O Laço de Fita (Documentação)
Um projeto sem documentação e sem dados pra testar é triste.
Gerei um README completo, um Swagger com dados preenchidos e um `data.sql` que já sobe 7 cupons pra galera brincar logo de cara.

---

## 🎯 Reflexão
A arquitetura não deve atrapalhar, deve dar asas pra gente focar no que importa: **resolver o problema do negócio.**
