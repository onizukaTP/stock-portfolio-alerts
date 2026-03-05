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

## Tech Stack

### Backend
- Spring Boot
- Spring Security
- Spring Data JPA

### Database
- MySQL

### Messaging
- Kafka
- RabbitMQ

### Caching
- Caffeine Cache

### Security
- JWT Authentication
- BCrypt Password Encryption

### Testing
- JUnit
- AssertJ

### Monitoring
- Spring Boot Actuator

### Frontend (Future)
- React

## Architecture

The system follows a **Modular Monolith Architecture**.<br>
Controller → Service → Repository → MySQL

External integrations include:

- Stock Price APIs
- Kafka Event Stream
- RabbitMQ Messaging
- Caffeine Cache

This architecture keeps deployment simple while supporting event-driven workflows.

## Package Structure
com.bl.stockportfolioalerts

auth<br>
├── controller<br>
├── service<br>
├── repository<br>
├── entity<br>
├── dto<br>
└── validation

portfolio<br>
stock<br>
alert<br>
messaging<br>
notification<br>

config<br>
exception<br>
audit

## Use Case Progress Tracker

| Use Case | Feature | Status |
|--------|--------|--------|
| UC1 | User Registration | Completed |
| UC2 | JWT Authentication | Pending |
| UC3 | Portfolio Creation | Pending |
| UC4 | Portfolio Retrieval | Pending |
| UC5 | Update Portfolio | Pending |
| UC6 | Delete Portfolio | Pending |
| UC7 | Add Stock | Pending |
| UC8 | Fetch Live Stock Price | Pending |
| UC9 | Create Alert | Pending |
| UC10 | Process Price Updates (Kafka) | Pending |
| UC11 | Send Alert Notification | Pending |
| UC12 | Health Check | Pending |
| UC13 | Global Exception Handling | Pending |
| UC14 | JUnit Testing | Pending |
| UC15 | REST Resource URI Design | Pending |

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

## 9️⃣ Concepts Used

```markdown
## Concepts Implemented

- Java Functional Interfaces
- Predicate-based validation
- JPA Entity mapping
- REST API design
- BCrypt password encryption
```
## Upcoming Features

- JWT Authentication
- Portfolio Management
- Stock Price Streaming
- Alert Engine
- Kafka Event Processing
- RabbitMQ Notifications
- Application Monitoring