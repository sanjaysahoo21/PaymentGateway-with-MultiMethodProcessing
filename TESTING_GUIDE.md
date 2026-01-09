# Testing Guide - Payment Gateway

## Step 1: Check All Services are Running

```powershell
docker-compose ps
```

You should see 4 services running:
- `postgres` (Database) - Port 5432
- `api` (Backend) - Port 8000
- `dashboard` (Merchant Dashboard) - Port 3000
- `checkout` (Payment Checkout) - Port 3001

## Step 2: Get Test Merchant Credentials

Open browser or use curl:
```powershell
curl http://localhost:8000/api/v1/test/merchant
```

**Expected Response:**
```json
{
  "email": "test@example.com",
  "api_key": "key_test_abc123",
  "api_secret": "secret_test_xyz789"
}
```

## Step 3: Create an Order (As Merchant)

```powershell
curl -X POST http://localhost:8000/api/v1/orders `
  -H "Content-Type: application/json" `
  -H "X-Api-Key: key_test_abc123" `
  -H "X-Api-Secret: secret_test_xyz789" `
  -d '{\"amount\": 50000, \"currency\": \"INR\", \"receipt\": \"receipt_001\"}'
```

**Expected Response:**
```json
{
  "id": "order_XXXXXXXXXXXXXXXX",
  "merchant_id": "...",
  "amount": 50000,
  "currency": "INR",
  "status": "created",
  ...
}
```

**Save the `order_id` from response!**

## Step 4: Test the Dashboard

1. Open browser: http://localhost:3000
2. Login with email: `test@example.com`
3. You should see:
   - API Key and Secret
   - Order list with your created order
   - Transaction history (empty for now)

## Step 5: Test Checkout Page (UPI Payment)

1. Open browser: `http://localhost:3001/checkout?order_id=<YOUR_ORDER_ID>`
2. You'll see the payment form with amount ₹500.00
3. Enter UPI ID: `test@upi`
4. Click "Pay Now"
5. Wait 5-10 seconds (simulated processing)
6. See success/failure result

## Step 6: Test Checkout Page (Card Payment)

1. Open browser: `http://localhost:3001/checkout?order_id=<NEW_ORDER_ID>`
2. Select "Card" payment method
3. Enter test card:
   - **Card Number:** `4111111111111111` (Visa)
   - **Expiry Month:** `12`
   - **Expiry Year:** `2027`
   - **CVV:** `123`
   - **Name:** `Test User`
4. Click "Pay Now"
5. See payment result

## Step 7: Verify Payment in Dashboard

1. Go back to dashboard: http://localhost:3000
2. Click "Transactions" tab
3. You should see your payment attempts with status (success/failed)

## Step 8: Test API - Get Payment Details

```powershell
curl http://localhost:8000/api/v1/payments/<PAYMENT_ID> `
  -H "X-Api-Key: key_test_abc123" `
  -H "X-Api-Secret: secret_test_xyz789"
```

## Step 9: Test API - List All Payments

```powershell
curl http://localhost:8000/api/v1/payments `
  -H "X-Api-Key: key_test_abc123" `
  -H "X-Api-Secret: secret_test_xyz789"
```

---

## Test Card Numbers

| Network | Card Number | Result |
|---------|-------------|--------|
| Visa | 4111111111111111 | Valid |
| Mastercard | 5555555555554444 | Valid |
| Amex | 378282246310005 | Valid |
| Invalid | 1234567890123456 | Fails Luhn check |

## Test UPI IDs

- `test@upi` - Valid
- `user@paytm` - Valid
- `merchant@okhdfcbank` - Valid
- `invalid-vpa` - Invalid format

---

## Troubleshooting

**Services not starting?**
```powershell
docker-compose logs api
docker-compose logs postgres
```

**Port already in use?**
```powershell
# Stop all services
docker-compose down

# Check what's using the ports
netstat -ano | findstr :8000
netstat -ano | findstr :3000
```

**Restart everything:**
```powershell
docker-compose down
docker-compose up -d
```

**View live logs:**
```powershell
docker-compose logs -f api
```
