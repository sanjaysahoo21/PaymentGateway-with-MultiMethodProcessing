import React, { useEffect, useMemo, useState } from 'react';
import axios from 'axios';

// API client configuration
const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8000';
const client = axios.create({ baseURL });

/**
 * Checkout page component for processing payments.
 * Supports two payment methods: UPI (VPA) and Card.
 * 
 * Features:
 * - Load order details from order_id query parameter
 * - Display order amount and currency
 * - Accept UPI VPA address or card details
 * - Submit payment to backend
 * - Poll payment status every 2 seconds until completion
 * - Display success/failure messages and error details
 * 
 * This is a public page (no authentication required).
 * Used as hosted checkout embedded in merchant websites.
 * 
 * @returns {React.ReactNode} Checkout form with payment method selection
 */
function Checkout() {
  // Order details fetched from backend
  const [order, setOrder] = useState(null);
  // Selected payment method: 'upi' or 'card'
  const [method, setMethod] = useState('');
  // UPI-specific: Virtual Payment Address
  const [vpa, setVpa] = useState('');
  // Card-specific: Card details
  const [card, setCard] = useState({ number: '', expiry: '', cvv: '', name: '' });
  // Payment ID returned after submission (used for polling)
  const [paymentId, setPaymentId] = useState('');
  // Payment processing state: idle, processing, success, failed
  const [status, setStatus] = useState('idle');
  // Error message to display
  const [error, setError] = useState('');

  /**
   * Extract order_id from URL query parameters.
   * Format: ?order_id=order_XXXXXXXX
   */
  const orderId = useMemo(() => {
    const params = new URLSearchParams(window.location.search);
    return params.get('order_id');
  }, []);

  /**
   * Load order details from public endpoint on component mount.
   * Order ID is extracted from URL query parameters.
   */
  useEffect(() => {
    const loadOrder = async () => {
      if (!orderId) {
        setError('Missing order_id');
        return;
      }
      try {
        // Fetch order without authentication (public endpoint)
        const res = await client.get(`/api/v1/orders/${orderId}/public`);
        setOrder(res.data);
      } catch (err) {
        setError('Order not found');
      }
    };
    loadOrder();
  }, [orderId]);

  /**
   * Poll payment status every 2 seconds after payment submission.
   * Stops polling when payment reaches final status (success or failed).
   */
  useEffect(() => {
    let interval;
    if (paymentId && status === 'processing') {
      interval = setInterval(async () => {
        try {
          // Poll payment status from public endpoint
          const res = await client.get(`/api/v1/payments/${paymentId}/public`);
          const nextStatus = res.data.status;
          setStatus(nextStatus);
          // Stop polling when payment reaches terminal state
          if (nextStatus !== 'processing') {
            clearInterval(interval);
          }
        } catch (err) {
          clearInterval(interval);
          setStatus('failed');
          setError('Payment could not be processed');
        }
      }, 2000);
    }
    return () => interval && clearInterval(interval);
  }, [paymentId, status]);

  /**
   * Format order amount from paise to INR display.
   * @returns {string} Formatted amount with rupee symbol
   */
  const amountDisplay = order ? `₹${(order.amount / 100).toFixed(2)}` : '';

  /**
   * Submit UPI payment with VPA address.
   * Sends payment request to public endpoint and starts polling.
   * @param {React.FormEvent} e - Form submission event
   */
  const submitUpi = async (e) => {
    e.preventDefault();
    setError('');
    setStatus('processing');
    try {
      // Submit UPI payment request
      const res = await client.post('/api/v1/payments/public', {
        order_id: orderId,
        method: 'upi',
        vpa
      });
      // Store payment ID for polling
      setPaymentId(res.data.id);
    } catch (err) {
      setStatus('failed');
      // Display error from backend response or generic message
      setError(err?.response?.data?.error?.description || 'Payment could not be processed');
    }
  };

  /**
   * Submit card payment with card details.
   * Parses expiry MM/YY format to separate month/year.
   * @param {React.FormEvent} e - Form submission event
   */
  const submitCard = async (e) => {
    e.preventDefault();
    setError('');
    setStatus('processing');
    // Parse expiry MM/YY format
    const [expiry_month = '', expiry_year = ''] = (card.expiry || '').split('/');
    try {
      // Submit card payment request
      const res = await client.post('/api/v1/payments/public', {
        order_id: orderId,
        method: 'card',
        card: {
          number: card.number,
          expiry_month,
          expiry_year,
          cvv: card.cvv,
          holder_name: card.name
        }
      });
      // Store payment ID for polling
      setPaymentId(res.data.id);
    } catch (err) {
      setStatus('failed');
      // Display error from backend response or generic message
      setError(err?.response?.data?.error?.description || 'Payment could not be processed');
    }
  };

  const reset = () => {
    setStatus('idle');
    setError('');
    setPaymentId('');
  };

  return (
    <div className="page" data-test-id="checkout-container">
      <div className="card" data-test-id="order-summary">
        <h2>Complete Payment</h2>
        <div>
          <span>Amount: </span>
          <span data-test-id="order-amount">{amountDisplay}</span>
        </div>
        <div>
          <span>Order ID: </span>
          <span data-test-id="order-id">{order?.id}</span>
        </div>
      </div>

      <div className="card" data-test-id="payment-methods">
        <button data-test-id="method-upi" data-method="upi" onClick={() => setMethod('upi')}>
          UPI
        </button>
        <button data-test-id="method-card" data-method="card" onClick={() => setMethod('card')}>
          Card
        </button>
      </div>

      <form data-test-id="upi-form" style={{ display: method === 'upi' ? 'block' : 'none' }} onSubmit={submitUpi} className="card">
        <input
          data-test-id="vpa-input"
          placeholder="username@bank"
          type="text"
          value={vpa}
          onChange={(e) => setVpa(e.target.value)}
        />
        <button data-test-id="pay-button" type="submit">
          Pay {amountDisplay}
        </button>
      </form>

      <form data-test-id="card-form" style={{ display: method === 'card' ? 'block' : 'none' }} onSubmit={submitCard} className="card">
        <input
          data-test-id="card-number-input"
          placeholder="Card Number"
          type="text"
          value={card.number}
          onChange={(e) => setCard({ ...card, number: e.target.value })}
        />
        <input
          data-test-id="expiry-input"
          placeholder="MM/YY"
          type="text"
          value={card.expiry}
          onChange={(e) => setCard({ ...card, expiry: e.target.value })}
        />
        <input
          data-test-id="cvv-input"
          placeholder="CVV"
          type="text"
          value={card.cvv}
          onChange={(e) => setCard({ ...card, cvv: e.target.value })}
        />
        <input
          data-test-id="cardholder-name-input"
          placeholder="Name on Card"
          type="text"
          value={card.name}
          onChange={(e) => setCard({ ...card, name: e.target.value })}
        />
        <button data-test-id="pay-button" type="submit">
          Pay {amountDisplay}
        </button>
      </form>

      <div data-test-id="processing-state" style={{ display: status === 'processing' ? 'block' : 'none' }} className="card">
        <div className="spinner" />
        <span data-test-id="processing-message">Processing payment...</span>
      </div>

      <div data-test-id="success-state" style={{ display: status === 'success' ? 'block' : 'none' }} className="card">
        <h2>Payment Successful!</h2>
        <div>
          <span>Payment ID: </span>
          <span data-test-id="payment-id">{paymentId}</span>
        </div>
        <span data-test-id="success-message">Your payment has been processed successfully</span>
      </div>

      <div data-test-id="error-state" style={{ display: status === 'failed' ? 'block' : 'none' }} className="card">
        <h2>Payment Failed</h2>
        <span data-test-id="error-message">{error || 'Payment could not be processed'}</span>
        <button data-test-id="retry-button" onClick={reset}>Try Again</button>
      </div>
    </div>
  );
}

export default Checkout;
