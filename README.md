# Real-Time Stock Portfolio Alerts Application

A backend system that allows investors to:

- Manage stock portfolios
- Track stock prices
- Configure price alerts
- Receive notifications when price thresholds are reached

The system is designed using an **event-driven architecture** with messaging and real-time processing.

This project demonstrates enterprise backend concepts such as:

- Secure authentication using JWT
- Portfolio management using JPA relationships
- Event-driven processing using Kafka
- Asynchronous messaging using RabbitMQ
- Performance optimization using caching

## Architecture

The system follows a **Modular Monolith Architecture**.<br>
Controller → Service → Repository → MySQL

External integrations include:

- Stock Price APIs
- Kafka Event Stream
- RabbitMQ Messaging
- Caffeine Cache

This architecture keeps deployment simple while supporting event-driven workflows.

## Use Case Progress Tracker

| Use Case | Feature | Status    |
|--------|--------|-----------|
| UC1 | User Registration | Completed |
| UC2 | JWT Authentication | Completed |
| UC3 | Portfolio Creation | Completed |
| UC4 | Portfolio Retrieval | Completed |
| UC5 | Update Portfolio | Completed |
| UC6 | Delete Portfolio | Completed |
| UC7 | Add Stock | Pending   |
| UC8 | Fetch Live Stock Price | Pending   |
| UC9 | Create Alert | Pending   |
| UC10 | Process Price Updates (Kafka) | Pending   |
| UC11 | Send Alert Notification | Pending   |
| UC12 | Health Check | Pending   |
| UC13 | Global Exception Handling | Pending   |
| UC14 | JUnit Testing | Pending   |
| UC15 | REST Resource URI Design | Pending   |

## UC1 – User Registration

### Goal

Allow new investors to register securely in the system.

### Flow

1. User submits:

- Name
- Email
- Password

2. Backend performs:

- Validation using **Java 8 Predicate**
- Password encryption using **BCrypt**
- Duplicate email verification
- Persistence using **JPA**

3. Response returned

HTTP Status: **201 Created**

### API Endpoint

POST /api/users/register

### Example Request

```json
{
  "name": "user1",
  "email": "user1@email.com",
  "password": "StrongPass123"
}
```
---

# UC2 - JWT Authentication

### Goal
Authenticate registered users and generate a JWT token for stateless authorization.

### Flow
1. User submits login credentials. 
2. Backend fetches user using Optional. 
3. Password verified. 
4. JWT token generated. 
5. Token returned to the frontend.

### API Endpoint
POST /api/auth/login

### Example Request
```json
{
  "email": "user1@email.com",
  "password": "StrongPass123"
}
```

### Example Response
```json
{
  "token": "JWT_TOKEN"
}
```
---

## UC3 - Portfolio Creation

### Goal
Allow users to create a stock portfolio and compute initial investment value using Java 8 Streams.

### Flow
1. User submits stock list.
2. Backend calculates the total portfolio value.
3. Portfolio saved in MySQL/Postgres.
4. Response returned.

### API Endpoint
POST /api/portfolios/
POST /api/portfolios/upload

### Example request
```json
{
  "holdings": [
    {
      "ticker": "AAPL",
      "quantity": 10,
      "buyingPrice": 150
    },
    {
      "ticker": "TSLA",
      "quantity": 5,
      "buyingPrice": 220
    }
  ]
}
```

### Example Response
```json
{
    "holdings": [
        {
            "buyingPrice": 150.0,
            "id": 7,
            "quantity": 10,
            "ticker": "AAPL",
            "value": 150
        },
        {
            "buyingPrice": 200.0,
            "id": 8,
            "quantity": 5,
            "ticker": "TSLA",
            "value": 220
        }
    ],
    "id": 4,
    "totalValue": 2600.0,
    "user": {
        "email": "user@email.com",
        "id": 1,
        "name": "user_name",
        "password": "password"
    }
}
```
---

## UC4 - Portfolio Retrieval

### Goal
Retrieve portfolio details with proper REST resource design and filtering.

### Flow
1. User requests portfolio by ID. 
2. Backend fetches from DB. 
3. Applies filtering logic. 
4. Returns response with HATEOAS links.

### API Endpoint
GET /api/portfolios/{id}

### Example Response
```json
{
  "portfolioId": 1,
  "totalValue": 2300,
  "holdings": [
    {
      "ticker": "AAPL",
      "quantity": 10,
      "buyingPrice": 150,
      "value": 1500
    },
    {
      "ticker": "TSLA",
      "quantity": 2,
      "buyingPrice": 400,
      "value": 800
    }
  ],
  "_links": {
    "self": {
      "href": "/api/portfolios/1"
    },
    "create": {
      "href": "/api/portfolios"
    }
  }
}
```
---

## UC5 - Update Portfolio

### Goal
Allow modification of the portfolio with transactional safety.

### Flow
1. User submits update request. 
2. Backend validates the request. 
3. @Transactional ensures atomic update. 
4. DB commit performed.

### API Endpoint
PUT /api/portfolios/update

### Example Request
```json
{
  "holdings": [
    {
      "ticker": "AAPL",
      "quantity": 20,
      "buyingPrice": 150
    },
    {
      "ticker": "TSLA",
      "quantity": 5,
      "buyingPrice": 220
    }
  ]
}
```

## UC6 - Delete Portfolio

### Goal
Allow an authenticated user to delete an existing portfolio safely while maintaining audit logs using AOP.

### Flow
1. User sends a DELETE request. 
2. Backend verifies ownership. 
3. Portfolio removed from database. 
4. AOP logs deletion activity. 
5. Success response returned.

### API Endpoint
DELETE /api/portfolios/{id}

### Example logs
```bash
INFO  API request received to delete portfolio id=3
INFO  AUDIT: Portfolio deletion attempt detected. portfolioId=3
INFO  Delete portfolio request received. portfolioId=3, user=user@email.com
INFO  Portfolio deleted successfully. portfolioId=3, user=user@email.com
INFO  AUDIT: Portfolio deleted successfully. portfolioId=3
```