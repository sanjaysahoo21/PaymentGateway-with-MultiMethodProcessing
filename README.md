# MonochromePay — Minimalist Payment Gateway

A minimalist payment gateway simulation built with **Spring Boot**, **PostgreSQL**, and **React**. Supports merchant authentication, order management, multi-method payments (UPI & Card), and a hosted checkout experience.

Perfect for portfolio demonstration or learning payment system architecture.

---

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- (Optional) Java 17, Maven, Node.js for local development

### Run Locally

```bash
# Clone and navigate to project
cd PaymentGateway-with-MultiMethodProcessing

# Copy environment file with your database password
cp .env.example .env

# Start all services (PostgreSQL, API, Dashboard, Checkout)
docker-compose up
```

**Services will be available at:**
- 🔗 **API**: http://localhost:8000
- 📊 **Dashboard**: http://localhost:3000
- 🛒 **Checkout**: http://localhost:3001

---

## 📋 Test the Project

### 1. **Get Test Merchant Credentials**
```bash
curl http://localhost:8000/api/v1/test/merchant
```
Response:
```json
{
  "email": "test@example.com",
  "api_key": "key_test_abc123",
  "api_secret": "secret_test_xyz789"
}
```

### 2. **Create an Order**
```bash
curl -X POST http://localhost:8000/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: key_test_abc123" \
  -H "X-Api-Secret: secret_test_xyz789" \
  -d '{
    "amount": 50000,
    "currency": "INR",
    "receipt": "receipt_001"
  }'
```

### 3. **Access Dashboard**
- Navigate to http://localhost:3000
- Login with email: `test@example.com`
- View API credentials and transaction history

### 4. **Test Checkout**
- Create an order (get `order_id` from step 2)
- Navigate to: `http://localhost:3001/checkout?order_id=order_XXXXXXXX`
- Fill UPI or Card details and submit
- Watch the 2-second polling for payment status

---

## 🏗️ Architecture

### Backend (Java + Spring Boot 3)
- **REST API** with 10+ endpoints
- **API Key/Secret Authentication** via custom filter
- **Order Management** - create, retrieve, list orders
- **Payment Processing** - validate, simulate, and track payments
- **Validation Layer** - Luhn algorithm, VPA regex, card networks
- **Database** - PostgreSQL with auto-migration

### Frontend
- **Dashboard** (React) - Merchant login, stats, transactions
- **Checkout** (React) - Embedded payment form with polling
- **Vite** - Modern build tooling

### Database (PostgreSQL)
- **Merchants** table - Stores merchant accounts & API credentials
- **Orders** table - Payment orders with amounts & status
- **Payments** table - Payment attempts with method & result
- Proper indexing for query optimization

---

## 🔑 API Endpoints

### Authentication
All endpoints (except marked `/public`) require headers:
```
X-Api-Key: your_api_key
X-Api-Secret: your_api_secret
```

### Orders
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/v1/orders` | ✅ | Create new order |
| `GET` | `/api/v1/orders/{id}` | ✅ | Get order (merchant only) |
| `GET` | `/api/v1/orders/{id}/public` | ❌ | Get order for checkout |

### Payments
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/v1/payments` | ✅ | Create payment |
| `POST` | `/api/v1/payments/public` | ❌ | Create payment (checkout) |
| `GET` | `/api/v1/payments/{id}` | ✅ | Get payment (merchant only) |
| `GET` | `/api/v1/payments/{id}/public` | ❌ | Get payment (polling) |
| `GET` | `/api/v1/payments` | ✅ | List merchant payments |

### System
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/health` | ❌ | Health check |
| `GET` | `/api/v1/test/merchant` | ❌ | Get test credentials |

---

## ⚙️ Configuration

Create a `.env` file in project root (see `.env.example`):

```env
# Database
DB_PASSWORD=your_postgres_password

# Test Merchant (optional)
TEST_MERCHANT_EMAIL=test@example.com
TEST_API_KEY=key_test_abc123
TEST_API_SECRET=secret_test_xyz789

