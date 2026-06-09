# Stock Portfolio Alerts

A real-time stock portfolio management and alerting system built with Spring Boot. Users can manage their investment portfolios, set price-threshold alerts on individual stocks, and receive automated notifications when prices break through configured thresholds — driven by a dual-messaging architecture using Apache Kafka and RabbitMQ.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Data Flow](#data-flow)
- [API Reference](#api-reference)
    - [Authentication](#authentication)
    - [User Registration](#user-registration)
    - [Portfolio Management](#portfolio-management)
    - [Alert Management](#alert-management)
    - [Health & Monitoring](#health--monitoring)
- [Key Design Decisions](#key-design-decisions)
- [Prerequisites](#prerequisites)
- [Environment Variables](#environment-variables)
- [Running the Application](#running-the-application)
- [Running with Docker](#running-with-docker)
- [Configuration Reference](#configuration-reference)
- [Testing](#testing)

---

## Architecture Overview

```
                         ┌───────────────────────────────────────────────────┐
                         │              Stock Portfolio Alerts API           │
                         │                  (Spring Boot 3.3.5)              │
                         │                                                   │
  Client── HTTP──► ┌─────┤  SecurityFilterChain (JWT)                        │
                   │     │                                                   │
                   │     │  ┌──────────────┐  ┌──────────────┐               │
                   │     │  │  Auth Module │  │ User Module  │               │
                   │     │  │  /api/auth   │  │ /api/v1/users│               │
                   │     │  └──────────────┘  └──────────────┘               │
                   │     │                                                   │
                   │     │  ┌──────────────────────┐  ┌────────────────┐     │
                   │     │  │  Portfolio Module    │  │  Alert Module  │     │
                   │     │  │  /api/portfolios     │  │  /api/alerts   │     │
                   │     │  └──────────────────────┘  └────────────────┘     │
                   │     │           │                         │             │
                   │     └───────────┼─────────────────────────┼─────────────┘
                   │                 │                         │
                   │         ┌───────▼──────┐         ┌───────▼───────────────┐
                   │         │  AlphaVantage│         │  Kafka Listener       │
                   │         │  Stock API   │         │  (stock-price topic)  │
                   │         └──────────────┘         └───────────────────────┘
                   │         (cached 30s via                  │
                   │          Caffeine)              price > threshold?
                   │                                          │
                   │                                 ┌────────▼─────────────┐
                   │                                 │  RabbitMQ Publisher  │
                   │                                 │  (stock-alert-queue) │
                   │                                 └──────────────────────┘
                   │                                          │
                   │                                 ┌────────▼──────────────┐
                   │                                 │  AlertEventConsumer   │
                   │                                 │  → NotificationService│
                   │                                 └───────────────────────┘
                   │
                   └─────────────────────────────────────────────►  MySQL DB
```

### Core Modules

| Module | Responsibility |
|---|---|
| `auth` | JWT-based user registration and login |
| `portfolio` | CRUD for portfolios and holdings, CSV upload, live stock price integration |
| `alert` | Price-threshold alert creation and retrieval |
| `messaging.kafka` | Consumes real-time stock price events from a Kafka topic |
| `messaging.rabbitmq` | Publishes triggered alerts to a RabbitMQ queue; consumer dispatches notifications |
| `stock` | External Alpha Vantage API client with Caffeine caching |
| `audit` | AOP-based audit logging for sensitive portfolio operations |
| `config` | Security, caching, and actuator configuration |
| `exception` | Global error handling with consistent API error responses |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security + JWT (JJWT 0.11.5) |
| Persistence | Spring Data JPA + Hibernate + MySQL |
| Caching | Caffeine (30s TTL, max 100 entries) |
| Messaging (inbound) | Apache Kafka (`stock-price` topic) |
| Messaging (outbound) | RabbitMQ (`stock-alert-queue`) |
| External API | Alpha Vantage (Global Quote endpoint) |
| HATEOAS | Spring HATEOAS |
| AOP | Spring AOP |
| Monitoring | Spring Boot Actuator |
| Build | Maven |
| Boilerplate | Lombok |

---

## Project Structure

```
src/main/java/com/bl/stockportfolioalerts/
│
├── StockportfolioalertsApplication.java   # Entry point, @EnableCaching
│
├── alert/
│   ├── controller/AlertController.java    # POST /api/alerts, GET /api/alerts/{id}
│   ├── dto/CreateAlertRequest.java
│   ├── entity/Alert.java                  # ticker, thresholdPrice, User FK
│   ├── repository/AlertRepository.java    # findByTicker(String)
│   └── service/AlertService.java          # Creates alerts, resolves caller from JWT
│
├── auth/
│   ├── controller/
│   │   ├── AuthController.java            # POST /api/auth/login
│   │   └── UserController.java            # POST /api/v1/users/register
│   ├── dto/                               # LoginRequest, RegisterRequest, JwtResponse
│   ├── entity/User.java
│   ├── repository/UserRepository.java     # findByEmail(String)
│   ├── security/
│   │   ├── JwtFilter.java                 # GenericFilter, validates Bearer token
│   │   ├── JwtUtil.java                   # Generates / validates / extracts HS256 JWTs
│   │   └── CustomUserDetailsService.java  # Loads user by email for Spring Security
│   ├── service/UserService.java           # Registers user, BCrypt password encoding
│   └── validation/UserValidation.java     # Functional predicates for email/password
│
├── audit/
│   └── PortfolioAuditAspect.java          # @Before/@AfterReturning/@AfterThrowing on deletePortfolio
│
├── config/
│   ├── CacheConfig.java                   # Caffeine cache manager (stockPrices, 30s TTL)
│   ├── CustomHealthIndicator.java         # Custom /actuator/health detail
│   └── SecurityConfig.java                # CSRF off, JWT filter, permit /register + /login
│
├── exception/
│   ├── ApiErrorResponse.java              # timestamp, status, error
│   └── GlobalExceptionHandler.java        # @ControllerAdvice for Runtime + generic Exception
│
├── messaging/
│   ├── kafka/
│   │   ├── KafkaConfig.java               # Declares `stock-price` topic (1 partition, 1 replica)
│   │   ├── KafkaHealthConfig.java         # Actuator health indicator for Kafka
│   │   ├── StockPriceEvent.java           # { ticker, price }
│   │   └── StockPriceListener.java        # @KafkaListener: matches events to alerts, publishes to RabbitMQ
│   └── rabbitmq/
│       ├── RabbitMQConfig.java            # Declares durable queue, Jackson converter
│       ├── AlertEvent.java                # { alertId, ticker, thresholdPrice, userId }
│       ├── AlertEventPublisher.java       # Converts and sends to stock-alert-queue
│       └── AlertEventConsumer.java        # @RabbitListener: receives and calls NotificationService
│
├── notification/
│   └── NotificationService.java           # Log-based notification stub (email/SMS ready to extend)
│
├── portfolio/
│   ├── controller/PortfolioController.java
│   ├── dto/                               # Create/Update/Add requests and responses
│   ├── entity/
│   │   ├── Portfolio.java                 # totalValue, @Version (optimistic locking), User FK
│   │   └── Holding.java                  # ticker, quantity, buyingPrice, Portfolio FK
│   ├── repository/
│   │   ├── PortfolioRepository.java       # findByUserId(Long)
│   │   └── HoldingRepository.java        # deleteByPortfolio_Id(Long)
│   ├── service/PortfolioService.java      # Business logic, auth ownership checks
│   └── util/CsvParser.java               # Parses ticker,quantity,buyingPrice CSV
│
└── stock/
    ├── client/StockPriceClient.java       # Alpha Vantage HTTP client (RestTemplate)
    └── service/StockPriceService.java     # @Cacheable wrapper around StockPriceClient
```

---

## Data Flow

### Alert Triggering Pipeline

```
External Producer
      │
      ▼
Kafka Topic: stock-price
  payload: { ticker: "AAPL", price: 195.50 }
      │
      ▼
StockPriceListener.consumeStockPrice()
  → queries AlertRepository.findByTicker("AAPL")
  → filters alerts where event.price > alert.thresholdPrice
  → for each match:
        AlertEventPublisher.publishAlertEvent(alertEvent)
              │
              ▼
        RabbitMQ Queue: stock-alert-queue
              │
              ▼
        AlertEventConsumer.consumeAlertEvent()
              │
              ▼
        NotificationService.sendNotification(message)
              │
              ▼
        [Log output — extend to send email/SMS/push]
```

### Buy-a-Stock Flow (live pricing)

```
POST /api/portfolios/{id}/stocks  { ticker: "TSLA", quantity: 10 }
      │
      ▼
PortfolioController.addStock()
      │
      ▼
StockPriceService.getStockPrice("TSLA")   ← checks Caffeine cache first (30s TTL)
      │  cache miss
      ▼
StockPriceClient.fetchStockPrice("TSLA")  ← Alpha Vantage API
      │
      ▼
Holding created at live price, portfolio holdings sorted by value (desc)
```

---

## API Reference

All endpoints except `/api/v1/users/register` and `/api/auth/login` require:

```
Authorization: Bearer <jwt_token>
```

---

### Authentication

#### Login

```
POST /api/auth/login
```

Authenticates a registered user and returns a JWT valid for **1 hour**.

**Request body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass1"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Error `400 Bad Request`:** Invalid credentials.

---

### User Registration

#### Register a New User

```
POST /api/v1/users/register
```

Creates a new user account. Password is BCrypt-encoded before storage.

**Validation rules:**
- Email must contain `@`
- Password must be at least 8 characters

**Request body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass1"
}
```

**Response `201 Created`:**
```
User registered successfully
```

**Error `400 Bad Request`:** Invalid email, weak password, or email already registered.

---

### Portfolio Management

All portfolio endpoints require authentication. Ownership is enforced — users can only access their own portfolios.

---

#### Create Portfolio (JSON)

```
POST /api/portfolios
```

Creates a new portfolio with one or more holdings. `totalValue` is computed as `sum(quantity × buyingPrice)`.

**Request body:**
```json
{
  "holdings": [
    { "ticker": "AAPL", "quantity": 10, "buyingPrice": 180.00 },
    { "ticker": "MSFT", "quantity": 5,  "buyingPrice": 420.00 }
  ]
}
```

**Response `200 OK`:**
```json
{
  "portfolioId": 1,
  "totalValue": 3900.00,
  "holdings": [
    { "ticker": "AAPL", "quantity": 10, "buyingPrice": 180.00, "value": 1800.00 },
    { "ticker": "MSFT", "quantity": 5,  "buyingPrice": 420.00, "value": 2100.00 }
  ]
}
```

---

#### Create Portfolio (CSV Upload)

```
POST /api/portfolios/upload
Content-Type: multipart/form-data
```

Accepts a `.csv` file where each row is `ticker,quantity,buyingPrice`. Creates a portfolio from parsed holdings.

**CSV format:**
```
AAPL,10,180.00
MSFT,5,420.00
GOOGL,3,175.50
```

**Form parameter:** `file` (MultipartFile)

**Response `200 OK`:** Same as JSON creation response.

**Error `400 Bad Request`:** Malformed CSV (invalid column count or non-numeric values).

---

#### Get Portfolio by ID

```
GET /api/portfolios/{id}
```

Returns a portfolio with HATEOAS hypermedia links. Only holdings with `quantity > 0` are included.

**Response `200 OK`:**
```json
{
  "portfolioId": 1,
  "totalValue": 3900.00,
  "holdings": [
    { "ticker": "AAPL", "quantity": 10, "buyingPrice": 180.00, "value": 1800.00 }
  ],
  "_links": {
    "self":   { "href": "http://localhost:8080/api/portfolios/1" },
    "create": { "href": "http://localhost:8080/api/portfolios" }
  }
}
```

**Error `400 Bad Request`:** Portfolio not found or unauthorized access.

---

#### Get All Portfolios (for Authenticated User)

```
GET /api/portfolios/showall
```

Returns all portfolios belonging to the currently authenticated user.

**Response `200 OK`:**
```json
[
  {
    "portfolioId": 1,
    "totalValue": 3900.00,
    "holdings": [ "..." ]
  },
  {
    "portfolioId": 2,
    "totalValue": 1200.00,
    "holdings": [ "..." ]
  }
]
```

---

#### Update Portfolio

```
PUT /api/portfolios/{id}
```

Fully replaces all holdings of a portfolio. Existing holdings are deleted and replaced with the new set. `totalValue` is recalculated. Response includes HATEOAS self-link.

**Request body:**
```json
{
  "holdings": [
    { "ticker": "NVDA", "quantity": 8, "buyingPrice": 900.00 }
  ]
}
```

**Response `200 OK`:**
```json
{
  "portfolioId": 1,
  "totalValue": 7200.00,
  "holdings": [
    { "ticker": "NVDA", "quantity": 8, "buyingPrice": 900.00, "value": 7200.00 }
  ],
  "_links": {
    "self": { "href": "http://localhost:8080/api/portfolios/1" }
  }
}
```

---

#### Delete Portfolio

```
DELETE /api/portfolios/{id}
```

Permanently deletes a portfolio and its holdings. Ownership is verified. This operation is audit-logged via AOP (`PortfolioAuditAspect`) — the attempt, success, and any failure are all captured in application logs.

**Response `204 No Content`**

**Error `400 Bad Request`:** Portfolio not found or unauthorized access.

---

#### Add a Stock to Portfolio (Live Price)

```
POST /api/portfolios/{id}/stocks
```

Adds a new holding to an existing portfolio using the **live market price** fetched from Alpha Vantage (cached for 30 seconds per ticker). Holdings are re-sorted by value in descending order after insertion.

**Request body:**
```json
{
  "ticker": "TSLA",
  "quantity": 5
}
```

**Response `200 OK`:** Updated `PortfolioResponse` (same shape as GET).

**Error `400 Bad Request`:** Portfolio not found, unauthorized access, or Alpha Vantage API error/rate-limit.

---

### Alert Management

#### Create Alert

```
POST /api/alerts
```

Creates a price-threshold alert for the authenticated user. When a Kafka `stock-price` event arrives for the configured ticker with a price **above** the threshold, a notification is triggered.

**Request body:**
```json
{
  "ticker": "AAPL",
  "thresholdPrice": 200.00
}
```

**Response `200 OK`:**
```json
{
  "id": 1,
  "ticker": "AAPL",
  "thresholdPrice": 200.00,
  "user": {
    "id": 3,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

---

#### Get Alert by ID

```
GET /api/alerts/{id}
```

Retrieves a specific alert by its ID.

**Response `200 OK`:** Alert object (same shape as create response).

**Error `400 Bad Request`:** Alert not found.

---

### Health & Monitoring

Spring Boot Actuator is enabled with custom indicators.

#### Health Check

```
GET /actuator/health
```

**Response `200 OK`:**
```json
{
  "status": "UP",
  "components": {
    "application": {
      "status": "UP",
      "details": {
        "application": "Stock Portfolio Alerts Running"
      }
    },
    "kafka": {
      "status": "UP",
      "details": {
        "kafka": "Available"
      }
    },
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

#### Application Info

```
GET /actuator/info
```

---

## Key Design Decisions

### Dual Messaging Architecture
Kafka handles the high-throughput, real-time stock price stream (many prices arriving rapidly). RabbitMQ handles alert notifications — a separate, durable, acknowledgeable queue for reliable one-time delivery. This separation keeps price ingestion decoupled from user notification.

### Optimistic Locking on Portfolio
`Portfolio` uses `@Version` for optimistic locking. Concurrent update attempts (e.g., two requests adding stocks simultaneously) will fail fast rather than silently corrupt data.

### HATEOAS on Portfolio Endpoints
`GET /api/portfolios/{id}` and `PUT /api/portfolios/{id}` return hypermedia links (self, create), making the API self-describing for clients that follow the HATEOAS constraint.

### Caffeine Cache for Stock Prices
Alpha Vantage rate-limits free-tier keys aggressively. The Caffeine cache layer with a 30-second TTL and 100-entry cap prevents redundant external calls when multiple users hold the same ticker, and protects against API rate-limit errors.

### AOP Audit Logging
Rather than scattering audit log calls across service code, `PortfolioAuditAspect` uses Spring AOP pointcuts to intercept `deletePortfolio` and emit structured log entries at the attempt, success, and failure stages — without modifying business logic.

### Security Architecture
The JWT signing key is generated fresh at application startup (in-memory). Tokens expire after 1 hour. This means tokens are invalidated on restart, which is acceptable for a stateless service. For production, externalize the key to a secret manager (AWS Secrets Manager, HashiCorp Vault, etc.) and set `spring.jpa.hibernate.ddl-auto=validate` or `update`.

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java | 21 |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Apache Kafka | 3.x |
| RabbitMQ | 3.x |
| Alpha Vantage API Key | Free tier sufficient for dev |

---

## Environment Variables

| Variable | Description | Required |
|---|---|---|
| `ALPHA_VANTAGE_API_KEY` | API key from [alphavantage.co](https://www.alphavantage.co/support/#api-key) | Yes |

---

## Running the Application

**1. Clone the repository**
```bash
git clone <repo-url>
cd stockportfolioalerts
```

**2. Set up MySQL**
```sql
CREATE DATABASE stock_portfolio_alerts_db;
```

**3. Start Kafka and RabbitMQ**

Kafka (with Zookeeper):
```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka broker
bin/kafka-server-start.sh config/server.properties
```

RabbitMQ:
```bash
rabbitmq-server
```

**4. Export environment variables**
```bash
export ALPHA_VANTAGE_API_KEY=your_key_here
```

**5. Build and run**
```bash
./mvnw clean install
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`.

---

## Running with Docker

A `Dockerfile` is included. Build and run:

```bash
docker build -t stockportfolioalerts .

docker run -p 8080:8080 \
  -e ALPHA_VANTAGE_API_KEY=your_key_here \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/stock_portfolio_alerts_db \
  stockportfolioalerts
```

> Kafka and RabbitMQ addresses are currently hardcoded to `localhost:9092` and default RabbitMQ settings. For a full containerized setup, use Docker Compose and override `spring.kafka.bootstrap-servers` and `spring.rabbitmq.*` via environment variables.

---

## Configuration Reference

Key properties in `src/main/resources/application.properties`:

| Property | Default | Notes |
|---|---|---|
| `server.port` | `8080` | Application port |
| `alpha.vantage.api.key` | `${ALPHA_VANTAGE_API_KEY}` | Must be set via env var |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/stock_portfolio_alerts_db` | |
| `spring.datasource.username` | `root` | Change for non-local environments |
| `spring.datasource.password` | `root` | Change for non-local environments |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | Use `validate` or `update` in production |
| `spring.kafka.bootstrap-servers` | `localhost:9092` | |
| `management.endpoints.web.exposure.include` | `health,info` | Exposed actuator endpoints |
| `management.endpoint.health.show-details` | `always` | Show full health details |

---

## Testing

The project includes unit and integration tests using JUnit 5, Mockito, and Spring Security Test.

```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=AlertServiceTest

# Run with coverage report
./mvnw verify
```

**Test classes:**

| Class | What it tests |
|---|---|
| `AlertServiceTest` | Alert creation, user resolution from security context, repository interactions |
| `PortfolioControllerTest` | Controller layer with mocked service, security context injection |
| `StockportfolioalertsApplicationTests` | Application context loads successfully |

---

## Error Response Format

All errors return a consistent JSON body:

```json
{
  "timestamp": "2024-03-26T14:09:00",
  "status": 400,
  "error": "Portfolio not found"
}
```

| Status | Condition |
|---|---|
| `400 Bad Request` | Business rule violation, not found, unauthorized access, bad input |
| `500 Internal Server Error` | Unexpected application error |
| `401 Unauthorized` | Missing or invalid JWT token (returned by Spring Security before reaching controllers) |