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
      <div className="topbar">
        <div className="brand" onClick={() => navigate('/dashboard')}>
          <span className="brand-dot" />
          <div>
            <span className="brand-name">Gateway</span>
            <span className="brand-sub">Payments Console</span>
          </div>
        </div>
        <div className="topbar-actions">
          <button className="ghost" onClick={() => navigate('/dashboard/transactions')}>
            <i className="fa-solid fa-table" />
            <span>Transactions</span>
          </button>
          <button className="ghost" onClick={() => setAuth(null)}>
            <i className="fa-solid fa-arrow-right-from-bracket" />
            <span>Logout</span>
          </button>
        </div>
      </div>

      <div className="panel-grid">
        <div className="card api-card" data-test-id="api-credentials">
          <div className="panel-head">
            <div>
              <p className="eyebrow">API Access</p>
              <h3>Live Credentials</h3>
            </div>
            <i className="fa-solid fa-key" />
          </div>
          <div className="key-line">
            <label>API Key</label>
            <span data-test-id="api-key">{auth?.apiKey}</span>
          </div>
          <div className="key-line">
            <label>API Secret</label>
            <span data-test-id="api-secret">{auth?.apiSecret}</span>
          </div>
        </div>

        <div className="card stats-card" data-test-id="stats-container">
          <div className="panel-head">
            <div>
              <p className="eyebrow">Performance</p>
              <h3>Payment Pulse</h3>
            </div>
            <i className="fa-solid fa-chart-line" />
          </div>
          {loading ? <p>Loading...</p> : (
            <div className="stat-tiles">
              <div className="stat-tile">
                <div className="stat-icon success"><i className="fa-solid fa-bolt" /></div>
                <div className="stat-meta">
                  <p>Transactions</p>
                  <div data-test-id="total-transactions" className="stat-value">{stats.total}</div>
                </div>
              </div>
              <div className="stat-tile">
                <div className="stat-icon primary"><i className="fa-solid fa-indian-rupee-sign" /></div>
                <div className="stat-meta">
                  <p>Volume</p>
                  <div data-test-id="total-amount" className="stat-value">{formatAmount(stats.amount)}</div>
                </div>
              </div>
              <div className="stat-tile">
                <div className="stat-icon neutral"><i className="fa-solid fa-gauge-high" /></div>
                <div className="stat-meta">
                  <p>Success Rate</p>
                  <div data-test-id="success-rate" className="stat-value">{stats.successRate}%</div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
