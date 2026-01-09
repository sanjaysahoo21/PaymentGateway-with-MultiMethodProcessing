# Mentor Specification Alignment Checklist

## ✅ TECH STACK & STRUCTURE

| Requirement | Status | Evidence |
|---|---|---|
| Backend: Java Spring Boot | ✅ | `backend/pom.xml` - Spring Boot 3.2.2 |
| Database: PostgreSQL | ✅ | `docker-compose.yml` - postgres:15-alpine |
| Frontend Dashboard: React | ✅ | `frontend/` - Vite + React |
| Checkout Page: React | ✅ | `checkout-page/` - Vite + React |
| Deployment: Docker + docker-compose | ✅ | `docker-compose.yml` present |
| Project structure matches spec | ✅ | All required directories exist |

---

## ✅ DOCKER REQUIREMENTS

| Service | Required Port | Status | Evidence |
|---|---|---|---|
| postgres | 5432 | ✅ | `docker-compose.yml:18` |
| api | 8000 | ✅ | `docker-compose.yml:29`, PORT=8000 env var |
| dashboard | 3000 | ✅ | `docker-compose.yml:42`, mapped to Nginx port 80 |
| checkout | 3001 | ✅ | `docker-compose.yml:48`, mapped to Nginx port 80 |
| API waits for DB health checks | ✅ | `docker-compose.yml:38` - depends_on condition |

---

## ✅ DATABASE SCHEMA (STRICT)

### Merchants Table
| Column | Type | Constraint | Status |
|---|---|---|---|
| id | UUID | PK | ✅ |
| name | VARCHAR(255) | NOT NULL | ✅ |
| email | VARCHAR(255) | UNIQUE | ✅ |
| api_key | VARCHAR(64) | UNIQUE | ✅ |
| api_secret | VARCHAR(64) | NOT NULL | ✅ |
| webhook_url | TEXT | Optional | ✅ |
| is_active | BOOLEAN | DEFAULT TRUE | ✅ |
| created_at | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | ✅ |
| updated_at | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | ✅ |

**File**: `backend/src/main/resources/schema.sql:30-43`

### Orders Table
| Column | Type | Constraint | Status |
|---|---|---|---|
| id | VARCHAR(64) | PK (order_XXXXX format) | ✅ |
| merchant_id | UUID | FK to merchants | ✅ |
| amount | INTEGER | >= 100 | ✅ |
| currency | VARCHAR(3) | DEFAULT 'INR' | ✅ |
| receipt | VARCHAR(255) | Optional | ✅ |
| notes | JSONB | JSON support | ✅ |
| status | VARCHAR(20) | DEFAULT 'created' | ✅ |
| created_at | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | ✅ |
| updated_at | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | ✅ |

**File**: `backend/src/main/resources/schema.sql:48-65`

### Payments Table
| Column | Type | Constraint | Status |
|---|---|---|---|
| id | VARCHAR(64) | PK (pay_XXXXX format) | ✅ |
| order_id | VARCHAR(64) | FK to orders | ✅ |
| merchant_id | UUID | FK to merchants | ✅ |
| amount | INTEGER | NOT NULL | ✅ |
| currency | VARCHAR(3) | DEFAULT 'INR' | ✅ |
| method | VARCHAR(20) | 'upi' or 'card' | ✅ |
| status | VARCHAR(20) | DEFAULT 'processing' | ✅ |
| vpa | VARCHAR(255) | UPI only | ✅ |
| card_network | VARCHAR(20) | Card only (never full number) | ✅ |
| card_last4 | VARCHAR(4) | Card only (last 4 digits only) | ✅ |
| error_code | VARCHAR(50) | Optional, on failure | ✅ |
| error_description | TEXT | Optional, on failure | ✅ |
| created_at | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | ✅ |
| updated_at | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | ✅ |

**File**: `backend/src/main/resources/schema.sql:70-108`

### Indexes
| Index Name | Table | Column | Status |
|---|---|---|---|
| idx_orders_merchant_id | orders | merchant_id | ✅ |
| idx_payments_order_id | payments | order_id | ✅ |
| idx_payments_status | payments | status | ✅ |

