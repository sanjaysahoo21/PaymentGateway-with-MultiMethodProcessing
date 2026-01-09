import React, { useMemo, useState } from 'react';
import { Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Transactions from './pages/Transactions';

export const AuthContext = React.createContext();
export const ThemeContext = React.createContext();

function App() {
  const navigate = useNavigate();
  const [auth, setAuth] = useState(() => {
    const saved = localStorage.getItem('merchantAuth');
    return saved ? JSON.parse(saved) : null;
  });
  // Dark-only theme (no light mode)
  const theme = 'dark';
  const toggleTheme = () => {};

  const value = useMemo(() => ({
    auth,
    setAuth: (data) => {
      setAuth(data);
      if (data) {
        localStorage.setItem('merchantAuth', JSON.stringify(data));
      } else {
        localStorage.removeItem('merchantAuth');
        navigate('/login');
      }
    }
  }), [auth, navigate]);

  return (
    <AuthContext.Provider value={value}>
      <ThemeContext.Provider value={{ theme, toggleTheme }}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/dashboard" element={
            <Protected auth={auth}>
              <Dashboard />
            </Protected>
          } />
          <Route path="/dashboard/transactions" element={
            <Protected auth={auth}>
              <Transactions />
            </Protected>
          } />
          <Route path="*" element={<Navigate to={auth ? '/dashboard' : '/login'} replace />} />
        </Routes>
      </ThemeContext.Provider>
    </AuthContext.Provider>
  );
}

/**
 * Protected route wrapper component.
 * Redirects unauthenticated users to login.
 * @param {Object} props
 * @param {Object} props.auth - Current auth state
 * @param {React.ReactNode} props.children - Route component to protect
 * @returns {React.ReactNode} Protected route or redirect
 */
function Protected({ auth, children }) {
  if (!auth) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

export default App;
