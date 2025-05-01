
# Estudos com Rest Assured

Este repositório foi criado como um estudo voltado para o desenvolvimento de **POCs (Provas de Conceito)** utilizando a dependência **Rest Assured**, muito utilizada para testes de integração em APIs REST. A aplicação de exemplo consiste em uma API de loja com funcionalidades de produtos e pedidos, desenvolvida em **Spring Boot** com banco de dados em memória **H2**.

## Sobre a dependência

[Rest Assured](https://rest-assured.io) é uma dependência do Java que facilita a escrita de testes para serviços REST. Ela permite realizar requisições HTTP e fazer validações.

Com ele, é possível:
- Realizar chamadas HTTP com `GET`, `POST`, `PUT`, `DELETE`, etc.
- Verificar status de resposta.
- Validar corpo da resposta com matchers (por exemplo: `equalTo`, `hasItems`).
- Fazer autenticação (Básica, OAuth2, etc).
- Filtrar resultados usando expressões Groovy diretamente no JSON.

Imports essenciais para usar a dependência:
```java
import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
```

## Json-simple

Além do Rest Assured, foi utilizada a dependência `json-simple` para facilitar a criação de JSONs a partir de `Map<String, Object>`.

## Configuração Básica

No método `setUp()`, configure o endereço da API:

```java
@BeforeAll
public void setUp() {
    baseURI = "http://localhost:8080"; // Lembrar de trocar a porta dos testes se estiver rodandno a aplicação na 8080 e em outro projeto.
}
```

## Principais métodos do Rest Assured utilizados

A seguir estão alguns dos métodos mais utilizados no Rest Assured com exemplos práticos:

- `given().get("/endpoint")`: Realiza uma requisição GET ao endpoint especificado.
- `.then().statusCode(200)`: Verifica se o código de status da resposta é 200 (ou outro esperado).
- `.body("id", is(1))`: Verifica se o campo `id` do corpo da resposta tem o valor `1`. Pode-se usar `equalTo("valor")` para valores string.
- `.body("categories.id", hasItems(2, 3))`: Valida que a lista `categories.id` contém os itens 2 e 3.
- `.body("content.id[1]", is(6))`: Acessa o índice 1 da lista `content.id` e valida se o valor é 6.

Além disso, é possível usar `when()` após `given()` para tornar o código mais legível, por exemplo:

```java
given()
    .when()
    .get("/products")
    .then()
    .statusCode(200);
```

O uso de `when()` é opcional, mas pode ajudar a organizar melhor os blocos de requisição e resposta.


## Exemplos de Uso

### Filtragem na própria validação
```java
.body("content.findAll { it.price > 2000 }.name", hasItems("PC Gamer Boo", "PC Gamer Foo"));
```

> Observação: se o tipo do campo usado no filtro não for inteiro (por exemplo, `float`), você pode fazer a conversão diretamente na expressão Groovy:
```java
.body("content.findAll { it.price.toFloat() > 2000.0 }.name", hasItems("Produto X"));
```

### Requisição com Autenticação e Token OAuth2
Para testes de endpoints protegidos, foi necessário obter um token de acesso via OAuth2:

```java
public static Response getAccessToken(String username, String password) {
    return given()
        .auth().preemptive().basic("myclientid", "myclientsecret")
        .contentType("application/x-www-form-urlencoded")
        .formParam("grant_type", "password")
        .formParam("username", username)
        .formParam("password", password)
    .when()
        .post("/oauth2/token");
}
```

Explicações:
- `auth()` → configura autenticação.
- `preemptive()` → envia o cabeçalho Authorization imediatamente, sem esperar desafio 401.
- `basic("id", "secret")` → autenticação básica com clientId e clientSecret.
- `formParam()` → parâmetros da requisição.

Para extrair o token da `Response`:
```java
String token = response.jsonPath().getString("access_token");
```

Esse token deve ser incluído nos headers das requisições protegidas:

```java
given()
    .header("Authorization", "Bearer " + token)
    .post("/products")
.then()
    .statusCode(201);
```

---

## Testes Realizados

### Problema 1: Consultar Produto por Nome

- Teste GET em `/products?name=Macbook`
- Valida se `id`, `name`, `price` e `imgUrl` retornam corretamente.
- Esperado: status 200.

---

### Problema 2: Inserir Produto (POST)

**Cenários:**
- Insere com sucesso quando logado como admin.
- Retorna 422 com mensagens customizadas para:
  - Nome inválido
  - Descrição inválida
  - Preço negativo
  - Preço zero
  - Sem categoria
- Retorna 403 quando logado como cliente.
- Retorna 401 quando não autenticado.

---

### Problema 3: Deletar Produto (DELETE)

**Cenários:**
- Deleta com sucesso (admin).
- Retorna 404 para produto inexistente.
- Retorna 400 para produto com dependências.
- Retorna 403 (cliente).
- Retorna 401 (sem autenticação).

---

### Problema 4: Consultar Pedido por ID (GET)

**Cenários:**
- Retorna pedido (admin).
- Retorna pedido do próprio usuário (cliente).
- Retorna 403 se pedido não pertence ao cliente.
- Retorna 404 para pedido inexistente.
- Retorna 401 (não autenticado).

---

## Conclusão

Este projeto apresenta um exemplo simples e funcional de como utilizar o Rest Assured para testes de endpoints REST em uma API Spring Boot. A abordagem cobre cenários comuns como autenticação, filtros, validações de campos e tratamento de erros.

Sinta-se à vontade para clonar o repositório e rodar os testes!
