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
