import React, { useMemo, useState } from 'react';
import { Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Transactions from './pages/Transactions';

/**
 * Global authentication context providing merchant auth state across the app.
 * Contains api_key and api_secret for authenticated API requests.
 */
export const AuthContext = React.createContext();

/**
 * Root application component managing routing and authentication state.
 * Implements role-based access control with ProtectedRoute wrapper.
 * 
 * Features:
 * - Persistent authentication via localStorage
 * - Route protection with auth check
 * - Automatic redirect to login for unauthenticated users
 * - Three main routes: login, dashboard, transactions
 */
function App() {
  const navigate = useNavigate();
  // Initialize auth state from localStorage on mount
  const [auth, setAuth] = useState(() => {
    const saved = localStorage.getItem('merchantAuth');
    return saved ? JSON.parse(saved) : null;
  });

  // Memoized auth context value with persistent storage
  const value = useMemo(() => ({
    auth,
    setAuth: (data) => {
      setAuth(data);
      if (data) {
        // Persist auth to localStorage when login succeeds
        localStorage.setItem('merchantAuth', JSON.stringify(data));
      } else {
        // Clear auth and redirect to login on logout
        localStorage.removeItem('merchantAuth');
        navigate('/login');
      }
    }
  }), [auth, navigate]);

  return (
    <AuthContext.Provider value={value}>
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
        {/* Default redirect based on auth state */}
        <Route path="*" element={<Navigate to={auth ? '/dashboard' : '/login'} replace />} />
      </Routes>
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
