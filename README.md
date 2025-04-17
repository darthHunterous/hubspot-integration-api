# HubSpot Integration API

Esta aplicação é uma API Spring Boot para integração com o HubSpot, conforme proposto no case técnico. 
O objetivo principal é permitir:

- Autenticação via OAuth 2.0
- Criação de contatos no HubSpot
- Processamento de webhooks com validação de assinatura
- Segurança de acesso aos endpoints sensíveis
- Resiliência e testes automatizados

---

## Funcionalidades

### OAuth com HubSpot
- Fluxo completo de autorização (`authorization_code`)
- Armazenamento e renovação automática do `access_token` via `refresh_token`

### Criação de contatos
- Endpoint `POST /contacts`
- Recebe propriedades e associações no formato esperado pelo HubSpot
- Validação de campos obrigatórios

### Webhooks
- Endpoint `POST /webhook/contact`
- Valida a assinatura `X-HubSpot-Signature` (versão v1)
- Rejeita requisições com timestamp inválido (proteção contra replays)

### Segurança
- Proteção com token customizado no endpoint `/contacts`
- Filtro configurado por propriedade `contacts.auth.token`
- Swagger documentado e exige autenticação apenas onde necessário

### Resiliência
- Requisições ao HubSpot com retry inteligente
- Tratamento de rate limit (`HTTP 429`)
- Logs detalhados de resposta e headers de rate limit

### Testes
- Testes de integração para todos os controllers
- Testes unitários para serviços, filtros, utilitários e handlers
- Mocks de WebClient

---

## Variáveis de Ambiente

Configure as variáveis no ambiente ou no arquivo `.env`:

```env
HUBSPOT_CLIENT_ID=client-id-here
HUBSPOT_CLIENT_SECRET=client-secret-here
HUBSPOT_REDIRECT_URI=redirect-uri-here
CONTACTS_AUTH_TOKEN=contacts-auth-token-here
```

* `HUBSPOT_CLIENT_ID` e `HUBSPOT_CLIENT_SECRET` devem ser obtidos do painel de aplicação da API 
do próprio HubSpot
* `HUBSPOT_REDIRECT_URI` referente a como acessar o endpoint `auth/callback` da aplicação
  * Também precisa ser configurada no painel de configurações da aplicação criada no HubSpot
* `CONTACTS_AUTH_TOKEN` pode ser definido qualquer token que achar conveniente ou gerado de forma
mais robusta

---

## Compilação e Execução

* A melhor forma é através do Docker
* `docker-compose up` para compilar e executar
* `docker-compose down` para parar o container em execução
* Expõe a porta 8080
  * Localmente pode-se acessar com `http://localhost:8080`

---

## Exemplos de Uso

### 01 - Obter URL para autorização

* `GET /auth/url`

## 02 - Trocar código de acesso por Token

* `GET /auth/callback?code=abc123`

## 03 - Criar Contato

```json
POST /contacts
Authorization: Bearer {CONTACTS_AUTH_TOKEN}
Content-Type: application/json

{
  "properties": {
    "email": "example@email.com",
    "firstname": "FirstName",
    "lastname": "LastName"
  },
  "associations": [
    {
      "to": { "id": "32173437505" },
      "types": [
        {
          "associationCategory": "HUBSPOT_DEFINED",
          "associationTypeId": 1
        }
      ]
    }
  ]
}
```
* `32173437505` é um ID válido para um objeto do tipo `Company` criado para teste
* O payload acima pode ser passado sem o array de `Associations`, que é opcional
* Para testar uma nova inserção basta copiar o payload acima trocando o email, que serve como ID para
objeto `Contact`

### Swagger

* Para efeitos de documentação e praticidade nos testes, o Swagger pode ser acessado
  * `http://localhost:8080/swagger-ui/index.html`

## Testes

Os testes fornecem uma boa cobertura de código das principais funcionalidades. Há tanto testes unitários
quanto de integração
* Podem ser executados através de uma IDE como IntelliJ, ou através do Maven
  * `mvn test`

### Testar Webhook Localmente

* Foi utilizado a ferramenta `ngrok`
* https://ngrok.com/
* Após instalação seguindo instruções no site, basta executar
  * `ngrok http 8080`
* O ngrok irá gerar uma url pública que deve ser configurada no painel de Webhooks do HubSpot

## Arquitetura dos Pacotes

* `controller` - Endpoints REST
* `service` - Regras de negócio e OAuth
* `client` - Comunicação com a API do HubSpot
* `dto` - Representação dos dados
* `exception` - Tratamento centralizado para erros
* `util` - Utilitários
* `config` - Configurações em geral

## Decisões Técnicas

### OAuth com WebClient

* `WebClient` permite facilidade nas requisições
* Fluxo OAuth utilizando `access_token` e `refresh_token` como recomenda o HubSpot
* `refresh_token` atualizado automaticamente com `@Scheduled`

### Separação de responsabilidade

* Aplicando princípios de Clean Architecture e SOLID para definir uma responsabilidade clara para cada 
camada
  * `controller` para endpoints
  * `service` - regras de negócio e OAuth
  * `client` - comunicação com HubSpot
  * `token` - armazenamento dos tokens
  * `exception`, `config`, `util`, para métodos auxiliares de tratamento de erro, configuração
e utilitários

### Retry

* Falhando por erro 429 (rate limit), o endpoint tenta novamente algumas vezes, exibindo o log com os
headers de rate limit

### Testes automatizados

* Buscando maximizar a cobertura de testes
* Testes de integração usam `WebTestClient` e `@SpringBootTest`
* Testes unitários com `Mockito`, também mockando `WebClient` com `ExchangeFunction`

### Segurança

* Endpoint de criação de contatos protegido com token customizado que deve ser passado no header
`Authorization: Bearer`
* WebHook valida o header `X-HubSpot-Signature` para garantir autenticidade e também desconsidera
requests com mais de 5 minutos no timestamp para evitar ataques

### Swagger

* Disponível em `http://localhost:8080/swagger-ui/index.html`, para facilitar documentação e testagem
da API

### Variáveis de Ambiente

* Ocultam chaves secretas de API e são recebidas através do arquivo `.env`
* Há um arquivo `.env.example` para guiar a configuração do ambiente local
* O `.env` é injetado com a biblioteca `dotenv-java`

## Justificativas para Bibliotecas escolhidas

* `spring-boot-starter-web`: API REST em Spring
* `spring-boot-starter-webflux`: uso de `WebClient` para requisições HTTP ao HubSpot
* `spring-boot-starter-security`: protege endpoint sensíveis (criação de contato)
* `springdoc-openapi`: para documentar a API com Swagger
* `dotenv-java`: fácil configuração de variáveis de ambiente locais via `.env`
* `Retry`: facilita retry de requisições controlando os erros recebidos
* `jakarta-validation`: validações nos DTOs para garantir que os dados sejam transmitidos conforme
esperado
* `Mockito` e `JUnit`: para testar unitariamente com mocks

## Possíveis Melhorias Futuras

* Persistência de tokens
* Modularização
* Adição de mais endpoints do HubSpot