import React from 'react';
import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { UnauthorizedView } from '../layout/UnauthorizedView';

interface ProtectedSlotProps {
  requiredRoles?: string[];
  requiredPermissions?: string[];
  children: ReactNode;
}

/**
 * Componente Wrapper para garantizar la seguridad a nivel de Frontend.
 * Valida de forma estructurada que el usuario esté autenticado y posea los roles.
 */
export const ProtectedSlot: React.FC<ProtectedSlotProps> = ({ requiredRoles = [], requiredPermissions = [], children }) => {
  const { isAuthenticated, hasAnyRole, hasAnyPermission, loading } = useAuth();

  // Mientras se comprueba la sesión (fetch inicial), no renderizamos nada
  if (loading) return null;

  // Si no está autenticado, redirigimos al login
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Si está autenticado pero no tiene los roles necesarios, mostramos la vista de acceso denegado
  const roleAllowed = requiredRoles.length > 0 && hasAnyRole(requiredRoles);
  const permissionAllowed = requiredPermissions.length > 0 && hasAnyPermission(requiredPermissions);
  const hasRestrictions = requiredRoles.length > 0 || requiredPermissions.length > 0;

  if (hasRestrictions && !roleAllowed && !permissionAllowed) {
    return <UnauthorizedView />;
  }

  // Cumple las políticas, renderizamos la vista objetivo.
  return <>{children}</>;
};
