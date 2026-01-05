# Payment Gateway with Multi-Method Processing

A minimal payment gateway simulation with API-key authentication, order and payment flows (UPI / card), and two React frontends: merchant dashboard (port 3000) and hosted checkout (port 3001). Runs fully via `docker-compose up -d`.

## Quick start

```bash
# from repo root
cp .env.example .env  # optional for local runs
docker-compose up -d  # starts postgres, api, dashboard, checkout
```

Services:
- API: http://localhost:8000
- Dashboard: http://localhost:3000
- Checkout: http://localhost:3001/checkout?order_id=...

Test merchant (auto-seeded on startup):
- Email: `test@example.com`
- API Key: `key_test_abc123`
- API Secret: `secret_test_xyz789`

## API basics
- Health: `GET /health`
- Create order: `POST /api/v1/orders` (auth headers `X-Api-Key`, `X-Api-Secret`)
- Get order: `GET /api/v1/orders/{order_id}` (auth)
- Public order for checkout: `GET /api/v1/orders/{order_id}/public`
- Create payment: `POST /api/v1/payments` (auth)
- Public create payment: `POST /api/v1/payments/public`
- Get payment: `GET /api/v1/payments/{payment_id}` (auth)
- Public payment status: `GET /api/v1/payments/{payment_id}/public`
- List payments (per merchant): `GET /api/v1/payments` (auth)
- Test merchant info: `GET /api/v1/test/merchant`

Payment simulation:
- Payments start in `processing`, sleep 5–10s (or test delay), then become `success` or `failed` using configured success rates (test mode supported).

## Environment
See `.env.example` for configurable values (DB URL, success rates, test mode flags). `application.properties` reads these via Spring config. Frontends read `VITE_API_URL` (default http://localhost:8000).

## Development
- Backend: Java 17 + Spring Boot 3, build with Maven
- Frontends: Vite + React
- Docker: backend uses multi-stage build; frontends build then serve with nginx
