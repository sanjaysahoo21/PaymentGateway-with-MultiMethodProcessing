import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../App';
import { createClient } from '../api';

/**
 * Dashboard page component for merchants.
 * Displays API credentials and summary statistics of all merchant payments.
 * 
 * Statistics shown:
 * - Total payment count
 * - Total amount of successful payments (in INR)
 * - Success rate percentage
 * 
 * Loads payment data on mount using authenticated client.
 * Provides navigation to transactions detail page and logout.
 * 
 * @returns {React.ReactNode} Dashboard with stats, credentials, and navigation
 */
function Dashboard() {
  const { auth, setAuth } = useContext(AuthContext);
  const navigate = useNavigate();
  // Stats state initialized with zeros
  const [stats, setStats] = useState({ total: 0, amount: 0, successRate: 0 });
  const [loading, setLoading] = useState(true);

  /**
   * Load payment statistics on component mount.
   * Calculates total payments, successful amount, and success rate.
   */
  useEffect(() => {
    const load = async () => {
      try {
        // Create authenticated API client with merchant credentials
        const client = createClient(auth);
        // Fetch all merchant payments
        const res = await client.get('/api/v1/payments');
        const payments = res.data || [];
        // Calculate statistics
        const total = payments.length;
        const successful = payments.filter((p) => p.status === 'success');
        const amount = successful.reduce((sum, p) => sum + (p.amount || 0), 0);
        const successRate = total === 0 ? 0 : Math.round((successful.length / total) * 100);
        setStats({ total, amount, successRate });
      } catch (err) {
        // On error, show zero stats
        setStats({ total: 0, amount: 0, successRate: 0 });
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [auth]);

  /**
   * Format amount in paise to INR with proper localization.
   * @param {number} amt - Amount in paise
   * @returns {string} Formatted amount with INR symbol
   */
  const formatAmount = (amt) => `₹${(amt / 100).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;

  return (
    <div className="page" data-test-id="dashboard">
      <div className="nav">
        <button onClick={() => navigate('/dashboard')}>Home</button>
        <button onClick={() => navigate('/dashboard/transactions')}>Transactions</button>
        <button onClick={() => setAuth(null)}>Logout</button>
      </div>

      {/* Display merchant API credentials */}
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
