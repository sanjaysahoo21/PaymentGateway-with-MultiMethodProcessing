# Database Schema Documentation

## Overview
MonochromePay uses PostgreSQL 15 with three main tables: Merchants, Orders, and Payments. The schema supports multi-method payment processing with full transaction tracking.

---

## Table Relationships Diagram

```
┌─────────────────────┐
│    MERCHANTS        │
│                     │
│ PK: id (UUID)       │
│ UQ: email           │
│ UQ: api_key         │
│ UQ: api_secret      │
└──────────┬──────────┘
           │
           │ 1:N
           ├────────────────────────────────────┐
           │                                    │
           │                                    │
           ▼                                    ▼
┌─────────────────────┐          ┌─────────────────────┐
│     ORDERS          │          │    PAYMENTS         │
│                     │          │                     │
│ PK: id (VARCHAR)    │◄─────────│ FK: merchant_id     │
│     order_XXXXX     │   FK     │ FK: order_id        │
│ FK: merchant_id     │          │ PK: id (VARCHAR)    │
│                     │          │     pay_XXXXX       │
│ Status: created     │          │                     │
└─────────────────────┘          │ Status: processing, │
                                 │         success,    │
                                 │         failed      │
                                 │                     │
                                 └─────────────────────┘
```

---

## Table Definitions

### 1. MERCHANTS Table

**Purpose**: Store merchant accounts and API credentials

```sql
CREATE TABLE merchants (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  
  -- Merchant Identity
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  
  -- API Credentials
  api_key VARCHAR(64) NOT NULL UNIQUE,
  api_secret VARCHAR(64) NOT NULL,
  
  -- Webhook Configuration
  webhook_url TEXT,
  
  -- Status
  is_active BOOLEAN DEFAULT TRUE,
  
  -- Timestamps
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

**Columns Explained**:
| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| `id` | UUID | PK, default gen_random_uuid() | Unique merchant identifier |
| `name` | VARCHAR(255) | NOT NULL | Merchant business name |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | Merchant contact email |
| `api_key` | VARCHAR(64) | NOT NULL, UNIQUE | Public key for authentication |
| `api_secret` | VARCHAR(64) | NOT NULL | Private secret for authentication |
| `webhook_url` | TEXT | Optional | Webhook endpoint (future use) |
| `is_active` | BOOLEAN | DEFAULT TRUE | Account status |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Account creation time |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update time |

**Sample Data** (Auto-seeded):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Test Merchant",
  "email": "test@example.com",
  "api_key": "key_test_abc123",
  "api_secret": "secret_test_xyz789",
  "is_active": true,
  "created_at": "2026-01-09T12:00:00Z"
}
```

**Indexes**:
```sql
CREATE INDEX idx_merchants_email ON merchants(email);
CREATE INDEX idx_merchants_api_key ON merchants(api_key);
```

---

### 2. ORDERS Table

**Purpose**: Track payment orders and their lifecycle

