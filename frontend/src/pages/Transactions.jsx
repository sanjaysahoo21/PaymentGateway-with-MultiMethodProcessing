import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../App';
import { createClient } from '../api';

/**
 * Transactions page component for displaying all merchant payments.
 * Shows a detailed table of all payments with status, amounts, and timestamps.
 * 
 * Displays columns:
 * - Payment ID (pay_XXXXXXXX format)
 * - Order ID (order_XXXXXXXX format)
 * - Amount in paise
 * - Payment method (upi or card)
 * - Status (processing, success, failed)
 * - Creation timestamp
 * 
 * Data is loaded on component mount using authenticated client.
 * Provides navigation back to dashboard and logout functionality.
 * 
 * @returns {React.ReactNode} Transactions table with payment details
 */
function Transactions() {
  const { auth, setAuth } = useContext(AuthContext);
  const navigate = useNavigate();
  const [payments, setPayments] = useState([]);

  /**
   * Load all merchant payments on component mount.
   */
  useEffect(() => {
    const load = async () => {
      try {
        // Create authenticated API client with merchant credentials
        const client = createClient(auth);
        // Fetch all payments for this merchant
        const res = await client.get('/api/v1/payments');
        setPayments(res.data || []);
      } catch (err) {
        // On error, show empty table
        setPayments([]);
      }
    };
    load();
  }, [auth]);

  /**
   * Format amount in paise. Returns amount as-is (let backend handle conversion).
   * @param {number} amt - Amount in paise
   * @returns {number} Amount value
   */
  const formatAmount = (amt) => (amt ? amt : 0);

  /**
   * Format timestamp to local date/time string.
   * @param {string} d - ISO timestamp string
   * @returns {string} Formatted date/time
   */
  const formatDate = (d) => d ? new Date(d).toLocaleString() : '';

  return (
    <div className="page">
      <div className="nav">
        <button onClick={() => navigate('/dashboard')}>Home</button>
        <button onClick={() => navigate('/dashboard/transactions')}>Transactions</button>
        <button onClick={() => setAuth(null)}>Logout</button>
      </div>
      <h2>Transactions</h2>
      <table data-test-id="transactions-table" className="table">
        <thead>
          <tr>
            <th>Payment ID</th>
            <th>Order ID</th>
            <th>Amount</th>
            <th>Method</th>
            <th>Status</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          {/* Render payment row for each transaction */}
          {payments.map((p) => (
            <tr key={p.id} data-test-id="transaction-row" data-payment-id={p.id}>
              <td data-test-id="payment-id">{p.id}</td>
              <td data-test-id="order-id">{p.order_id}</td>
              <td data-test-id="amount">{formatAmount(p.amount)}</td>
              <td data-test-id="method">{p.method}</td>
              <td data-test-id="status">{p.status}</td>
              <td data-test-id="created-at">{formatDate(p.created_at)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Transactions;
