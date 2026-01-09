# System Architecture

## High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                               │
├─────────────────────────────────────────────────────────────────────┤
│  Dashboard (React)              Checkout Page (React)                │
│  Port: 3000                     Port: 3001                          │
│  ├─ Login Page                  ├─ Order Summary                    │
│  ├─ Dashboard (API Creds)       ├─ Payment Methods (UPI/Card)      │
│  └─ Transactions Table          └─ Success/Failure Pages            │
│                                                                      │
│  Features: Light/Dark Theme, Real-time Updates, Responsive Design  │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                    HTTP(S) REST API Calls
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      API LAYER (Spring Boot)                        │
│                        Port: 8000                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ REST Controllers                                              │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │ • OrderController      - Order CRUD & public fetch           │  │
│  │ • PaymentController    - Payment processing & public API     │  │
│  │ • HealthController     - Health check endpoint               │  │
│  │ • TestController       - Test merchant credentials           │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                  │                                   │
│                                  ▼                                   │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ Authentication & Security Layer                               │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │ • MerchantAuthenticationFilter                               │  │
│  │   - Validates X-Api-Key & X-Api-Secret headers              │  │
│  │   - Allows public endpoints (/public routes)                 │  │
│  │ • GlobalExceptionHandler                                     │  │
│  │   - Unified error response formatting                        │  │
│  │   - HTTP status code mapping                                 │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                  │                                   │
│                                  ▼                                   │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ Business Logic Layer (Services)                              │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │ • OrderService                                               │  │
│  │   - Order creation & retrieval                               │  │
│  │   - Amount validation (>= 100 paise)                         │  │
│  │                                                               │  │
│  │ • PaymentService                                             │  │
│  │   - Payment validation (UPI, Card)                           │  │
│  │   - VPA format validation                                    │  │
│  │   - Luhn algorithm (card number)                             │  │
│  │   - Card network detection                                   │  │
│  │   - Expiry date validation                                   │  │
│  │   - Payment simulation (5-10s processing)                    │  │
│  │   - Status transitions (processing → success/failed)         │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                  │                                   │
│                                  ▼                                   │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ Validation Layer (ValidationUtil)                            │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │ • UPI VPA: ^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$                   │  │
│  │ • Card Luhn: 13-19 digits checksum validation                │  │
│  │ • Network Detection: Visa, MC, Amex, RuPay                  │  │
│  │ • Expiry: MM/YY format, not in past                          │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                  │                                   │
│                                  ▼                                   │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ Data Access Layer (Repositories - JPA)                       │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │ • MerchantRepository   - Merchant queries & auth             │  │
│  │ • OrderRepository      - Order CRUD operations               │  │
│  │ • PaymentRepository    - Payment queries & status tracking   │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                        JDBC SQL Connections
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER (PostgreSQL)                      │
│                        Port: 5432                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │ Merchants Table                                             │  │
│  ├─────────────────────────────────────────────────────────────┤  │
│  │ • id (UUID) - Primary Key                                   │  │
│  │ • email, api_key, api_secret (unique, indexed)             │  │
│  │ • is_active, created_at, updated_at                         │  │
│  │ • Test Merchant Auto-Seeded: key_test_abc123               │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │ Orders Table                                                │  │
│  ├─────────────────────────────────────────────────────────────┤  │
│  │ • id (VARCHAR) - order_XXXXX format                         │  │
│  │ • merchant_id (FK) - Linked to Merchants                    │  │
│  │ • amount (INTEGER, >= 100)                                  │  │
│  │ • currency, receipt, notes, status                          │  │
│  │ • created_at, updated_at                                    │  │
│  │ • Index: merchant_id for fast lookups                       │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │ Payments Table                                              │  │
│  ├─────────────────────────────────────────────────────────────┤  │
│  │ • id (VARCHAR) - pay_XXXXX format                           │  │
│  │ • order_id, merchant_id (FKs)                               │  │
│  │ • amount, currency, method (upi|card)                       │  │
│  │ • status (processing|success|failed)                        │  │
│  │ • vpa (UPI only), card_network, card_last4 (Card only)     │  │
│  │ • error_code, error_description (on failure)                │  │
│  │ • Indexes: order_id, status for efficient queries           │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Data Flow Diagrams

### 1. Order Creation Flow

