import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import DoctorDashboard from './pages/DoctorDashboard';
import Login from './pages/Login';

function App() {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-slate-50">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/doctor"
          element={isAuthenticated ? <DoctorDashboard /> : <Navigate to="/login" replace />}
        />
        <Route path="/" element={<Navigate to="/doctor" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