# Payment Processing
UPI_SUCCESS_RATE=0.90
CARD_SUCCESS_RATE=0.95
PROCESSING_DELAY_MIN=5000
PROCESSING_DELAY_MAX=10000
TEST_MODE=false
```

---

## 📁 Project Structure

```
PaymentGateway-with-MultiMethodProcessing/
├── backend/                          # Spring Boot API
│   ├── src/main/java/com/gateway/
│   │   ├── controllers/             # REST endpoints
│   │   ├── services/                # Business logic
│   │   ├── models/                  # JPA entities
│   │   ├── repositories/            # Data access
│   │   ├── config/                  # Security & seeding
│   │   ├── util/                    # Validation utilities
│   │   └── exception/               # Error handling
│   ├── src/main/resources/
│   │   ├── schema.sql              # Database schema
│   │   └── application.properties   # Configuration
│   ├── pom.xml                      # Maven dependencies
│   └── Dockerfile                   # Multi-stage build
│
├── frontend/                        # Merchant Dashboard
│   ├── src/
│   │   ├── pages/                  # Login, Dashboard, Transactions
│   │   ├── App.jsx                 # Routing & auth context
│   │   ├── api.js                  # API client
│   │   └── styles.css              # Styling
│   ├── package.json
│   └── Dockerfile
│
├── checkout-page/                   # Hosted Checkout
│   ├── src/
│   │   ├── pages/Checkout.jsx      # Checkout form & polling
│   │   └── styles.css
│   ├── package.json
│   └── Dockerfile
│
├── docker-compose.yml               # Service orchestration
├── .env.example                     # Environment template
└── README.md                        # This file
```

---

## 🔐 Security Features

- ✅ **API Key/Secret Authentication** - Prevents unauthorized access
- ✅ **No Card Storage** - Only last 4 digits + network stored
- ✅ **Password Protection** - PostgreSQL user permissions
- ✅ **HTTPS Ready** - Stateless design for reverse proxy
- ✅ **Merchant Isolation** - Cross-merchant access prevented
- ✅ **Input Validation** - VPA, Luhn, expiry date checks

---

## 💳 Payment Methods Supported

### UPI (Unified Payments Interface)
- VPA format validation (username@bankname)
- Success rate: 90% (configurable)
- Instant processing

### Card (Credit/Debit)
- **Validation**: Luhn algorithm (13-19 digits)
- **Networks**: Visa (4xxx), Mastercard (51-55xx), Amex (34/37xx), RuPay (60/65/81-89xx)
- **Expiry**: Month/Year validation
- **Security**: Full card number never stored
- Success rate: 95% (configurable)

---

## 🧪 Test Mode

For rapid testing during development, enable test mode:

```env
TEST_MODE=true
TEST_PAYMENT_SUCCESS=true
TEST_PROCESSING_DELAY=1000
```

This:
- ✅ Guarantees payment success
- ✅ Reduces delay to 1 second
- ✅ Allows deterministic testing

---

## 📊 Database Schema

### Merchants
```sql
id (UUID) | name | email | api_key | api_secret | is_active | created_at | updated_at
```

### Orders
```sql
id | merchant_id (FK) | amount | currency | receipt | notes (JSON) | status | created_at | updated_at
```

### Payments
```sql
id | order_id (FK) | merchant_id (FK) | amount | method | status | vpa | card_network | card_last4 | error_code | error_description | created_at | updated_at
```

---

## 🛠️ Development

### Local Development (without Docker)

**Backend:**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Dashboard:**
```bash
cd frontend
npm install
npm run dev
```

**Checkout:**
```bash
cd checkout-page
npm install
npm run dev
```

---

## 📝 Key Features

- ✅ **32 Commits** - Well-documented development history
- ✅ **Comprehensive Javadoc** - All classes and methods documented
- ✅ **Type-Safe** - No Lombok, explicit constructors
- ✅ **Error Handling** - Standardized exception responses
- ✅ **Realistic Simulation** - Configurable delays & success rates
- ✅ **Production-Ready** - Docker, env vars, security best practices
- ✅ **Responsive UI** - Mobile-friendly frontend

---

## 🚀 Deployment

### Using Docker Compose
```bash
docker-compose up -d
```

### Using Kubernetes
Update image references in your deployment manifests and apply.

### Environment Variables
All sensitive data (passwords, API keys) should be injected via environment variables, never hardcoded.

---

## 📞 Support & Notes

- **Test Merchant**: Auto-seeded on startup at `test@example.com`
- **Database**: PostgreSQL 15 with pgcrypto extension
- **Java Version**: 17 (LTS)
- **Spring Boot**: 3.2.2
- **React**: 18 with Vite

---

## 📄 License

This project is for educational and portfolio purposes.

---

**Built with ❤️ for learning payment system architecture**
