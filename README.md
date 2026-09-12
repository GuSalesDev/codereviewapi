# Code Review Assistant — Documentação do Projeto

## O que o projeto faz

API REST em Java que recebe um ou mais arquivos de código Java e retorna
sugestões de code review automáticas, geradas por uma IA (LLM). A API analisa
os arquivos enviados e aponta bugs, riscos de segurança, problemas de
performance e violações de boas práticas — cada sugestão vem com linha
aproximada, categoria, severidade, descrição do problema e uma correção
sugerida.

Foi pensado como projeto de portfólio para demonstrar integração de um
backend Java tradicional com IA generativa de forma estruturada e
profissional (não é só "chamar um chat e devolver texto solto" — a resposta
é validada e estruturada em JSON tipado).

---

## Tecnologias utilizadas

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.1 (Spring Framework 7) |
| Build | Maven (com Maven Wrapper — `mvnw.cmd`) |
| Integração com IA | LangChain4j 1.20.0 |
| Provedor de LLM | OpenAI — modelo `gpt-5.6-luna` |
| Serialização JSON | Jackson (instanciado manualmente no service, ver seção de decisões técnicas) |
| Validação de request | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Servidor embutido | Tomcat (padrão do Spring Boot) |
| Testes | JUnit + MockMvc (`spring-boot-starter-test`) |

---

## Como a API funciona (fluxo passo a passo)

1. **Cliente envia um `POST /api/v1/reviews`** com um JSON contendo a
   linguagem, a lista de arquivos (nome + código) e, opcionalmente, um foco
   de análise (ex: só bugs, só segurança).

2. **Validação (`@Valid` + Jakarta Validation)** — o `ReviewController`
   valida que a lista de arquivos não está vazia, que cada arquivo tem nome
   e código preenchidos, e que o tamanho do código não excede o limite
   configurado. Se algo estiver inválido, retorna `400 Bad Request` com uma
   mensagem clara.

3. **Montagem do prompt (`ReviewPromptBuilder`)** — todos os arquivos
   enviados são combinados em um **único prompt**, junto com instruções
   explícitas pedindo que a IA responda apenas com um array JSON, seguindo
   um formato exato (categoria, severidade, título, descrição, correção).
   A decisão de mandar todos os arquivos numa chamada só (em vez de uma
   chamada por arquivo) foi tomada pensando em manter o contexto entre
   arquivos (ex: perceber que um Controller não trata uma exceção que o
   Service lança) e reduzir custo de tokens repetidos.

4. **Chamada ao LLM (`ChatModel.chat(prompt)`)** — o `ReviewServiceImpl`
   envia o prompt pro modelo OpenAI configurado (`gpt-5.6-luna`) através do
   LangChain4j.

