# MonochromePay — Minimalist Payment Gateway

A full-featured payment gateway simulation built with **Spring Boot**, **PostgreSQL**, and **React**. Process payments via UPI and Card methods with a merchant dashboard, hosted checkout page, and comprehensive API.

**Perfect for:**
- Learning payment system architecture
- Portfolio demonstrations
- Testing payment workflows
- Understanding multi-method payment processing

---

## 📋 Table of Contents
1. [Features](#features)
2. [Tech Stack](#tech-stack)
3. [Quick Start](#quick-start)
4. [Services & Ports](#services--ports)
5. [How to Use](#how-to-use)
6. [API Documentation](#api-documentation)
7. [Test Credentials](#test-credentials)
8. [Project Structure](#project-structure)

---

## ✨ Features

### 🛡️ Authentication & Security
- API Key/Secret authentication for merchant requests
- Separate public endpoints for checkout (no auth required)
- Request validation and error handling

### 💳 Payment Methods
- **UPI**: VPA validation (e.g., `user@bank`)
- **Card**: Full Luhn algorithm validation + card network detection (Visa, Mastercard, Amex, RuPay)
- Card expiry validation
- Secure storage (only last 4 digits + network stored)

### 📊 Merchant Dashboard
- Login with email (no password required for demo)
- View live API credentials
- Track transaction stats (count, volume, success rate)
- View all payments in transaction history
- **Light/Dark theme toggle**

### 🛒 Hosted Checkout Page
- Embedded payment form
- Order details display
- Payment method selection (UPI/Card)
- Real-time payment status polling
- Success/Failure notifications
- **Light/Dark theme toggle**

### 🔄 Payment Processing
- Deterministic test mode (configurable success/failure)
- Configurable processing delay
- Status polling with 2-second intervals
- Payment state transitions (processing → success/failed)

### 📱 Responsive Design
- Mobile-friendly dashboard
- Responsive checkout page
- Clean, minimalist UI

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| **Backend** | Spring Boot 3.2.2 (Java 17) |
| **Database** | PostgreSQL 15 |
| **Frontend Dashboard** | React 18 + Vite |
| **Checkout Page** | React 18 + Vite |
| **Deployment** | Docker + Docker Compose |
| **Build Tools** | Maven (backend), NPM (frontend) |

---

## 🚀 Quick Start

### Prerequisites
- **Docker & Docker Compose** (recommended)
- OR: Java 17, Maven, PostgreSQL, Node.js (local development)

### Run with Docker (Recommended)

```bash
# Clone the repository
git clone https://github.com/your-repo/PaymentGateway-with-MultiMethodProcessing.git
cd PaymentGateway-with-MultiMethodProcessing

# Start all services
docker compose up -d

# Check logs
docker compose logs -f api
```

**Wait ~30 seconds for services to initialize.**

### Run Locally (Development)

```bash
# Backend
cd backend
mvn clean install
mvn spring-boot:run

# Frontend (in separate terminal)
cd frontend
npm install
npm run dev

# Checkout (in another terminal)
cd checkout-page
npm install
npm run dev
```

---

## 🌐 Services & Ports

| Service | Port | URL | Purpose |
|---------|------|-----|---------|
| **PostgreSQL** | 5432 | `localhost:5432` | Database |
| **API** | 8000 | `http://localhost:8000` | REST API endpoints |
| **Dashboard** | 3000 | `http://localhost:3000` | Merchant console |
| **Checkout** | 3001 | `http://localhost:3001` | Hosted payment page |

---

## 📱 How to Use

### 1️⃣ Merchant Dashboard

#### Access the Dashboard
```
http://localhost:3000
```

#### Login
- **Email**: `test@example.com`
- **Password**: Any value (demo mode, no validation)

#### Dashboard Features
- **API Credentials**: Copy your API Key and Secret
- **Payment Pulse**: View transaction stats
  - Total transactions processed
  - Total volume in INR
  - Success rate percentage
- **Transactions Table**: Browse all your payments
  - Payment ID
  - Order ID
  - Amount
  - Method (UPI/Card)
  - Status (processing/success/failed)
  - Created timestamp

#### Theme Toggle
Click the **sun/moon icon** in the top-right to switch between light and dark modes.

---

### 2️⃣ Create an Order (API)

**Endpoint**: `POST /api/v1/orders`

**Headers**:
```
X-Api-Key: key_test_abc123
X-Api-Secret: secret_test_xyz789
Content-Type: application/json
```

**Request Body**:
```json
{
  "amount": 50000,
  "currency": "INR",
  "receipt": "receipt_001"
}
```

**Response**:
```json
{
  "id": "order_ABC123DEF456",
  "merchant_id": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 50000,
  "currency": "INR",
  "status": "created",
  "created_at": "2026-01-09T12:00:00Z"
}
```

**Example with cURL**:
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

---

### 3️⃣ Hosted Checkout Page

#### Access Checkout
Use the `order_id` from step 2:
```
http://localhost:3001/?order_id=order_ABC123DEF456
```

#### Complete Payment via UPI
1. Click **UPI** button
2. Enter UPI ID (e.g., `user@icici`)
3. Click **Pay ₹500.00**
4. Watch status change to "Processing payment..."
5. After 5-10 seconds, see success/failure result

#### Complete Payment via Card
1. Click **Card** button
2. Fill card details:
   - **Card Number**: `4532015112830366` (test Visa)
   - **Expiry**: `12/26` (MM/YY format)
   - **CVV**: Any 3 digits
   - **Name**: Any name
3. Click **Pay ₹500.00**
4. See processing then success/failure

#### Test Card Numbers
- **Visa**: `4532015112830366`
- **Mastercard**: `5425233010103442`
- **Amex**: `374245455400126`
- **RuPay**: `6522500000000000`

#### Theme Toggle
Click the **sun/moon icon** in the top-right to switch between light and dark modes.

---

### 4️⃣ Check Payment Status (API)

**Endpoint**: `GET /api/v1/payments/{id}`

**Headers** (for authenticated access):
```
X-Api-Key: key_test_abc123
X-Api-Secret: secret_test_xyz789
```

**Response**:
```json
{
  "id": "pay_ABC123DEF456",
  "order_id": "order_ABC123DEF456",
  "amount": 50000,
  "method": "upi",
  "status": "success",
  "vpa": "user@icici",
  "created_at": "2026-01-09T12:00:00Z"
}
```

---

## 🔌 API Documentation

### Public Endpoints (No Auth Required)

#### GET `/health`
Health check endpoint.
```bash
curl http://localhost:8000/health
```

#### GET `/api/v1/orders/{id}/public`
Fetch order details without authentication.
```bash
curl http://localhost:8000/api/v1/orders/order_ABC123DEF456/public
```

#### POST `/api/v1/payments/public`
Create a payment without authentication (checkout page endpoint).
```bash
curl -X POST http://localhost:8000/api/v1/payments/public \
  -H "Content-Type: application/json" \
  -d '{
    "order_id": "order_ABC123DEF456",
    "method": "upi",
    "vpa": "user@icici"
  }'
```

#### GET `/api/v1/payments/{id}/public`
Poll payment status without authentication.
```bash
curl http://localhost:8000/api/v1/payments/pay_ABC123DEF456/public
```

---

### Authenticated Endpoints (Require API Key/Secret)

#### GET `/api/v1/test/merchant`
Get test merchant credentials.
```bash
curl http://localhost:8000/api/v1/test/merchant
```

#### POST `/api/v1/orders` *(as shown above)*

#### GET `/api/v1/orders/{id}`
Fetch your order.

#### GET `/api/v1/payments/{id}`
Fetch your payment details.

#### POST `/api/v1/payments`
Create a payment (authenticate access).

---

## 🔐 Test Credentials

### Merchant Account
| Field | Value |
|-------|-------|
| Email | `test@example.com` |
| API Key | `key_test_abc123` |
| API Secret | `secret_test_xyz789` |

### Test Card Numbers
- **Visa**: `4532015112830366`, Expiry: `12/26`, CVV: `100`
- **Mastercard**: `5425233010103442`, Expiry: `12/26`, CVV: `100`

### Test UPI IDs
- `user@icici`
- `merchant@ybl`
- `demo@upi`

---

## 📁 Project Structure

```
PaymentGateway-with-MultiMethodProcessing/
├── backend/                          # Spring Boot API
│   ├── src/main/java/com/gateway/
│   │   ├── config/                   # Security, exceptions, seeders
│   │   ├── controllers/              # REST endpoints
│   │   ├── dto/                      # Data transfer objects
│   │   ├── models/                   # JPA entities
│   │   ├── repositories/             # Database access
│   │   ├── services/                 # Business logic
│   │   └── util/                     # Validators, ID generator
│   ├── src/main/resources/
│   │   ├── schema.sql                # Database schema
│   │   └── application.properties    # Configuration
│   ├── Dockerfile
│   └── pom.xml                       # Maven dependencies
│
├── frontend/                         # Dashboard React app
│   ├── src/
│   │   ├── pages/
│   │   │   ├── Login.jsx             # Merchant login
│   │   │   ├── Dashboard.jsx         # Stats & credentials
│   │   │   └── Transactions.jsx      # Payment history
│   │   ├── styles.css                # Global styles + light/dark theme
│   │   ├── main.jsx                  # React entry
│   │   └── App.jsx                   # Router setup
│   ├── Dockerfile
│   ├── package.json
│   └── vite.config.js
│
├── checkout-page/                    # Checkout React app
│   ├── src/
│   │   ├── pages/
│   │   │   ├── Checkout.jsx          # Payment form
│   │   │   ├── Success.jsx           # Success page
│   │   │   └── Failure.jsx           # Failure page
│   │   ├── styles.css                # Checkout styles + light/dark theme
│   │   ├── main.jsx                  # React entry
│   │   └── App.jsx                   # Router setup
│   ├── Dockerfile
│   ├── package.json
│   └── vite.config.js
│
├── docker-compose.yml                # Orchestration
├── README.md                         # This file
└── .env.example                      # Environment template
```

---

## 🧪 Testing Workflow

### 1. Health Check
```bash
curl http://localhost:8000/health
```

### 2. Get Merchant Credentials
```bash
curl http://localhost:8000/api/v1/test/merchant
```

### 3. Create Order
```bash
ORDER=$(curl -s -X POST http://localhost:8000/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: key_test_abc123" \
  -H "X-Api-Secret: secret_test_xyz789" \
  -d '{"amount": 50000, "currency": "INR"}' | jq -r '.id')
echo $ORDER
```

### 4. Open Checkout
```
http://localhost:3001/?order_id=$ORDER
```

### 5. Complete Payment
- Select UPI or Card
- Enter test details
- Submit and watch status

### 6. View in Dashboard
```
http://localhost:3000
```
- Login: `test@example.com`
- See transaction in history

---

## 🔧 Configuration

### Environment Variables
Create `.env` from `.env.example`:

```env
# Database
POSTGRES_DB=payment_gateway
POSTGRES_USER=gateway_user
POSTGRES_PASSWORD=gateway_pass

# API
PORT=8000
DATABASE_URL=jdbc:postgresql://postgres:5432/payment_gateway

# Test Mode (optional)
TEST_MODE=true
TEST_PAYMENT_SUCCESS=true
TEST_PROCESSING_DELAY=3000
```

### Test Mode
- **TEST_MODE=true**: Enables deterministic payment outcomes
- **TEST_PAYMENT_SUCCESS=true**: Force all payments to succeed
- **TEST_PAYMENT_SUCCESS=false**: Force all payments to fail
- **TEST_PROCESSING_DELAY=ms**: Fixed delay instead of random

---

## 🚨 Troubleshooting

### Services not starting
```bash
docker compose down
docker compose up --build
```

### Database connection error
Ensure PostgreSQL health check passes:
```bash
docker compose logs postgres
```

### Payment creation fails
Check API credentials are correct:
```bash
curl http://localhost:8000/api/v1/test/merchant
```

### Checkout page blank
Ensure order_id is valid:
```bash
http://localhost:3001/?order_id=order_ABC123
```

---

## 📚 Learning Resources

- **Payment Validation**: `backend/src/main/java/com/gateway/util/ValidationUtil.java`
- **Payment Processing**: `backend/src/main/java/com/gateway/services/PaymentService.java`
- **Authentication Filter**: `backend/src/main/java/com/gateway/config/MerchantAuthenticationFilter.java`
- **Database Schema**: `backend/src/main/resources/schema.sql`

---

## 📝 License

This project is open-source and available for educational and portfolio purposes.

---

## 🤝 Contributing

Feel free to fork, modify, and improve this project for your portfolio!

---

**Built with ❤️ for payment system learning**
