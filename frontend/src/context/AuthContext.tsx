import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import type { ReactNode } from 'react';
import type { AxiosError } from 'axios';
import api from '../services/api';

export interface User {
  id: string;
  name: string;
  email: string;
  realm_access?: { roles: string[] };
  given_name?: string;
  family_name?: string;
  preferred_username?: string;
  firstName?: string;
  lastName?: string;
}

export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  roles: string[];
  permissions: string[];
  account: { authorities: string[]; permissions: string[]; firstName?: string; lastName?: string; email?: string } | null;
  loading: boolean;

  login: (username: string, password?: string) => Promise<{success: boolean, error?: string, requiresBrowserLogin?: boolean}>;
  continueLoginInBrowser: (redirectPath?: string) => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
  hasPermission: (permission: string) => boolean;
  hasAnyPermission: (permissions: string[]) => boolean;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true); // Empieza cargando para verificar sesión
  const [user, setUser] = useState<User | null>(null);
  const [roles, setRoles] = useState<string[]>([]);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [account, setAccount] = useState<{ authorities: string[]; permissions: string[]; firstName?: string; lastName?: string; email?: string } | null>(null);

  /**
   * Obtiene los datos de la sesión activa desde /api/account (BFF).
   * Si hay sesión válida en el backend (cookie de sesión), retorna el usuario real con sus roles efectivos.
   * Si no hay sesión, lanza error 401.
   */
  const fetchAccount = useCallback(async () => {
    try {
      const response = await api.get('/api/account');
      const acc = response.data;

      setIsAuthenticated(true);
      setAccount({
        authorities: acc.authorities || [],
        permissions: acc.permissions || [],
        firstName: acc.firstName,
        lastName: acc.lastName,
        email: acc.email,
      });
      setRoles(acc.authorities || []);
      setPermissions(acc.permissions || []);
      setUser({
        id: acc.id?.toString() || acc.login,
        name: `${acc.firstName || ''} ${acc.lastName || ''}`.trim() || acc.login,
        email: acc.email || '',
        preferred_username: acc.login,
        firstName: acc.firstName,
        lastName: acc.lastName,
      });

      return true;
    } catch {
      // No hay sesión activa (401) o error de red
      setIsAuthenticated(false);
      setUser(null);
      setRoles([]);
      setPermissions([]);
      setAccount(null);
      return false;
    }
  }, []);

  // Al montar el provider, intentar recuperar la sesión existente
  useEffect(() => {
    const init = async () => {
      setLoading(true);
      await fetchAccount();
      setLoading(false);
    };
    init();
  }, [fetchAccount]);

  const login = async (username: string, password?: string) => {
    setLoading(true);
    try {
      // El login real va por /api/authenticate (BFF) que crea la sesión
      await api.post('/api/authenticate', { username, password });

      // Después de autenticar, obtener los datos reales del account
      const success = await fetchAccount();
      setLoading(false);

      if (success) {
        return { success: true };
      }
      return { success: false, error: 'No se pudo cargar la cuenta después del login' };
    } catch (err) {
      const axiosError = err as AxiosError<{ detail?: string; requiresBrowserLogin?: boolean }>;
      setLoading(false);
      return {
        success: false,
        error: axiosError.response?.data?.detail || 'Error al iniciar sesión',
        requiresBrowserLogin: Boolean(axiosError.response?.data?.requiresBrowserLogin),
      };
    }
  };

  const continueLoginInBrowser = (redirectPath = '/login') => {
    const redirectUri = new URL(redirectPath, window.location.origin).toString();
    window.location.assign(`/api/authenticate/browser?redirect_uri=${encodeURIComponent(redirectUri)}`);
  };

  const logout = async () => {
    try {
      await api.post('/api/logout');
    } catch {
      // Ignorar — limpiar estado local de todas formas
    }
    try {
      localStorage.removeItem('activeConsultation');
    } catch {
      void 0;
    }
    setIsAuthenticated(false);
    setUser(null);
    setRoles([]);
    setPermissions([]);
    setAccount(null);
  };

  const hasRole = (role: string) => {
    return roles.includes(role);
  };

  const hasAnyRole = (requiredRoles: string[]) => {
    if (requiredRoles.length === 0) return true;
    return requiredRoles.some((role) => roles.includes(role));
  };

  const hasPermission = (permission: string) => {
    return permissions.includes(permission);
  };

  const hasAnyPermission = (requiredPermissions: string[]) => {
    if (requiredPermissions.length === 0) return true;
    return requiredPermissions.some((permission) => permissions.includes(permission));
  };

  return (
      <AuthContext.Provider
      value={{
        isAuthenticated,
        user,
        roles,
        permissions,
        account,
        loading,
        login,
        continueLoginInBrowser,
        logout,
        hasRole,
        hasAnyRole,
        hasPermission,
        hasAnyPermission,
      }}
      >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthState => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