**File**: `backend/src/main/resources/schema.sql:110-114`

---

## ✅ AUTO-SEED TEST MERCHANT

| Requirement | Status | Evidence |
|---|---|---|
| ID: 550e8400-e29b-41d4-a716-446655440000 | ✅ | `TestMerchantSeeder.java:70` |
| Name: Test Merchant | ✅ | `TestMerchantSeeder.java:71` |
| Email: test@example.com | ✅ | `TestMerchantSeeder.java:72` |
| API Key: key_test_abc123 | ✅ | `TestMerchantSeeder.java:34-38` |
| API Secret: secret_test_xyz789 | ✅ | `TestMerchantSeeder.java:47-51` |
| Skip if already exists | ✅ | `TestMerchantSeeder.java:61` - ifPresentOrElse |

**File**: `backend/src/main/java/com/gateway/config/TestMerchantSeeder.java`

---

## ✅ AUTHENTICATION

| Requirement | Status | Evidence |
|---|---|---|
| Validate X-Api-Key header | ✅ | `MerchantAuthenticationFilter.java:56-69` |
| Validate X-Api-Secret header | ✅ | `MerchantAuthenticationFilter.java:70-75` |
| Return 401 on invalid auth | ✅ | `MerchantAuthenticationFilter.java:88-93` |
| Error code: AUTHENTICATION_ERROR | ✅ | `MerchantAuthenticationFilter.java:88` |
| Public endpoints exempt | ✅ | `MerchantAuthenticationFilter.java:43-51` |

**File**: `backend/src/main/java/com/gateway/config/MerchantAuthenticationFilter.java`

---

## ✅ API ENDPOINTS (EXACT)

### 1. GET /health
```json
Response format:
{
  "status": "healthy",
  "database": "connected",
  "timestamp": "ISO"
}
```
**File**: `backend/src/main/java/com/gateway/controllers/HealthController.java`
**Status**: ✅

### 2. POST /api/v1/orders
**Validations**:
- ✅ amount >= 100: `OrderService.java:42`
- ✅ Generates order_id format (order_): `IdGenerator.java`
- ✅ Saves to DB: `OrderController.java:44-57`

**File**: `backend/src/main/java/com/gateway/controllers/OrderController.java:44`

### 3. GET /api/v1/orders/{id}
**File**: `backend/src/main/java/com/gateway/controllers/OrderController.java:59-71`
**Status**: ✅

### 4. POST /api/v1/payments
**Validations**:
- ✅ Order exists & belongs to merchant: `PaymentService.java:53-63`
- ✅ UPI: VPA regex `^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$`: `ValidationUtil.java:9-15`
- ✅ Card: Luhn algorithm: `ValidationUtil.java:20-47`
- ✅ Card: Network detection (Visa/MC/Amex/RuPay): `ValidationUtil.java:56-72`
- ✅ Card: Expiry validation: `ValidationUtil.java:80-92`
- ✅ Stores only last4 + network: `PaymentService.java:81, 82`
- ✅ Status = processing: `Payment.java` (default in DB schema)
- ✅ 5-10s delay: `PaymentService.java:92-93`
- ✅ Random success/failure: `PaymentService.java:95-100`
- ✅ Updates status to success/failed: `PaymentService.java:102-108`

**File**: `backend/src/main/java/com/gateway/controllers/PaymentController.java:43-51`

### 5. GET /api/v1/payments/{id}
**File**: `backend/src/main/java/com/gateway/controllers/PaymentController.java:69-77`
**Status**: ✅

### 6. GET /api/v1/test/merchant
**File**: `backend/src/main/java/com/gateway/controllers/TestController.java:34-45`
**Status**: ✅ Returns seeded merchant with api_key and api_secret

---

## ✅ VALIDATION RULES

| Rule | Regex/Logic | Status | Evidence |
|---|---|---|---|
| VPA Format | `^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$` | ✅ | `ValidationUtil.java:9` |
| Luhn Algorithm | Sum-based validation, 13-19 digits | ✅ | `ValidationUtil.java:20-47` |
| Card Network Detection | Visa (4), MC (51-55), Amex (34,37), RuPay (60,65,81-89) | ✅ | `ValidationUtil.java:56-72` |
| Expiry Validation | MM (1-12), YY/YYYY, must be current/future | ✅ | `ValidationUtil.java:80-92` |

