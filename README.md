# 🏦 Banking Platform — ADRIA & ENSET

### Architecture Microservices • Intelligence Artificielle • Développement Full-Stack

**Projet réalisé dans le cadre de l'épreuve technique ADRIA & ENSET — Master SDIA**

---

## 📌 Présentation du projet

Banking Platform est une plateforme bancaire intelligente conçue autour d'une architecture distribuée basée sur les microservices.

L'objectif est de développer une application permettant de gérer les comptes bancaires, d'enregistrer et de consulter les transactions, de réaliser des virements et de publier des événements métier.

La plateforme intègre également un assistant conversationnel intelligent permettant aux utilisateurs d'interagir avec les services bancaires en langage naturel, grâce à un LLM et au protocole Model Context Protocol (MCP).

Le projet repose sur Spring Boot, Spring Cloud, Apache Kafka, Spring AI et Angular.

## 🎯 Objectifs fonctionnels

- Gestion et consultation des comptes bancaires.
- Enregistrement et consultation des transactions.
- Mise à jour automatique des soldes.
- Réalisation de virements entre comptes.
- Validation des opérations selon les règles métier.
- Communication entre microservices.
- Publication et consommation d'événements avec Kafka.
- Assistant conversationnel utilisant des outils MCP.
- Interface web Angular pour les opérations bancaires.

## 🏗️ Architecture du système

L'application adopte une architecture microservices dans laquelle chaque service possède une responsabilité spécifique.

### Architecture cible

| Service | Responsabilité | Port |
|---|---|---:|
| `config-service` | Configuration centralisée | 8888 |
| `discovery-service` | Découverte des services avec Eureka | 8761 |
| `gateway-service` | Routage des requêtes | 8080 |
| `account-service` | Gestion des comptes bancaires | 8081 |
| `transaction-service` | Transactions et virements | 8082 |
| `event-consumer-service` | Consommation des événements Kafka | 8083 |
| `chatbot-service` | Assistant conversationnel IA | 8084 |
| `bank-client` | Interface web Angular | 4200 |

*Les ports correspondent à la configuration cible du projet ; ils doivent être vérifiés avec les fichiers de configuration effectifs.*

### Schéma des communications

```text
                 Angular Client
                       |
                       v
               Spring Cloud Gateway
                       |
              +--------+--------+
              |                 |
              v                 v
        Account Service   Transaction Service
              |                 |
              v                 v
           H2 DB              H2 DB
                                |
                                v
                          Kafka Producer
                                |
                                v
                     bank.transactions.created
                                |
                                v
                       Kafka Consumer

              AI Chatbot / Spring AI
                       |
                       v
                   MCP Client
                  /          \
                 v            v
          Account MCP    Transaction MCP

       Eureka Discovery + Config Server
             Services transverses
```

## 🛠️ Technologies utilisées

### Backend

- Java 17
- Spring Boot
- Spring Cloud
- Spring Data JPA
- Spring Web MVC
- Spring GraphQL
- Spring Cloud OpenFeign
- Resilience4J

### Base de données

- H2 Database
- Hibernate / JPA

### Intelligence artificielle

- Spring AI
- Large Language Models (LLM)
- Model Context Protocol (MCP)

### Communication asynchrone

- Apache Kafka
- Kafka Producer / Consumer

### Frontend

- Angular
- TypeScript
- HTML5 / CSS3

### Outils de développement

- IntelliJ IDEA
- Maven
- Git & GitHub
- Docker (pour l'environnement Kafka si nécessaire)

## 📂 Organisation du projet

```text
banking-platform/
│
├── pom.xml
│
├── account-service/
│   ├── src/
│   └── pom.xml
│
├── transaction-service/
│
├── gateway-service/
│
├── discovery-service/
│
├── config-service/
│
├── event-consumer-service/
│
├── chatbot-service/
│
├── bank-client/
│
├── docs/
│   ├── architecture/
│   ├── screenshots/
│   └── report/
│
├── .gitignore
└── README.md
```

*Cette arborescence représente l'organisation cible. Les dossiers seront renseignés au fur et à mesure de l'implémentation.*

## 💳 Règles métier

Chaque transaction doit respecter les règles suivantes :

1. Le montant doit être strictement positif.
2. Le montant doit respecter le plafond configuré.
3. Le compte doit exister et être actif.
4. La devise de la transaction doit correspondre à celle du compte.
5. Le solde doit être vérifié avant l'enregistrement.
6. Le solde du compte doit être mis à jour après l'enregistrement réussi.
7. Les virements doivent prendre en compte le compte source et le compte destinataire.

Paramètre centralisé :

```properties
bank.transaction.max-amount=1000
```

## 🔄 Communication entre microservices

La communication synchrone est assurée par OpenFeign.

Resilience4J est utilisé pour améliorer la tolérance aux pannes à travers :

- Circuit Breaker
- Retry
- Timeout
- Fallback contrôlé

Les interfaces REST, GraphQL et MCP utilisent la même couche service afin d'éviter la duplication des règles métier.

## 🤖 Assistant conversationnel

L'assistant repose sur Spring AI et un modèle de langage.

Il utilise les outils MCP exposés par les microservices pour effectuer des opérations telles que :

- Consulter les informations d'un compte.
- Vérifier un solde.
- Consulter les transactions.
- Initier une opération bancaire autorisée.

Les opérations sensibles nécessitent une validation et une confirmation appropriées.

## 📡 Communication asynchrone avec Kafka

Après la persistance réussie d'une transaction, un événement est publié dans :

```text
bank.transactions.created
```

La clé du message est `accountId`.

Le consumer reçoit l'événement et le journalise sans modifier les données bancaires.

## 🧪 Tests et validation

Les tests couvrent progressivement les fonctionnalités suivantes :

- Tests unitaires des règles métier.
- Tests des API REST.
- Tests des requêtes GraphQL.
- Tests des outils MCP.
- Tests de communication OpenFeign.
- Tests de tolérance aux pannes avec Resilience4J.
- Tests de publication et de consommation Kafka.
- Tests fonctionnels de l'interface Angular.
- Tests d'interaction avec l'assistant conversationnel.

Les résultats détaillés et les captures d'écran sont disponibles dans le rapport technique.

## 🚀 Installation et exécution

### Prérequis

- JDK 17
- Maven
- IntelliJ IDEA
- Node.js et npm pour Angular
- Docker pour l'environnement Kafka, selon la configuration retenue

### Backend

Depuis le dossier d'un microservice :

```bash
cd account-service
mvn clean install
mvn spring-boot:run
```

### Frontend Angular

Depuis le dossier du client Angular :

```bash
cd bank-client
npm install
npm start
```

Les instructions de démarrage global seront complétées lorsque l'ensemble des services et leurs configurations seront disponibles.

## 👩‍💻 Auteur

**Fatima Zahra Lasfar**

Master : Systèmes Distribués et Intelligence Artificielle (SDIA)

**Établissement : ENSET**

**Épreuve technique : ADRIA & ENSET — 2027**

---

*Ce projet est réalisé dans un cadre académique et expérimental. Il vise à mettre en pratique les concepts d'architecture microservices, de systèmes distribués, d'intelligence artificielle et de développement full-stack.*
