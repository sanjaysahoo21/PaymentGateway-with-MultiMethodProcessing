import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../App';
import { createClient } from '../api';

function Transactions() {
  const { auth, setAuth } = useContext(AuthContext);
  const navigate = useNavigate();
  const [payments, setPayments] = useState([]);
  const [theme, setTheme] = useState(() => {
    const saved = window.localStorage.getItem('theme');
    return saved === 'light' || saved === 'dark' ? saved : 'dark';
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    window.localStorage.setItem('theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((t) => (t === 'dark' ? 'light' : 'dark'));
  };

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
      <div className="topbar">
        <div className="brand" onClick={() => navigate('/dashboard')}>
          <span className="brand-dot" />
          <div>
            <span className="brand-name">Gateway</span>
            <span className="brand-sub">Payments Console</span>
          </div>
        </div>
        <div className="topbar-actions">
          <button className="icon-btn" aria-label="Toggle theme" onClick={toggleTheme}>
            {theme === 'dark' ? (
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 4V2M12 22v-2M4.93 4.93L3.51 3.51M20.49 20.49l-1.42-1.42M4 12H2M22 12h-2M4.93 19.07L3.51 20.49M20.49 3.51l-1.42 1.42" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                <circle cx="12" cy="12" r="4" stroke="currentColor" strokeWidth="2"/>
              </svg>
            ) : (
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" stroke="currentColor" strokeWidth="2"/>
              </svg>
            )}
          </button>
          <button className="ghost" onClick={() => navigate('/dashboard')}>
            <i className="fa-solid fa-chart-simple" />
            <span>Dashboard</span>
          </button>
          <button className="ghost" onClick={() => setAuth(null)}>
            <i className="fa-solid fa-arrow-right-from-bracket" />
            <span>Logout</span>
          </button>
        </div>
      </div>

      <div className="card table-card">
        <div className="panel-head">
          <div>
            <p className="eyebrow">Activity</p>
            <h3>Transactions</h3>
          </div>
          <i className="fa-solid fa-receipt" />
        </div>
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
    </div>
  );
}

export default Transactions;
