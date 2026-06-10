# Testing Guide — Stock Portfolio Alerts

A step-by-step walkthrough to verify Docker infrastructure, check Kafka listeners, and test every API endpoint using Postman.

---

## Prerequisites

All containers must be running before you start. Run:

```bash
docker compose -f docker-compose.full.yml up --build
```

---

## Step 1 — Verify All Containers Are Up

```bash
docker ps
```

Expected output — all four containers should show `healthy` or `Up`:

```
CONTAINER ID   IMAGE                             STATUS
xxxx           stockportfolioalerts-app          Up X minutes
xxxx           confluentinc/cp-kafka:7.6.1       Up X minutes (healthy)
xxxx           rabbitmq:3.13-management-alpine   Up X minutes (healthy)
xxxx           mysql:8.0                         Up X minutes (healthy)
xxxx           confluentinc/cp-zookeeper:7.6.1   Up X minutes
```

If any container is restarting or unhealthy:

```bash
# Check logs for that specific container
docker logs spa-app -f
docker logs spa-kafka -f
docker logs spa-mysql -f
docker logs spa-rabbitmq -f
```

---

## Step 2 — Verify Kafka Listeners

This is the most common failure point. Run these checks before testing any endpoints.

### Check advertised listeners

```bash
docker exec -it spa-kafka env | grep KAFKA_ADVERTISED
```

Expected:

```
KAFKA_ADVERTISED_LISTENERS=PLAINTEXT_INTERNAL://kafka:29092,PLAINTEXT_LOCAL://localhost:9092
```

If you only see `PLAINTEXT://kafka:29092` — your compose file still has the old config. Update it and restart.

### Confirm broker is reachable inside the container

```bash
docker exec -it spa-kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

Expected: A long list of API versions printed. No `WARN` or connection errors.

### Confirm Spring Boot connected to Kafka successfully

```bash
docker logs spa-app 2>&1 | grep -i "kafka\|topic\|broker"
```

Expected lines to see:

```
Successfully configured topics
partitions assigned: [stock-price-0]
```

### List all Kafka topics

```bash
docker exec -it spa-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --list
```

Expected: `stock-price` appears in the list.

### Describe the stock-price topic

```bash
docker exec -it spa-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic stock-price
```

Expected:

```
Topic: stock-price  Partition: 0  Leader: 1  Replicas: 1  Isr: 1
```

---

## Step 3 — Verify RabbitMQ

Open your browser and go to:

```
http://localhost:15672
```

Login with `guest / guest`. You should see the RabbitMQ management dashboard.

Go to **Queues** tab — after the first alert is triggered later in this guide, `stock-alert-queue` will appear here.

Alternatively check via terminal:

```bash
docker exec -it spa-rabbitmq rabbitmq-diagnostics ping
```

Expected: `Ping succeeded`

---

## Step 4 — Postman Setup

### Create a Collection

Open Postman → New Collection → name it `Stock Portfolio Alerts`.

### Set up automatic token storage

Go to your Collection → **Edit** → **Variables** tab. Add:

| Variable | Initial Value | Current Value |
|---|---|---|
| `token` | (empty) | (empty) |
| `base_url` | `http://localhost:8080` | `http://localhost:8080` |

Now use `{{base_url}}` and `{{token}}` in all requests.

---

## Step 5 — Register a User

```
Method  : POST
URL     : {{base_url}}/api/v1/users/register
Headers : Content-Type: application/json
```

Body (raw → JSON):

```json
{
  "name": "Tharun",
  "email": "tharun@example.com",
  "password": "Tharun123"
}
```

Expected response `200 OK`:

```
User registered successfully
```

Common errors:

| Error | Cause |
|---|---|
| `403 Forbidden` | `/api/v1/users/register` not in permitAll — check SecurityConfig |
| `400 Bad Request` | Email missing `@` or password under 8 characters |
| `500` | Email already registered — use a different email |

---

## Step 6 — Login and Capture Token

```
Method  : POST
URL     : {{base_url}}/api/auth/login
Headers : Content-Type: application/json
```

Body:

```json
{
  "email": "tharun@example.com",
  "password": "Tharun123"
}
```