```
Client App (Dashboard/API)
         │
         │ POST /api/v1/orders
         │ Headers: X-Api-Key, X-Api-Secret
         │ Body: {amount, currency, receipt}
         │
         ▼
OrderController.createOrder()
         │
         ├─ Validate authentication (MerchantAuthenticationFilter)
         │
         ├─ Validate amount >= 100
         │
         ├─ Generate order_id (order_XXXXX)
         │
         ├─ Create Order entity
         │
         └─► OrderRepository.save()
             │
             ▼
         PostgreSQL
         │
         └─► Return Order (id, amount, status: "created")
```

### 2. Payment Processing Flow

```
Checkout Page
    │
    │ 1. POST /api/v1/payments/public
    │    Body: {order_id, method, [vpa|card]}
    │
    ▼
PaymentController.createPayment()
    │
    ├─ Fetch Order (OrderRepository)
    │
    ├─ Validate Order exists & matches amount
    │
    ├─ VALIDATE PAYMENT METHOD
    │  │
    │  ├─ If UPI:
    │  │  └─ ValidationUtil.isValidVPA()
    │  │     → Regex: ^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$
    │  │
    │  └─ If Card:
    │     ├─ ValidationUtil.isValidCardNumber()
    │     │  → Luhn algorithm (13-19 digits)
    │     ├─ ValidationUtil.getCardNetwork()
    │     │  → Detect: Visa(4), MC(51-55), Amex(34,37), RuPay(60,65,81-89)
    │     └─ ValidationUtil.isValidExpiry()
    │        → MM(1-12), YY/YYYY, not in past
    │
    ├─ Create Payment entity (status: "processing")
    │
    ├─ PaymentRepository.save()
    │
    ├─ SIMULATE PROCESSING
    │  └─ Sleep 5-10 seconds (configurable)
    │
    ├─ RANDOM OUTCOME
    │  └─ ~90% success, ~10% failure
    │
    └─► Update Payment (status: "success"|"failed")
        └─► PaymentRepository.save()
            │
            ▼
        Return Payment (id, status)


Checkout Page (Poll every 2 seconds)
    │
    │ 2. GET /api/v1/payments/{id}/public
    │
    ▼
PaymentController.getPayment()
    │
    └─► Return Payment
        ├─ If processing: Poll again
        ├─ If success: Show success page
        └─ If failed: Show failure page
```

### 3. Dashboard Authentication Flow

```
Dashboard (Login Page)
    │
    │ Email: test@example.com (any password)
    │
    ▼
App.jsx (AuthContext)
    │
    └─► Store auth:
        {
          email: "test@example.com",
          apiKey: "key_test_abc123",
          apiSecret: "secret_test_xyz789"
        }
        │
        ▼
    Dashboard.jsx
        │
        ├─ Fetch /api/v1/payments (with auth headers)
        │
        └─► MerchantAuthenticationFilter
            │
            ├─ Extract X-Api-Key header
            ├─ Extract X-Api-Secret header
            │
            ├─ Validate against MerchantRepository
            │
            └─► Allow access to protected endpoints
```

---

## Component Architecture

### Backend Components

```
com.gateway
├── PaymentGatewayApplication.java
│   └─ Spring Boot main entry point
│
├── config/
│   ├─ SecurityConfig.java
│   │  └─ Spring Security configuration
│   │
│   ├─ MerchantAuthenticationFilter.java
│   │  └─ Custom API key/secret validation
│   │
│   ├─ GlobalExceptionHandler.java
│   │  └─ Centralized error handling
│   │
│   └─ TestMerchantSeeder.java
│      └─ Auto-seeds test merchant on startup
│
├── controllers/
│   ├─ OrderController.java
│   │  ├─ POST /api/v1/orders
│   │  └─ GET /api/v1/orders/{id}
│   │
│   ├─ PaymentController.java
│   │  ├─ POST /api/v1/payments
│   │  └─ GET /api/v1/payments/{id}
│   │
│   ├─ HealthController.java
│   │  └─ GET /health
│   │
│   └─ TestController.java
│      └─ GET /api/v1/test/merchant
│
├── dto/
│   ├─ OrderCreateRequest.java
│   ├─ PaymentCreateRequest.java
│   └─ ErrorResponse.java
│
├── exception/
│   └─ ApiException.java
│
├── models/
│   ├─ Merchant.java (JPA Entity)
│   ├─ Order.java (JPA Entity)
│   └─ Payment.java (JPA Entity)
│
├── repositories/
│   ├─ MerchantRepository.java (JpaRepository)
│   ├─ OrderRepository.java (JpaRepository)
│   └─ PaymentRepository.java (JpaRepository)
│
├── services/
│   ├─ OrderService.java
│   │  └─ Business logic for orders
│   │
│   └─ PaymentService.java
│      ├─ Payment validation
│      ├─ Processing simulation
│      └─ Status management
│
└── util/
    ├─ ValidationUtil.java
    │  ├─ VPA regex validation
    │  ├─ Luhn algorithm
    │  ├─ Card network detection
    │  └─ Expiry validation
    │
    └─ IdGenerator.java
       └─ order_XXXXX, pay_XXXXX generation
```

