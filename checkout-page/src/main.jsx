import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './pages/Checkout';
import './styles.css';

// Initialize theme from localStorage at startup
const initialTheme = (() => {
  try {
    const saved = window.localStorage.getItem('theme');
    return saved === 'light' || saved === 'dark' ? saved : 'dark';
  } catch {
    return 'dark';
  }
})();
document.documentElement.setAttribute('data-theme', initialTheme);

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