Expected response `200 OK`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Auto-capture the token

Go to this request → **Tests** tab → paste:

```javascript
const res = pm.response.json();
pm.collectionVariables.set("token", res.token);
console.log("Token saved:", res.token);
```

Now every subsequent request uses `{{token}}` automatically. Token expires after 1 hour — re-run login to refresh it.

---

## Step 7 — Create a Portfolio

```
Method  : POST
URL     : {{base_url}}/api/portfolios
Headers : Content-Type: application/json
          Authorization: Bearer {{token}}
```

Body:

```json
{
  "holdings": [
    { "ticker": "AAPL", "quantity": 10, "buyingPrice": 180.00 },
    { "ticker": "MSFT", "quantity": 5,  "buyingPrice": 420.00 }
  ]
}
```

Expected response `200 OK`:

```json
{
  "portfolioId": 1,
  "totalValue": 3900.00,
  "holdings": [
    { "ticker": "MSFT", "quantity": 5,  "buyingPrice": 420.00, "value": 2100.00 },
    { "ticker": "AAPL", "quantity": 10, "buyingPrice": 180.00, "value": 1800.00 }
  ]
}
```

Note: holdings are sorted by value descending. Note the `portfolioId` — use it in the next requests.

---

## Step 8 — Get Portfolio by ID

```
Method  : GET
URL     : {{base_url}}/api/portfolios/1
Headers : Authorization: Bearer {{token}}
```

Expected response `200 OK` — same shape as above plus HATEOAS links:

```json
{
  "portfolioId": 1,
  "totalValue": 3900.00,
  "holdings": [ "..." ],
  "_links": {
    "self":   { "href": "http://localhost:8080/api/portfolios/1" },
    "create": { "href": "http://localhost:8080/api/portfolios" }
  }
}
```

---

## Step 9 — Get All Portfolios

```
Method  : GET
URL     : {{base_url}}/api/portfolios/showall
Headers : Authorization: Bearer {{token}}
```

Expected response `200 OK` — array of all portfolios for the logged-in user.

---

## Step 10 — Update a Portfolio

```
Method  : PUT
URL     : {{base_url}}/api/portfolios/1
Headers : Content-Type: application/json
          Authorization: Bearer {{token}}
```

Body:

```json
{
  "holdings": [
    { "ticker": "NVDA", "quantity": 8, "buyingPrice": 900.00 },
    { "ticker": "AAPL", "quantity": 5, "buyingPrice": 182.00 }
  ]
}
```

Expected response `200 OK` — existing holdings are replaced, `totalValue` recalculated.

---

## Step 11 — Upload Portfolio via CSV

```
Method  : POST
URL     : {{base_url}}/api/portfolios/upload
Headers : Authorization: Bearer {{token}}
Body    : form-data → key: file, type: File
```

Create a file called `holdings.csv` on your machine with this content:

```
AAPL,10,180.00
MSFT,5,420.00
GOOGL,3,175.50
```

In Postman, select `Body → form-data`, set key to `file`, change type to `File`, and upload the csv.

Expected response `200 OK` — portfolio created from CSV rows.

---

## Step 12 — Add a Stock Using Live Price

```
Method  : POST
URL     : {{base_url}}/api/portfolios/1/stocks
Headers : Content-Type: application/json
          Authorization: Bearer {{token}}
```

Body:

```json
{
  "ticker": "TSLA",
  "quantity": 3
}
```

Expected response `200 OK` — TSLA added at live price (or fallback price if rate limited).

Check app logs to confirm which path was taken:

```bash
docker logs spa-app 2>&1 | grep -i "tsla\|price\|fallback"
```

You will see one of:

```
# Live price fetched successfully
Fetched live price for ticker=TSLA price=182.50

# Rate limited — fallback used
Using fallback price for ticker=TSLA price=175.0 reason=rate limited
```

---

## Step 13 — Delete a Portfolio

```
Method  : DELETE
URL     : {{base_url}}/api/portfolios/1
Headers : Authorization: Bearer {{token}}
```

Expected response `204 No Content` — no body returned.

Verify audit log fired:

```bash
docker logs spa-app 2>&1 | grep -i "audit\|delete"
```

