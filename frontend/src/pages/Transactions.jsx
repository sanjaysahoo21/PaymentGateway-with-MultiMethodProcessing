import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../App';
import { createClient } from '../api';
function Transactions() {
  const { auth, setAuth } = useContext(AuthContext);
  const navigate = useNavigate();
  const [payments, setPayments] = useState([]);

  useEffect(() => {
    const load = async () => {
      try {
        const client = createClient(auth);
        const res = await client.get('/api/v1/payments');
        setPayments(res.data || []);
      } catch (err) {
        setPayments([]);
      }
    };
    load();
  }, [auth]);

  const formatAmount = (amt) => (amt ? amt : 0);

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
