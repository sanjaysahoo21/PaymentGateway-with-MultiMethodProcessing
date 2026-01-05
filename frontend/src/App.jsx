import React, { useMemo, useState } from 'react';
import { Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Transactions from './pages/Transactions';

export const AuthContext = React.createContext();

function App() {
  const navigate = useNavigate();
  const [auth, setAuth] = useState(() => {
    const saved = localStorage.getItem('merchantAuth');
    return saved ? JSON.parse(saved) : null;
  });

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
    </AuthContext.Provider>
  );
}

function Protected({ auth, children }) {
  if (!auth) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

export default App;