---

## Step 14 — Create a Price Alert

```
Method  : POST
URL     : {{base_url}}/api/alerts
Headers : Content-Type: application/json
          Authorization: Bearer {{token}}
```

Body — set threshold below current AAPL price so it triggers easily:

```json
{
  "ticker": "AAPL",
  "thresholdPrice": 150.00
}
```

Expected response `200 OK`:

```json
{
  "id": 1,
  "ticker": "AAPL",
  "thresholdPrice": 150.00,
  "user": {
    "id": 1,
    "name": "Tharun",
    "email": "tharun@example.com"
  }
}
```

Note the `id` — use it in the next request.

---

## Step 15 — Get Alert by ID

```
Method  : GET
URL     : {{base_url}}/api/alerts/1
Headers : Authorization: Bearer {{token}}
```

Expected response `200 OK` — same shape as create response.

---

## Step 16 — Trigger the Full Kafka Pipeline

This simulates a real stock price event flowing through Kafka → threshold check → RabbitMQ → notification.

### Open two terminals side by side

**Terminal 1 — watch app logs:**

```bash
docker logs spa-app -f
```

**Terminal 2 — start the Kafka producer:**

```bash
docker exec -it spa-kafka kafka-console-producer \
  --broker-list localhost:9092 \
  --topic stock-price
```

You will see a `>` prompt. Type this exactly and hit Enter:

```json
{"ticker":"AAPL","price":195.00}
```

Since `195.00 > 150.00` (your threshold from Step 14), the alert should fire.

### Expected in Terminal 1 (app logs):

```
StockPriceListener   - Received stock price event: ticker=AAPL price=195.0
AlertService         - Alert triggered for alertId=1 ticker=AAPL
AlertEventPublisher  - Publishing alert to RabbitMQ for ticker=AAPL
AlertEventConsumer   - Received alert event from RabbitMQ alertId=1
NotificationService  - ALERT: AAPL crossed threshold 150.0 — current price 195.0
```

### Test that alerts below threshold do NOT trigger

Send a price below the threshold:

```json
{"ticker":"AAPL","price":140.00}
```

Nothing should appear in the logs for this — the threshold check filters it out.

Hit `Ctrl+C` to exit the producer.

### Verify the message went through RabbitMQ

```bash
docker exec -it spa-rabbitmq rabbitmqctl list_queues name messages
```

Expected — messages should be `0` since they were consumed:

```
stock-alert-queue   0
```

---

## Step 17 — Health Check

```
Method  : GET
URL     : {{base_url}}/actuator/health
Headers : (none required)
```

Expected response `200 OK`:

```json
{
  "status": "UP",
  "components": {
    "application": {
      "status": "UP",
      "details": { "application": "Stock Portfolio Alerts Running" }
    },
    "kafka": { "status": "UP" },
    "db":    { "status": "UP" },
    "rabbit": { "status": "UP" }
  }
}
```

---

## Useful Docker Commands

### Restart everything cleanly (wipes DB and Kafka data)

```bash
docker compose -f docker-compose.full.yml down -v
docker compose -f docker-compose.full.yml up --build
```

### Restart only the app (after code change + jar rebuild)

```bash
./mvnw clean package -DskipTests
docker compose -f docker-compose.full.yml restart app
```

### Restart without rebuilding image (env/config change only)

```bash
docker compose -f docker-compose.full.yml down
docker compose -f docker-compose.full.yml up
```

### Watch logs for all containers at once

```bash
docker compose -f docker-compose.full.yml logs -f
```

### Check health status of all containers

```bash
docker inspect --format='{{.Name}} → {{.State.Health.Status}}' \
  spa-app spa-kafka spa-rabbitmq spa-mysql
```

### Check Kafka consumer group lag

```bash
docker exec -it spa-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group stock-alert-group
```

`LAG` column should be `0` when all messages are consumed.

### Read messages directly from Kafka topic

```bash
docker exec -it spa-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic stock-price \
  --from-beginning
```

### Flush all Docker build cache (if image issues persist)

```bash
docker builder prune -f
docker compose -f docker-compose.full.yml up --build --force-recreate
```