5. **Parsing da resposta** — a resposta em texto do LLM é limpa (remove
   blocos de markdown tipo ```json, se vierem) e desserializada diretamente
   para a lista de DTOs `FileReviewResult`/`Suggestion` usando Jackson.

6. **Resposta ao cliente** — a API devolve `200 OK` com um JSON contendo
   `id`, `status`, `createdAt`, um resumo textual, e a lista de arquivos com
   suas respectivas sugestões.

7. **Tratamento de erros** — se a chamada ao LLM falhar (sem crédito, erro
   de rede, parâmetro inválido) ou se a resposta vier em formato
   inesperado, a exceção é capturada e convertida em `503 Service
   Unavailable` com uma mensagem padronizada, sem vazar detalhes internos
   pro cliente. Os erros completos são logados no console do servidor via
   SLF4J para depuração.

---

## Estrutura do projeto

```
codereviewapi/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/gustavo/codereviewapi/
    │   │   ├── CodereviewapiApplication.java   # classe principal (main)
    │   │   │
    │   │   ├── controller/
    │   │   │   └── ReviewController.java       # expõe POST /api/v1/reviews
    │   │   │
    │   │   ├── service/
    │   │   │   ├── ReviewService.java          # interface do serviço
    │   │   │   └── impl/
    │   │   │       ├── ReviewServiceImpl.java      # orquestra: prompt -> LLM -> parsing
    │   │   │       └── ReviewPromptBuilder.java    # monta o texto do prompt
    │   │   │
    │   │   ├── config/
    │   │   │   └── LlmConfig.java              # define o bean ChatModel (OpenAI)
    │   │   │
    │   │   ├── dto/
    │   │   │   ├── ReviewRequest.java          # request: language, files, focus
    │   │   │   ├── FileInput.java              # um arquivo do request
    │   │   │   ├── ReviewResponse.java         # response completo
    │   │   │   ├── FileReviewResult.java       # resultado de um arquivo
    │   │   │   ├── Suggestion.java             # uma sugestão individual
    │   │   │   ├── ErrorResponse.java          # formato padronizado de erro
    │   │   │   └── enums/
    │   │   │       ├── Category.java           # BUG, SECURITY, PERFORMANCE, BEST_PRACTICE, STYLE
    │   │   │       ├── Severity.java           # HIGH, MEDIUM, LOW
    │   │   │       └── ReviewStatus.java        # COMPLETED, FAILED
    │   │   │
    │   │   └── exception/
    │   │       ├── GlobalExceptionHandler.java  # @RestControllerAdvice central
    │   │       └── LlmUnavailableException.java # erro de comunicação com o LLM
    │   │
    │   └── resources/
    │       └── application.properties          # config (porta, chave OpenAI, modelo)
    │
    └── test/
        └── java/com/gustavo/codereviewapi/
            └── ReviewControllerTest.java        # testes de integração (MockMvc)
```

---

## Contrato da API

### `POST /api/v1/reviews`

**Request:**
```json
{
  "language": "java",
  "files": [
    { "fileName": "UserService.java", "code": "public class UserService { ... }" }
  ],
  "focus": ["bugs", "security"]
}
```
- `files` é obrigatório (1 a 10 arquivos por request)
- `language` e `focus` são opcionais

**Response (200):**
```json
{
  "id": "uuid",
  "status": "COMPLETED",
  "createdAt": "2026-09-12T01:00:42Z",
  "summary": "1 arquivo(s) analisado(s), 3 sugestão(ões) encontrada(s).",
  "files": [
    {
      "fileName": "UserService.java",
      "suggestions": [
        {
          "line": 1,
          "category": "BUG",
          "severity": "HIGH",
          "title": "Uso inseguro de Optional.get()",
          "description": "...",
          "suggestedFix": "..."
        }
      ]
    }
  ]
}
```

**Erros:**
- `400 Bad Request` — request inválido (arquivos vazios, campos faltando)
- `503 Service Unavailable` — falha na comunicação com o LLM ou resposta em formato inesperado

---

## Decisões técnicas importantes (e por quê)

- **Spring Boot 4 usa Jackson 3 por padrão, não Jackson 2.** Por isso o
  Spring não expõe mais automaticamente um bean `com.fasterxml.jackson.databind.ObjectMapper`
  (o Jackson clássico) — ele configura um `tools.jackson.databind.json.JsonMapper`
  (Jackson 3) no lugar. Como o `ReviewServiceImpl` precisa do Jackson 2
  clássico pra desserializar a resposta do LLM, o `ObjectMapper` é
  instanciado manualmente dentro do service (`new ObjectMapper()`) em vez
  de injetado via Spring.

- **O modelo `gpt-5.6-luna` não aceita customizar `temperature`.** Modelos
  de raciocínio mais recentes da OpenAI travam esse parâmetro no valor
  padrão (1). O `LlmConfig` não define `temperature` no builder por conta
  disso.

- **Uma chamada ao LLM com todos os arquivos juntos**, em vez de uma
  chamada por arquivo — prioriza contexto entre arquivos e reduz custo de
  tokens repetidos (as instruções do prompt não precisam ser reenviadas
  por arquivo).

- **`LlmUnavailableException` unifica dois tipos de falha** (erro de
  comunicação com o provedor e resposta em formato inesperado) sob o mesmo
  código de erro HTTP (503), mantendo o contrato de erro simples.

---

## Configuração necessária para rodar

`src/main/resources/application.properties`:
```properties
spring.application.name=codereviewapi
server.port=8080

openai.api-key=${OPENAI_API_KEY}
openai.model-name=gpt-5.6-luna
```

A chave da OpenAI **nunca fica hardcoded** — vem de uma variável de
ambiente. Antes de rodar:
```powershell
$env:OPENAI_API_KEY = "sk-sua-chave-aqui"
.\mvnw.cmd spring-boot:run
```

---

## Roadmap / próximos passos possíveis

- Adicionar Resilience4j (circuit breaker/retry) em torno da chamada ao LLM
- Persistir o histórico de reviews em PostgreSQL
- Autenticação via JWT (reaproveitando o padrão já usado no projeto do
  catálogo de livros)
- Endpoint `GET /api/v1/reviews/{id}` para recuperar reviews anteriores
- Rate limiting (ex: Bucket4j) para controlar custo de uso da API de IA
