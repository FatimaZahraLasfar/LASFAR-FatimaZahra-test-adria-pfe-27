# Bank Platform — Épreuve technique ADRIA / ENSET 

Plateforme bancaire micro-services : Spring Boot 3.5, Spring Cloud 2025.0, Spring AI 1.0 (MCP), Kafka, Angular.

## Modules

| Module | Port | Rôle |
|---|---|---|
| `config-service` | 8888 | Config Server, partage `bank.transaction.max-amount=1000` |
| `discovery-service` | 8761 | Eureka |
| `gateway-service` | 8080 | Spring Cloud Gateway (point d'entrée unique) |
| `account-service` | 8081 | Comptes — REST, GraphQL, MCP |
| `transaction-service` | 8082 | Transactions et virements — REST, GraphQL, MCP, OpenFeign + Resilience4J, producteur Kafka |
| `event-consumer-service` | 8083 | Consomme `bank.transactions.created` et journalise |
| `chatbot-service` | 8084 | Agent IA (Spring AI) utilisant les outils MCP des deux services |
| `bank-client` | 4200 | Client Angular |

Architecture et diagrammes : [docs/architecture.md](docs/architecture.md).

## Prérequis

JDK 21, Maven 3.9+, Node 20+, Docker (pour Kafka), et une clé d'API LLM compatible OpenAI
(OpenAI, Groq ou Ollama en local).

## Démarrage

```bash
# 1. Compilation
mvn clean package

# 2. Services, dans cet ordre (un terminal chacun)
java -jar config-service/target/config-service-1.0.0.jar
java -jar discovery-service/target/discovery-service-1.0.0.jar
java -jar account-service/target/account-service-1.0.0.jar
java -jar transaction-service/target/transaction-service-1.0.0.jar
java -jar event-consumer-service/target/event-consumer-service-1.0.0.jar
java -jar gateway-service/target/gateway-service-1.0.0.jar

# 3. Client Angular
cd bank-client && npm install && npm start      # http://localhost:4200
```


## Points d'accès utiles

- Gateway : `http://localhost:8080` (`/api/accounts`, `/api/transactions`, `/api/transfers`, `/api/chat`)
- GraphQL via la gateway : `/graphql/accounts` et `/graphql/transactions` ; GraphiQL direct : `http://localhost:8081/graphiql`, `http://localhost:8082/graphiql`
- Serveurs MCP (SSE) : `http://localhost:8081/sse`, `http://localhost:8082/sse`
- Eureka : `http://localhost:8761`
- État des circuits Resilience4J : `http://localhost:8082/actuator/circuitbreakers`

## Tests

- `tests.http` : 25 scénarios (nominal, règles métier, virement, GraphQL, panne, chatbot).
- `mvn test` : tests unitaires des règles métier de `transaction-service`.

