/**
 * Payment Gateway Database Schema
 * PostgreSQL 15 with JSON support
 * 
 * Tables:
 * - merchants: Payment merchants integrated with the gateway
 * - orders: Sales orders requiring payment
 * - payments: Payment attempts against orders
 * 
 * All timestamps are stored in UTC with time zone information.
 * Indexes optimize query performance for common lookup patterns.
 */

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

/**
 * Merchants table - stores payment merchant accounts
 * 
 * Columns:
 * - id: UUID primary key, auto-generated
 * - name: Merchant display name
 * - email: Unique email for merchant identification
 * - api_key: Public API key for header authentication
 * - api_secret: Secret key for header authentication
 * - webhook_url: Optional URL for async payment notifications (future use)
 * - is_active: Boolean flag to enable/disable merchant
 * - created_at: Account creation timestamp (UTC)
 * - updated_at: Last modification timestamp (UTC)
 */
CREATE TABLE IF NOT EXISTS merchants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    api_key VARCHAR(64) NOT NULL UNIQUE,
    api_secret VARCHAR(64) NOT NULL,
    webhook_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

/**
 * Orders table - stores payment orders
 * 
 * Columns:
 * - id: Unique order identifier (order_XXXXXXXXXXXXXXXX format)
 * - merchant_id: Foreign key to merchants table
 * - amount: Order amount in paise (minimum 100)
 * - currency: Currency code (default: INR)
 * - receipt: Merchant's receipt identifier
 * - notes: JSON field for arbitrary merchant metadata
 * - status: Order status (created, processing, success, failed)
 * - created_at: Order creation timestamp (UTC)
 * - updated_at: Last modification timestamp (UTC)
 */
CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(64) PRIMARY KEY,
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    amount INTEGER NOT NULL CHECK (amount >= 100),
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    receipt VARCHAR(255),
    notes JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'created',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

/**
 * Payments table - stores payment attempts
 * 
 * Columns:
 * - id: Unique payment identifier (pay_XXXXXXXXXXXXXXXX format)
 * - order_id: Foreign key to orders table
 * - merchant_id: Foreign key to merchants table (denormalized for query optimization)
 * - amount: Payment amount in paise
 * - currency: Currency code (default: INR)
 * - method: Payment method (upi or card)
 * - status: Payment status (processing, success, failed)
 * - vpa: UPI Virtual Payment Address (populated for UPI payments only)
 * - card_network: Card brand (visa, mastercard, amex, rupay) - card payments only
 * - card_last4: Last 4 digits of card number - card payments only (full number never stored)
 * - error_code: Error code if payment failed
 * - error_description: Human-readable error message if failed
 * - created_at: Payment creation timestamp (UTC)
 * - updated_at: Last modification/status change timestamp (UTC)
 * 
 * SECURITY NOTE: Card numbers are NEVER stored. Only card_network and card_last4 are persisted.
 */
CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(64) PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL REFERENCES orders(id),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    amount INTEGER NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'processing',
    vpa VARCHAR(255),
    card_network VARCHAR(20),
    card_last4 VARCHAR(4),
    error_code VARCHAR(50),
    error_description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

/**
 * Performance Indexes
 * 
 * Index on merchant_id for orders - used by:
 * - Fetching orders owned by a specific merchant
 * 
 * Index on order_id for payments - used by:
 * - Fetching payments for a specific order
 * 
 * Index on payment status - used by:
 * - Queries filtering by success/failed/processing status
 */
CREATE INDEX IF NOT EXISTS idx_orders_merchant_id ON orders (merchant_id);
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments (order_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments (status);