```sql
CREATE TABLE orders (
  -- Primary Key
  id VARCHAR(64) PRIMARY KEY,  -- order_XXXXX format
  
  -- Foreign Key
  merchant_id UUID NOT NULL,
  CONSTRAINT fk_orders_merchant FOREIGN KEY (merchant_id)
    REFERENCES merchants(id) ON DELETE CASCADE,
  
  -- Order Details
  amount INTEGER NOT NULL,  -- In paise (100 = ₹1.00)
  currency VARCHAR(3) DEFAULT 'INR',
  receipt VARCHAR(255),  -- Merchant's receipt reference
  
  -- Additional Data
  notes JSONB,  -- Flexible JSON for custom fields
  
  -- Status
  status VARCHAR(20) DEFAULT 'created',
  
  -- Timestamps
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

**Columns Explained**:
| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| `id` | VARCHAR(64) | PK | Unique order ID (order_ABC123...) |
| `merchant_id` | UUID | NOT NULL, FK | Links to Merchants table |
| `amount` | INTEGER | NOT NULL | Amount in paise (smallest unit) |
| `currency` | VARCHAR(3) | DEFAULT 'INR' | Currency code (INR, USD, etc.) |
| `receipt` | VARCHAR(255) | Optional | Merchant's reference ID |
| `notes` | JSONB | Optional | JSON metadata (custom fields) |
| `status` | VARCHAR(20) | DEFAULT 'created' | Order state (created, pending, paid) |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Order creation time |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last modification time |

**Amount Validation**:
- Minimum: 100 paise (₹1.00)
- Stored as INTEGER to avoid floating-point issues
- Conversion: API receives in paise, displays in INR (÷100)

**Sample Data**:
```json
{
  "id": "order_ABC123DEF456",
  "merchant_id": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 50000,
  "currency": "INR",
  "receipt": "receipt_001",
  "notes": {"user_id": "12345", "subscription": "monthly"},
  "status": "created",
  "created_at": "2026-01-09T12:00:00Z"
}
```

**Indexes**:
```sql
CREATE INDEX idx_orders_merchant_id ON orders(merchant_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
```

---

### 3. PAYMENTS Table

**Purpose**: Track all payment attempts with method-specific details

```sql
CREATE TABLE payments (
  -- Primary Key
  id VARCHAR(64) PRIMARY KEY,  -- pay_XXXXX format
  
  -- Foreign Keys
  order_id VARCHAR(64) NOT NULL,
  CONSTRAINT fk_payments_order FOREIGN KEY (order_id)
    REFERENCES orders(id) ON DELETE CASCADE,
  
  merchant_id UUID NOT NULL,
  CONSTRAINT fk_payments_merchant FOREIGN KEY (merchant_id)
    REFERENCES merchants(id) ON DELETE CASCADE,
  
  -- Payment Details
  amount INTEGER NOT NULL,
  currency VARCHAR(3) DEFAULT 'INR',
  method VARCHAR(20) NOT NULL,  -- 'upi' or 'card'
  
  -- UPI-Specific Fields
  vpa VARCHAR(255),  -- Virtual Payment Address (username@bank)
  
  -- Card-Specific Fields
  card_network VARCHAR(20),  -- Visa, Mastercard, Amex, RuPay
  card_last4 VARCHAR(4),  -- Only last 4 digits for security
  
  -- Status & Errors
  status VARCHAR(20) DEFAULT 'processing',  -- processing, success, failed
  error_code VARCHAR(50),  -- Error classification
  error_description TEXT,  -- Human-readable error message
  
  -- Timestamps
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

**Columns Explained**:
| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| `id` | VARCHAR(64) | PK | Unique payment ID (pay_ABC123...) |
| `order_id` | VARCHAR(64) | NOT NULL, FK | Links to Orders table |
| `merchant_id` | UUID | NOT NULL, FK | Links to Merchants table |
| `amount` | INTEGER | NOT NULL | Amount in paise |
| `currency` | VARCHAR(3) | DEFAULT 'INR' | Currency code |
| `method` | VARCHAR(20) | NOT NULL | Payment method (upi, card) |
| `vpa` | VARCHAR(255) | UPI only | UPI ID (e.g., user@icici) |
| `card_network` | VARCHAR(20) | Card only | Card type detected |
| `card_last4` | VARCHAR(4) | Card only | Last 4 digits only (PCI compliance) |
| `status` | VARCHAR(20) | DEFAULT 'processing' | State (processing, success, failed) |
| `error_code` | VARCHAR(50) | On failure | Error classification |
| `error_description` | TEXT | On failure | Error details |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Payment creation time |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update time |

**Sample Data - UPI Payment**:
```json
{
  "id": "pay_ABC123DEF456",
  "order_id": "order_ABC123DEF456",
  "merchant_id": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 50000,
  "currency": "INR",
  "method": "upi",
  "vpa": "user@icici",
  "status": "success",
  "created_at": "2026-01-09T12:01:00Z"
}
```

**Sample Data - Card Payment**:
```json
{
  "id": "pay_XYZ789ABC123",
  "order_id": "order_XYZ789ABC123",
  "merchant_id": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 100000,
  "currency": "INR",
  "method": "card",
  "card_network": "Visa",
  "card_last4": "0366",
  "status": "success",
  "created_at": "2026-01-09T12:05:00Z"
}
```

**Sample Data - Failed Payment**:
```json
{
  "id": "pay_FAILED001",
  "order_id": "order_FAILED001",
  "merchant_id": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 75000,
  "currency": "INR",
  "method": "card",
  "card_network": "Mastercard",
  "card_last4": "3442",
  "status": "failed",
  "error_code": "INVALID_CARD",
  "error_description": "Card number failed Luhn validation",
  "created_at": "2026-01-09T12:10:00Z"
}
```

**Indexes**:
```sql
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_merchant_id ON payments(merchant_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at DESC);
```

---

## Schema Constraints & Relationships

### Foreign Key Constraints

```sql
-- Orders → Merchants
ALTER TABLE orders
ADD CONSTRAINT fk_orders_merchant FOREIGN KEY (merchant_id)
REFERENCES merchants(id) ON DELETE CASCADE;

-- Payments → Orders
ALTER TABLE payments
ADD CONSTRAINT fk_payments_order FOREIGN KEY (order_id)
REFERENCES orders(id) ON DELETE CASCADE;

-- Payments → Merchants
ALTER TABLE payments
ADD CONSTRAINT fk_payments_merchant FOREIGN KEY (merchant_id)
REFERENCES merchants(id) ON DELETE CASCADE;
```

**Cascade Behavior**:
- Deleting a Merchant cascades to Orders and Payments
- Deleting an Order cascades to related Payments
- Ensures data integrity and cleanup

### Unique Constraints

```sql
-- Merchant API credentials must be globally unique
ALTER TABLE merchants
ADD CONSTRAINT uq_merchants_email UNIQUE (email),
ADD CONSTRAINT uq_merchants_api_key UNIQUE (api_key);
```

---

## Indexes for Performance

```sql
-- Merchant lookups
CREATE INDEX idx_merchants_email ON merchants(email);
CREATE INDEX idx_merchants_api_key ON merchants(api_key);

-- Order filtering by merchant
CREATE INDEX idx_orders_merchant_id ON orders(merchant_id);

-- Order status queries
CREATE INDEX idx_orders_status ON orders(status);

-- Order time-series queries
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);

-- Payment queries by order
CREATE INDEX idx_payments_order_id ON payments(order_id);

-- Payment queries by merchant
CREATE INDEX idx_payments_merchant_id ON payments(merchant_id);

-- Payment status filtering
CREATE INDEX idx_payments_status ON payments(status);

-- Payment time-series queries
CREATE INDEX idx_payments_created_at ON payments(created_at DESC);
```

**Index Rationale**:
- API Key/Secret lookups (authentication) → `idx_merchants_api_key`
- Dashboard transactions filter → `idx_payments_merchant_id`, `idx_payments_status`
- Payment polling → `idx_payments_order_id`
- Time-series analytics → `idx_*_created_at`

---

## Data Flow Through Schema

### Order Creation
```
1. Client sends: POST /api/v1/orders
   ├─ Validate amount >= 100
   └─► INSERT into ORDERS (merchant_id, amount, status='created')

2. System generates: order_ABC123...
3. Return: order_id to client
```

### Payment Processing
```
1. Checkout page: POST /api/v1/payments/public
   ├─ Fetch from ORDERS WHERE id = ?
   ├─ Validate amount matches
   └─► INSERT into PAYMENTS (order_id, merchant_id, method, status='processing')

2. Validation Layer:
   ├─ If UPI: Validate VPA regex
   └─ If Card: Luhn + Network + Expiry

3. Processing Simulation:
   └─ Sleep 5-10 seconds

4. Update Payment:
   ├─ Random: ~90% success, ~10% failure
   └─► UPDATE PAYMENTS SET status='success'|'failed'

5. Polling:
   └─► Client: GET /api/v1/payments/{id}/public
       └─ FETCH from PAYMENTS WHERE id = ?
```

### Dashboard Display
```
1. Client: GET /api/v1/payments (authenticated)
   ├─ Extract merchant_id from auth
   └─► SELECT * FROM PAYMENTS WHERE merchant_id = ? AND status='success'

2. Aggregate Stats:
   ├─► SELECT COUNT(*) FROM PAYMENTS WHERE merchant_id = ? (total)
   ├─► SELECT SUM(amount) FROM PAYMENTS WHERE merchant_id = ? (volume)
   └─► Calculate success_rate = success_count / total_count
```

---

## Security Considerations

### Data Protection
1. **Card Numbers**: Never stored
   - Only card_network (Visa, MC, etc.) stored
   - Only card_last4 (last 4 digits) stored
   - Full card number exists only in memory during validation

2. **API Secrets**: Stored in plain text (for demo)
   - Production: Hash with bcrypt
   - Production: Use environment variables, AWS Secrets Manager, etc.

3. **VPA**: Fully stored (required for payment processing)

### Access Control
1. **API Authentication**: X-Api-Key + X-Api-Secret headers
2. **Public Endpoints**: No auth required for checkout
3. **Dashboard**: Email-based login (no password for demo)

### Query Optimization
- Indexes prevent full table scans
- Foreign keys ensure referential integrity
- Timestamps allow audit trails

---

## Schema Evolution (Future)

### Potential Additions
```sql
-- Webhook event tracking
CREATE TABLE webhook_events (
  id UUID PRIMARY KEY,
  merchant_id UUID REFERENCES merchants(id),
  event_type VARCHAR(50),  -- payment.success, payment.failed
  payload JSONB,
  status VARCHAR(20),  -- pending, delivered, failed
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Settlement tracking
CREATE TABLE settlements (
  id UUID PRIMARY KEY,
  merchant_id UUID REFERENCES merchants(id),
  amount INTEGER,
  status VARCHAR(20),  -- pending, completed
  settled_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Refund tracking
CREATE TABLE refunds (
  id UUID PRIMARY KEY,
  payment_id VARCHAR(64) REFERENCES payments(id),
  amount INTEGER,
  status VARCHAR(20),  -- pending, completed, failed
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Sample Queries

### Get Merchant's Statistics
```sql
SELECT
  m.id,
  m.email,
  COUNT(DISTINCT p.id) as total_payments,
  SUM(p.amount) as total_volume,
  COUNT(CASE WHEN p.status = 'success' THEN 1 END) as successful_payments,
  ROUND(100.0 * COUNT(CASE WHEN p.status = 'success' THEN 1 END) 
        / COUNT(p.id), 2) as success_rate
FROM merchants m
LEFT JOIN payments p ON m.id = p.merchant_id
WHERE m.id = ?
GROUP BY m.id, m.email;
```

### Get Recent Payments for Dashboard
```sql
SELECT p.id, p.order_id, p.amount, p.method, p.status, p.created_at
FROM payments p
WHERE p.merchant_id = ?
ORDER BY p.created_at DESC
LIMIT 50;
```

### Get Payment Status (Real-time Polling)
```sql
SELECT id, status, error_code, error_description, updated_at
FROM payments
WHERE id = ? AND order_id = ?;
```

### Find Orders by Merchant
```sql
SELECT o.id, o.amount, o.status, o.created_at
FROM orders o
WHERE o.merchant_id = ?
ORDER BY o.created_at DESC;
```

---

## Backup & Recovery

### Backup
```bash
docker exec pg_monochromepay pg_dump -U gateway_user payment_gateway > backup.sql
```

### Restore
```bash
docker exec -i pg_monochromepay psql -U gateway_user payment_gateway < backup.sql
```

### Data Persistence
- PostgreSQL volume: `payment_gateway_db_data` (Docker managed)
- Data persists across container restarts
- Use `docker volume inspect` to locate physical storage

---

## Monitoring & Maintenance

### Check Database Health
```sql
-- Connected clients
SELECT count(*) FROM pg_stat_activity;

-- Largest tables
SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) 
FROM pg_tables 
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Missing indexes
SELECT schemaname, tablename, attname 
FROM pg_stat_user_tables pst 
JOIN pg_attribute pa ON pst.relid = pa.attrelid;
```

### Regular Maintenance
```sql
-- Analyze query performance
ANALYZE;

-- Vacuum to reclaim space
VACUUM ANALYZE;

-- Check index health
REINDEX;
```

---

**Last Updated**: January 9, 2026
**Database Version**: PostgreSQL 15
**Schema Version**: 1.0
