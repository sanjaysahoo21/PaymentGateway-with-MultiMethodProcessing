import React, { useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../App';
import { fetchTestMerchant } from '../api';

function Login() {
  const { setAuth } = useContext(AuthContext);
  const navigate = useNavigate();
  const [email, setEmail] = useState('test@example.com');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const merchant = await fetchTestMerchant();
      if (email.trim().toLowerCase() !== (merchant.email || '').toLowerCase()) {
        setError('Invalid email');
        return;
      }
      setAuth({
        email: merchant.email,
        apiKey: merchant.api_key,
        apiSecret: merchant.api_secret
      });
      navigate('/dashboard');
    } catch (err) {
      setError('Unable to login');
    }
  };

  return (
    <div className="page auth-page">
      <div className="topbar">
        <div className="brand">
          <span className="brand-dot" />
          <div>
            <span className="brand-name">Gateway</span>
            <span className="brand-sub">Payments Console</span>
          </div>
        </div>
        <div className="topbar-actions" />
      </div>

      <div className="auth-hero">
        <div>
          <p className="eyebrow">Merchant Console</p>
          <h1>Access your payments cockpit</h1>
          <p className="lede">Use the seeded test merchant to explore orders, payments, and dashboards with a clean Vercel-inspired interface.</p>
        </div>
        <div className="glow" />
      </div>

      <form data-test-id="login-form" onSubmit={onSubmit} className="card auth-card">
        <h3>Sign in</h3>
        <div className="input-row">
          <label>Email</label>
          <input
            data-test-id="email-input"
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>
        <div className="input-row">
          <label>Password</label>
          <input
            data-test-id="password-input"
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>
        <button data-test-id="login-button" type="submit">Login</button>
        {error && <p className="error">{error}</p>}
      </form>
    </div>
  );
}

export default Login;
