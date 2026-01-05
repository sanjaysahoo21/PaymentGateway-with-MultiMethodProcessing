import React, { useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../App';
import { fetchTestMerchant } from '../api';

/**
 * Login page component for merchant authentication.
 * Validates credentials against the test merchant endpoint.
 * On success, stores API key/secret in AuthContext and navigates to dashboard.
 * 
 * Note: This uses email validation only (no password validation for demo purposes).
 * In production, this would validate against a proper authentication service.
 * 
 * @returns {React.ReactNode} Login form with email/password inputs
 */
function Login() {
  const { setAuth } = useContext(AuthContext);
  const navigate = useNavigate();
  // Pre-filled with test merchant email for demo convenience
  const [email, setEmail] = useState('test@example.com');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  /**
   * Handle login form submission.
   * Fetches test merchant from backend, validates email, and stores credentials.
   * @param {React.FormEvent} e - Form submission event
   */
  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      // Fetch test merchant credentials from backend
      const merchant = await fetchTestMerchant();
      // Simple email validation (no real password check in demo)
      if (email.trim().toLowerCase() !== (merchant.email || '').toLowerCase()) {
        setError('Invalid email');
        return;
      }
      // Store API credentials in auth context
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
    <div className="page">
      <h2>Merchant Login</h2>
      <form data-test-id="login-form" onSubmit={onSubmit} className="card">
        <input
          data-test-id="email-input"
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <input
          data-test-id="password-input"
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <button data-test-id="login-button" type="submit">Login</button>
        {error && <p className="error">{error}</p>}
      </form>
    </div>
  );
}

export default Login;