### Frontend Components

**Dashboard (frontend/):**
```
src/
├── pages/
│   ├─ Login.jsx          - Merchant login form
│   ├─ Dashboard.jsx      - API credentials + stats
│   └─ Transactions.jsx   - Payment history table
│
├── App.jsx               - Router + AuthContext
├── api.js                - Axios client setup
├── main.jsx              - React entry
└── styles.css            - Global styles + Light/Dark theme
```

**Checkout (checkout-page/):**
```
src/
├── pages/
│   ├─ Checkout.jsx       - Payment form + polling
│   ├─ Success.jsx        - Success page
│   └─ Failure.jsx        - Failure page
│
├── main.jsx              - React entry + Theme init
└── styles.css            - Checkout styles + Light/Dark theme
```

---

## Security Architecture

### Authentication
- **Merchant API Requests**: X-Api-Key + X-Api-Secret headers
- **Dashboard Login**: Email-based (no password for demo)
- **Public Checkout**: No authentication required

### Data Protection
- **Card Numbers**: Only last 4 digits stored
- **Card Network**: Detected and stored (never full number)
- **VPA**: Full value stored (UPI requires it)
- **Passwords**: Not stored (demo mode)

### Error Handling
- **Unified error format**: `{error: {code, description}}`
- **HTTP Status Codes**: 401 (auth), 400 (validation), 404 (not found), 500 (server)
- **Sensitive data**: Not exposed in error messages

---

## Deployment Architecture

```
┌─────────────────────────────────────────┐
│       Docker Compose Orchestration      │
├─────────────────────────────────────────┤
│                                         │
│  postgres:15-alpine                     │
│  ├─ Port: 5432                          │
│  ├─ Health check enabled                │
│  └─ Persists /var/lib/postgresql/data   │
│                                         │
│  monochromepay_api (Spring Boot)        │
│  ├─ Port: 8000                          │
│  ├─ Depends on: postgres (health)       │
│  ├─ ENV: DATABASE_URL, PORT, TEST_MODE  │
│  └─ Auto-seeds test merchant            │
│                                         │
│  monochromepay_dashboard (React)        │
│  ├─ Port: 3000                          │
│  ├─ Nginx reverse proxy                 │
│  └─ Depends on: api                     │
│                                         │
│  monochromepay_checkout (React)         │
│  ├─ Port: 3001                          │
│  ├─ Nginx reverse proxy                 │
│  └─ Depends on: api                     │
│                                         │
└─────────────────────────────────────────┘
```

### Service Dependencies
```
postgres (ready)
    ↓
api (waits for postgres health)
    ↓
dashboard (waits for api)
checkout (waits for api)
```

---

## Technology Rationale

| Component | Choice | Rationale |
|-----------|--------|-----------|
| **Language** | Java 17 | Type-safe, mature ecosystem, Spring Boot framework |
| **Framework** | Spring Boot 3.2 | Industry standard, built-in security, easy testing |
| **Database** | PostgreSQL 15 | Reliable, JSONB support, excellent for transactions |
| **ORM** | JPA/Hibernate | Automatic schema management, query optimization |
| **Frontend** | React 18 | Component reusability, fast rendering, large community |
| **Build Tool** | Vite | Fast development, modern JavaScript support |
| **Containerization** | Docker | Consistent deployment, easy scaling |
| **Orchestration** | Docker Compose | Simple setup, all-in-one deployment |

---

## System Performance Characteristics

- **Order Creation**: < 100ms
- **Payment Validation**: < 50ms
- **Payment Processing**: 5-10s (simulated)
- **Status Polling**: 2s intervals (configurable)
- **Dashboard Load**: < 500ms
- **Checkout Page Load**: < 200ms

---

## Scalability Considerations

### Current (Single Instance)
- All services on one machine
- PostgreSQL local persistence
- Suitable for demo/learning

### Future (Production)
- API service: Horizontal scaling with load balancer
- Database: Connection pooling, read replicas
- Dashboard/Checkout: CDN distribution
- Caching: Redis for session management
- Monitoring: Prometheus + Grafana metrics
