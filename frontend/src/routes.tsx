import React from 'react';
import type { RouteObject } from 'react-router-dom';
import { Navigate } from 'react-router-dom';
import { MainLayout } from './components/layout/MainLayout';
import { ProtectedSlot } from './components/auth/ProtectedSlot';
import { useAuth } from './context/AuthContext';

// Importaciones de Vistas y Dashboards
import DoctorDashboard from './pages/DoctorDashboard';
import DoctorHomeView from './components/dashboard/views/DoctorHomeView';
import DoctorConsultationView from './components/dashboard/views/DoctorConsultationView';
import PatientListView from './components/dashboard/views/PatientListView';
import AppointmentView from './components/dashboard/views/AppointmentView';
import InventoryView from './components/dashboard/views/InventoryView';
import RecordsView from './components/dashboard/views/RecordsView';
import LabResultsView from './components/dashboard/views/LabResultsView';

import AdminDashboard from './pages/AdminDashboard';
import AdminHomeView from './components/admin/views/AdminHomeView';
import AdminPacientesView from './components/admin/views/AdminPacientesView';
import AdminMedicamentosView from './components/admin/views/AdminMedicamentosView';
import AdminCitasView from './components/admin/views/AdminCitasView';
import AdminExpedientesView from './components/admin/views/AdminExpedientesView';
import AdminAuditoriaView from './components/admin/views/AdminAuditoriaView';
import AdminInteraccionesView from './components/admin/views/AdminInteraccionesView';
import DiagnosticoCatalogView from './components/shared/views/DiagnosticoCatalogView';

import NurseDashboard from './pages/NurseDashboard';
import NurseHomeView from './components/nurse/views/NurseHomeView';
import TriageView from './components/nurse/views/TriageView';
import WaitingRoomView from './components/nurse/views/WaitingRoomView';

import ReceptionDashboard from './pages/ReceptionDashboard';
import ReceptionHomeView from './components/reception/views/ReceptionHomeView';
import ReceptionAgendaView from './components/reception/views/ReceptionAgendaView';
import ReceptionExpedientesView from './components/reception/views/ReceptionExpedientesView';

import Unauthorized from './pages/Unauthorized';
import Login from './pages/Login';

/**
 * Componente dinámico para la ruta raíz ("/").
 * Evalúa los roles desde AuthContext y redirige al dashboard correspondiente.
 */
const RootRedirect: React.FC = () => {
  const { isAuthenticated, hasRole, loading } = useAuth();

  // Evitar redirecciones prematuras hasta que se confirme el estado de autenticación
  if (loading) return null;

  if (!isAuthenticated) return <Navigate to="/login" replace />;

  if (hasRole('ROLE_ADMIN')) return <Navigate to="/admin/dashboard" replace />;
  if (hasRole('ROLE_MEDICO')) return <Navigate to="/medico/dashboard" replace />;
  if (hasRole('ROLE_ENFERMERO')) return <Navigate to="/enfermeria/dashboard" replace />;
  if (hasRole('ROLE_RECEPCION')) return <Navigate to="/recepcion/dashboard" replace />;

  return <Navigate to="/unauthorized" replace />;
};

export const routerConfig: RouteObject[] = [
  {
    path: '/login',
    element: <Login />
  },
  {
    path: '/',
    element: <MainLayout />,
    children: [
      {
        index: true,
        element: <RootRedirect />
      },
      {
        path: 'unauthorized',
        element: <Unauthorized />
      },
      {
        path: 'admin',
        element: (
          <ProtectedSlot requiredRoles={['ROLE_ADMIN']}>
            <AdminDashboard />
          </ProtectedSlot>
        ),
        children: [
          { index: true, element: <Navigate to="dashboard" replace /> },
          { path: 'dashboard', element: <AdminHomeView /> },
          { path: 'pacientes', element: <AdminPacientesView /> },
          { path: 'medicamentos', element: <AdminMedicamentosView /> },
          { path: 'interacciones', element: <AdminInteraccionesView /> },
          { path: 'diagnosticos', element: <DiagnosticoCatalogView /> },
          { path: 'citas', element: <AdminCitasView /> },
          { path: 'expedientes', element: <AdminExpedientesView /> },
          { path: 'auditoria', element: <AdminAuditoriaView /> }
        ]
      },
      {
        path: 'medico',
        element: (
          <ProtectedSlot requiredRoles={['ROLE_ADMIN', 'ROLE_MEDICO']}>
            <DoctorDashboard />
          </ProtectedSlot>
        ),
        children: [
          { index: true, element: <Navigate to="dashboard" replace /> },
          { path: 'dashboard', element: <DoctorHomeView /> },
          { path: 'consulta/:citaId', element: <DoctorConsultationView /> },
          { path: 'diagnosticos', element: <DiagnosticoCatalogView /> },
          { path: 'pacientes', element: <PatientListView /> },
          { path: 'citas', element: <AppointmentView /> },
          { path: 'inventario', element: <InventoryView /> },
          { path: 'registros', element: <RecordsView /> },
          { path: 'laboratorio', element: <LabResultsView /> }
        ]
      },
      {
        path: 'enfermeria',
        element: (
          <ProtectedSlot requiredRoles={['ROLE_ADMIN', 'ROLE_ENFERMERO']}>
            <NurseDashboard />
          </ProtectedSlot>
        ),
        children: [
          { index: true, element: <Navigate to="dashboard" replace /> },
          { path: 'dashboard', element: <NurseHomeView /> },
          { path: 'sala-espera', element: <WaitingRoomView /> },
          { path: 'triage/:citaId', element: <TriageView /> },
          { path: 'inventario', element: <InventoryView /> }
        ]
      },
      {
        path: 'recepcion',
        element: (
          <ProtectedSlot requiredRoles={['ROLE_ADMIN', 'ROLE_RECEPCION']}>
            <ReceptionDashboard />
          </ProtectedSlot>
        ),
        children: [
          { index: true, element: <Navigate to="dashboard" replace /> },
          { path: 'dashboard', element: <ReceptionHomeView /> },
          { path: 'pacientes', element: <AdminPacientesView /> },
          { path: 'expedientes', element: <ReceptionExpedientesView /> },
          { path: 'citas', element: <ReceptionAgendaView /> }
        ]
      },
      {
        path: '*',
        element: <Navigate to="/" replace />
      }
    ]
  }
];
