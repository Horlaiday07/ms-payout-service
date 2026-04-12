# Payout Service

Production-grade fintech payout platform built with Spring Boot 3, Clean Architecture, and Hexagonal Architecture.

## Features

- **Name Enquiry** – Bank account verification (NIBSS NIP)
- **Single Transfer** – Async payout processing
- **Batch Transfer** – Bulk payout processing (10k+ transactions)
- **Transaction Query** – Status tracking
- **Webhooks** – HMAC-signed callbacks to merchants
- **Idempotency** – Merchant reference + Idempotency-Key support
- **Security** – HMAC signature validation, IP whitelist, merchant validation

## Architecture

```
payout-api (controllers)
    ↓
service (business logic)
    ↓
domain (entities, ports)
    ↓
infrastructure (persistence, messaging, processor, webhook, ledger)
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/v1/payout/name-enquiry | Bank account name lookup |
| POST | /api/v1/payout/transfer | Single payout |
| POST | /api/v1/payout/batch-transfer | Batch payout |
| GET | /api/v1/payout/transactions/{id} | Query transaction status |

## Required Headers

- `X-Merchant-Id` – Merchant identifier
- `X-API-KEY` – API key (must match merchant)
- `X-SIGNATURE` – HMAC-SHA256 of (requestBody + X-TIMESTAMP)
- `X-TIMESTAMP` – Unix timestamp (seconds)
- `Idempotency-Key` – Optional, for transfer idempotency

## Standard Response Format

```json
{
  "responseCode": "00",
  "responseDescription": "SUCCESS",
  "merchantReference": "...",
  "paymentReference": "...",
  "processorReference": "...",
  "amount": 0.0,
  "remarks": "completed"
}
```

## Prerequisites

- Java 17
- PostgreSQL
- Redis
- RabbitMQ

## Configuration

See `application.yml` for:

- Database (PostgreSQL)
- Redis (idempotency, ID generation)
- RabbitMQ (queues: transfer, response, query, webhook, DLQ)
- NIBSS processor URLs
- **ms-ledger-service** — `payout.ledger.base-url` / `LEDGER_BASE_URL` (default `http://localhost:8083`); see [../ms-ledger-service/README.md](../ms-ledger-service/README.md)

## Swagger

- UI: http://localhost:8082/swagger-ui.html
- API docs: http://localhost:8082/api-docs
