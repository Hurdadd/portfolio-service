# Portfolio Position Service (Spring Boot + Redis)

A small learning project that simulates a **portfolio position**(CRUD + caching + rate limiting)

This repository is primarily built for **learning purposes**. Some parts may be simplified compared to a production-grade system, but the core ideas are intentionally implemented.

## What this service does
It manages `Position` records:
- `clientId` (portfolio owner)
- `symbol` (e.g., MSFT, AAPL)
- `quantity`
- `avgPrice`
- `updatedAt`

### Endpoints
- `POST /positions` → create a position
- `GET /positions/{id}` → get position by UUID (Redis cached + rate limited)
- `GET /clients/{clientId}/positions` → list client positions (cached)
- `PUT /positions/{id}` → update
- `DELETE /positions/{id}` → delete

## Key features implemented
- **Multi-tenant key namespace** via `X-Tenant-Id`
- **Cache-aside pattern** for reads
- **Cache invalidation** on write operations
- **Stampede protection** (cache rebuild lock) using `SET NX EX` + Lua unlock
- **Fixed-window rate limiting** using Redis Lua script style pattern
- **TTL jitter** to reduce synchronized expirations (thundering herd)
- **Binary serialization (Protobuf)** for cache payloads (smaller + faster than JSON)


---

## Tech stack
- Java 17
- Spring Boot
- Spring Data JPA
- MSSQL
- Spring Data Redis (Lettuce)
- Protobuf (for binary cache encoding)

---

## Running locally

### 1) Start dependencies
If you have Docker Compose enabled:

```bash
docker compose up -d
```
### 2) Run the service
Using Gradle:

```bash
./gradlew bootRun
```
Service runs on:

http://localhost:8085

How to test (curl)
- Create position
```bash
curl -i -X POST "http://localhost:8085/positions" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: t1" \
  -H "X-Client-Id: c1" \
  -d '{
    "clientId": "c1",
    "symbol": "MMD",
    "quantity": 75,
    "avgPrice": 999.12
  }'
```
Copy the id from the response.

- Get by id (cached + rate-limited)
```bash
curl -i -H "X-Tenant-Id: t1" -H "X-Client-Id: c1" \
  "http://localhost:8085/positions/<UUID>"
```
- List by client (cached)
  ```bash
  curl -i -H "X-Tenant-Id: t1" -H "X-Client-Id: c1" \
  "http://localhost:8085/clients/c1/positions"
  ```
- Update
```bash
curl -i -X PUT "http://localhost:8085/positions/<UUID>" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: t1" \
  -H "X-Client-Id: c1" \
  -d '{
    "symbol": "GHOLY",
    "quantity": 56,
    "avgPrice": 425.10
  }'

```
- Delete
```bash
curl -i -X DELETE -H "X-Tenant-Id: t1" -H "X-Client-Id: c1" \
  "http://localhost:8085/positions/<UUID>"
```
How to observe Redis behavior

Open redis-cli and run:
```bash
 MONITOR
```
Then call `GET /positions/{id}` multiple times:

**First call**
- Cache MISS
- Acquire Redis lock
- Load from source
- SET cache with TTL (+ jitter)
- Release lock

**Next calls**
- Cache HIT
- Read directly from Redis
- No lock, no DB call
- Until TTL expires

## Project Structure 

- `api/`  
  Standard API error responses + global exception mapping.

- `config/`  
  Cross-cutting infra config: Jackson time serialization, Redis binary template, tenant + trace filters.

- `controller/`  
  REST endpoints for CRUD operations.

- `domain/`  
  JPA entity (`PortfolioPosition`) stored in DB.

- `dto/`  
  Request/response objects with validation.

- `redis/`  
  All Redis concerns:
  - caching (string + binary/proto)
  - locking (stampede protection)
  - rate limiting (Lua-ish via script)
  - key conventions + TTL jitter

- `repository/`  
  Spring Data JPA repository.

- `service/`  
  Business logic, caching strategy, invalidation logic.

- `proto/`  
  Protobuf schema used for compact Redis serialization.
