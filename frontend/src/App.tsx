import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { ProtectedRoute } from './components/layout/ProtectedRoute';

import DoctorDashboard from './pages/DoctorDashboard';
import AdminDashboard from './pages/AdminDashboard';
import NurseDashboard from './pages/NurseDashboard';
import ReceptionDashboard from './pages/ReceptionDashboard';
import Unauthorized from './pages/Unauthorized';
import Login from './pages/Login';

function App() {
  const { loading, account } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-slate-50 dark:bg-slate-900">
        <div className="flex flex-col items-center gap-4">
          <div className="animate-spin rounded-full h-12 w-12 border-4 border-slate-200 border-t-primary"></div>
          <span className="text-slate-500 font-medium">Cargando aplicación...</span>
        </div>
      </div>
    );
  }

  // Rutas por defecto basadas en el Rol principal
  const getDefaultRoute = () => {
    if (!account?.authorities) return '/login';
    if (account.authorities.includes('ROLE_ADMIN')) return '/admin';
    if (account.authorities.includes('ROLE_MEDICO')) return '/doctor';
    if (account.authorities.includes('ROLE_ENFERMERO')) return '/enfermeria';
    if (account.authorities.includes('ROLE_RECEPCION')) return '/recepcion';
    return '/unauthorized';
  };

  return (
    <Router>
      <Routes>
        {/* Rutas Públicas */}
        <Route path="/login" element={<Login />} />
        <Route path="/unauthorized" element={<Unauthorized />} />

        {/* Rutas Protegidas por Rol */}
        <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN']} />}>
          <Route path="/admin" element={<AdminDashboard />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_MEDICO']} />}>
          <Route path="/doctor" element={<DoctorDashboard />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_ENFERMERO']} />}>
          <Route path="/enfermeria" element={<NurseDashboard />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_RECEPCION']} />}>
          <Route path="/recepcion" element={<ReceptionDashboard />} />
        </Route>

        {/* Redirección Base */}
        <Route path="/" element={<Navigate to={getDefaultRoute()} replace />} />

        {/* Fallback 404 - Redirige al inicio para que re-evalúe los roles */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
