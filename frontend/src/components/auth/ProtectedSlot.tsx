import React from 'react';
import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { UnauthorizedView } from '../layout/UnauthorizedView';

interface ProtectedSlotProps {
  requiredRoles?: string[];
  children: ReactNode;
}

/**
 * Componente Wrapper para garantizar la seguridad a nivel de Frontend.
 * Valida de forma estructurada que el usuario esté autenticado y posea los roles.
 */
export const ProtectedSlot: React.FC<ProtectedSlotProps> = ({ requiredRoles = [], children }) => {
  const { isAuthenticated, hasAnyRole, loading } = useAuth();

  // Mientras se comprueba la sesión (fetch inicial), no renderizamos nada
  if (loading) return null;

  // Si no está autenticado, redirigimos al login
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Si está autenticado pero no tiene los roles necesarios, mostramos la vista de acceso denegado
  if (requiredRoles.length > 0 && !hasAnyRole(requiredRoles)) {
    return <UnauthorizedView />;
  }

  // Cumple las políticas, renderizamos la vista objetivo.
  return <>{children}</>;
};