---

## ✅ TEST MODE (REQUIRED)

| Env Variable | Value | Purpose | Status |
|---|---|---|---|
| TEST_MODE | true/false | Enable deterministic results | ✅ |
| TEST_PAYMENT_SUCCESS | true/false | Force success/failure outcome | ✅ |
| TEST_PROCESSING_DELAY | milliseconds | Fixed delay instead of random | ✅ |

**File**: `backend/src/main/resources/application.properties`
**Implementation**: `PaymentService.java:90-100` - checks TEST_MODE env

---

## ✅ FRONTEND DASHBOARD (3000)

### Pages Present
| Page | URL | Status |
|---|---|---|
| Login | /login | ✅ `frontend/src/pages/Login.jsx` |
| Dashboard | / | ✅ `frontend/src/pages/Dashboard.jsx` |
| Transactions | /dashboard/transactions | ✅ `frontend/src/pages/Transactions.jsx` |

### Data-Test-ID Attributes (Login)
| Element | data-test-id | Status |
|---|---|---|
| Login form | login-form | ✅ Line 55 |
| Email input | email-input | ✅ Line 57 |
| Password input | password-input | ✅ Line 64 |
| Login button | login-button | ✅ Line 70 |

### Dashboard Display
| Item | data-test-id | Status | Evidence |
|---|---|---|---|
| Dashboard container | dashboard | ✅ | `Dashboard.jsx:63` |
| API Key section | api-credentials | ✅ | `Dashboard.jsx:71` |
| API Key value | api-key | ✅ | `Dashboard.jsx:75` |
| API Secret value | api-secret | ✅ | `Dashboard.jsx:79` |
| Stats container | stats-container | ✅ | `Dashboard.jsx:83` |
| Total transactions | total-transactions | ✅ | `Dashboard.jsx:87` |
| Total amount | total-amount | ✅ | `Dashboard.jsx:88` |
| Success rate | success-rate | ✅ | `Dashboard.jsx:89` |

### Transactions Table
| Element | data-test-id | Status |
|---|---|---|
| Transactions table | transactions-table | ✅ Line 69 |
| Transaction row | transaction-row | ✅ Line 83 |
| Payment ID | payment-id | ✅ Line 84 |
| Order ID | order-id | ✅ Line 85 |
| Amount | amount | ✅ Line 86 |
| Method | method | ✅ Line 87 |
| Status | status | ✅ Line 88 |
| Created At | created-at | ✅ Line 89 |

---

## ✅ CHECKOUT PAGE (3001)

### URL Format
```
http://localhost:3001/checkout?order_id=order_XXXXXXXXXXXXXXXX
```
**Status**: ✅

### Flow Implementation
| Step | Implementation | Status |
|---|---|---|
| Fetch order (public API) | GET /api/v1/orders/{id}/public | ✅ |
| Select UPI or Card | Form with method selection | ✅ |
| Submit payment | POST /api/v1/payments/public | ✅ |
| Show processing | Status polling | ✅ |
| Poll payment status | Every 2 seconds | ✅ |
| Show success/failure | Success/Failure pages | ✅ |

### Data-Test-ID Attributes (Checkout)
| Element | data-test-id | Status |
|---|---|---|
| Checkout container | checkout-container | ✅ Line 177 |
| Order summary | order-summary | ✅ Line 178 |
| Order amount | order-amount | ✅ Line 182 |
| Order ID | order-id | ✅ Line 186 |
| Payment methods | payment-methods | ✅ Line 190 |
| UPI method button | method-upi | ✅ Line 191 |
| Card method button | method-card | ✅ Line 194 |
| UPI form | upi-form | ✅ Line 199 |
| VPA input | vpa-input | ✅ Line 201 |
| Card form | card-form | ✅ Line 212 |
| Card number input | card-number-input | ✅ Line 214 |
| Expiry input | expiry-input | ✅ Line 221 |
| CVV input | cvv-input | ✅ Line 228 |
| Cardholder name | cardholder-name-input | ✅ Line 235 |
| Pay button (UPI) | pay-button | ✅ Line 207 |
| Pay button (Card) | pay-button | ✅ Line 241 |
| Processing state | processing-state | ✅ Line 246 |
| Processing message | processing-message | ✅ Line 248 |
| Success state | success-state | ✅ Line 251 |
| Payment ID | payment-id | ✅ Line 255 |
| Success message | success-message | ✅ Line 257 |

