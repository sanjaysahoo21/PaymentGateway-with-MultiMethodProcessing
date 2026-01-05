import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../App';
import { createClient } from '../api';

function Dashboard() {
  const { auth, setAuth } = useContext(AuthContext);
  const navigate = useNavigate();
  const [stats, setStats] = useState({ total: 0, amount: 0, successRate: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const client = createClient(auth);
        const res = await client.get('/api/v1/payments');
        const payments = res.data || [];
        const total = payments.length;
        const successful = payments.filter((p) => p.status === 'success');
        const amount = successful.reduce((sum, p) => sum + (p.amount || 0), 0);
        const successRate = total === 0 ? 0 : Math.round((successful.length / total) * 100);
        setStats({ total, amount, successRate });
      } catch (err) {
        setStats({ total: 0, amount: 0, successRate: 0 });
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [auth]);

  const formatAmount = (amt) => `₹${(amt / 100).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;

  return (
    <div className="page" data-test-id="dashboard">
      <div className="nav">
        <button onClick={() => navigate('/dashboard')}>Home</button>
        <button onClick={() => navigate('/dashboard/transactions')}>Transactions</button>
        <button onClick={() => setAuth(null)}>Logout</button>
      </div>

      <div className="card" data-test-id="api-credentials">
        <h3>API Credentials</h3>
        <div>
          <label>API Key</label>
          <span data-test-id="api-key">{auth?.apiKey}</span>
        </div>
        <div>
          <label>API Secret</label>
          <span data-test-id="api-secret">{auth?.apiSecret}</span>
        </div>
      </div>

      <div className="card" data-test-id="stats-container">
        <h3>Stats</h3>
        {loading ? <p>Loading...</p> : (
          <div className="stats-grid">
            <div data-test-id="total-transactions">{stats.total}</div>
            <div data-test-id="total-amount">{formatAmount(stats.amount)}</div>
            <div data-test-id="success-rate">{stats.successRate}%</div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Dashboard;
