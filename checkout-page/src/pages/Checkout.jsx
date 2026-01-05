import React, { useEffect, useMemo, useState } from 'react';
import axios from 'axios';

const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8000';
const client = axios.create({ baseURL });

function Checkout() {
  const [order, setOrder] = useState(null);
  const [method, setMethod] = useState('');
  const [vpa, setVpa] = useState('');
  const [card, setCard] = useState({ number: '', expiry: '', cvv: '', name: '' });
  const [paymentId, setPaymentId] = useState('');
  const [status, setStatus] = useState('idle');
  const [error, setError] = useState('');

  const orderId = useMemo(() => {
    const params = new URLSearchParams(window.location.search);
    return params.get('order_id');
  }, []);

  useEffect(() => {
    const loadOrder = async () => {
      if (!orderId) {
        setError('Missing order_id');
        return;
      }
      try {
        const res = await client.get(`/api/v1/orders/${orderId}/public`);
        setOrder(res.data);
      } catch (err) {
        setError('Order not found');
      }
    };
    loadOrder();
  }, [orderId]);

  useEffect(() => {
    let interval;
    if (paymentId && status === 'processing') {
      interval = setInterval(async () => {
        try {
          const res = await client.get(`/api/v1/payments/${paymentId}/public`);
          const nextStatus = res.data.status;
          setStatus(nextStatus);
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

  const amountDisplay = order ? `₹${(order.amount / 100).toFixed(2)}` : '';

  const submitUpi = async (e) => {
    e.preventDefault();
    setError('');
    setStatus('processing');
    try {
      const res = await client.post('/api/v1/payments/public', {
        order_id: orderId,
        method: 'upi',
        vpa
      });
      setPaymentId(res.data.id);
    } catch (err) {
      setStatus('failed');
      setError(err?.response?.data?.error?.description || 'Payment could not be processed');
    }
  };

  const submitCard = async (e) => {
    e.preventDefault();
    setError('');
    setStatus('processing');
    const [expiry_month = '', expiry_year = ''] = (card.expiry || '').split('/');
    try {
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
      setPaymentId(res.data.id);
    } catch (err) {
      setStatus('failed');
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