### Public APIs
| Endpoint | Method | Status |
|---|---|---|
| /api/v1/orders/{id}/public | GET | ✅ `OrderController.java:73` |
| /api/v1/payments/public | POST | ✅ `PaymentController.java:53` |

---

## ✅ ERROR FORMAT (STRICT)

```json
{
  "error": {
    "code": "ERROR_CODE",
    "description": "message"
  }
}
```

**File**: `backend/src/main/java/com/gateway/dto/ErrorResponse.java`

### Valid Error Codes
| Code | Usage | Status |
|---|---|---|
| AUTHENTICATION_ERROR | Invalid API credentials | ✅ `MerchantAuthenticationFilter.java:88` |
| BAD_REQUEST_ERROR | Invalid request data | ✅ Used in validation |
| NOT_FOUND_ERROR | Resource not found | ✅ Used in getters |
| INVALID_VPA | Invalid UPI format | ✅ `PaymentService.java` |
| INVALID_CARD | Invalid card number | ✅ `PaymentService.java` |
| EXPIRED_CARD | Card expiry in past | ✅ `PaymentService.java` |
| PAYMENT_FAILED | Payment processing failed | ✅ `PaymentService.java` |

**File**: `backend/src/main/java/com/gateway/config/GlobalExceptionHandler.java`

---

## ✅ FINAL REQUIREMENTS

| Requirement | Status | Evidence |
|---|---|---|
| All broken APIs fixed | ✅ | Endpoints functional, tested via docker-compose |
| Validation logic correct | ✅ | VPA, Luhn, network detection, expiry implemented |
| Docker startup issues resolved | ✅ | spring-boot-maven-plugin repackage, DATABASE_URL env |
| DB schema matches spec | ✅ | All tables, columns, constraints per spec |
| Test merchant seeded | ✅ | TestMerchantSeeder runs on startup |
| Frontend uses real API data | ✅ | Dashboard/checkout fetch from /api/v1 endpoints |
| Checkout flow works | ✅ | Order fetch → Payment creation → Status polling |
| README with test steps | ✅ | `README.md` and `TESTING_GUIDE.md` present |

---

## ✅ TESTING CHECKLIST

After `docker-compose up -d`, verify:

```bash
# 1. Health check
curl http://localhost:8000/health

# 2. Test merchant exists
curl http://localhost:8000/api/v1/test/merchant

# 3. Create order
curl -X POST http://localhost:8000/api/v1/orders \
  -H "X-Api-Key: key_test_abc123" \
  -H "X-Api-Secret: secret_test_xyz789" \
  -H "Content-Type: application/json" \
  -d '{"amount": 500, "currency": "INR"}'

# 4. Submit payment
curl -X POST http://localhost:8000/api/v1/payments/public \
  -H "Content-Type: application/json" \
  -d '{"order_id":"order_XXXXX","method":"upi","vpa":"user@upi"}'

# 5. Check Dashboard
open http://localhost:3000
# Login: test@example.com / any password

# 6. Check Checkout
open http://localhost:3001/checkout?order_id=order_XXXXX
```

---

## 📊 SUMMARY

✅ **ALL REQUIREMENTS MET** - Project aligns 100% with mentor specification.

- **Tech Stack**: Spring Boot, PostgreSQL, React (2x) ✅
- **Database**: Complete schema with all tables, indexes, constraints ✅
- **API**: All 6 endpoints + validation + error handling ✅
- **Authentication**: API key/secret validation ✅
- **Payment Methods**: UPI + Card with full validation ✅
- **Test Mode**: Environment-based deterministic outcomes ✅
- **Frontend**: Dashboard + Checkout with all data-test-id attributes ✅
- **Docker**: All services orchestrated correctly ✅
- **Documentation**: README + Testing guide ✅

