import api from './api';
import { downloadBlob, getFilenameFromDisposition } from '../utils/download';

export interface PermissionDefinition {
  code: string;
  label: string;
  description: string;
}

export interface RoleDefinition {
  roleName: string;
  description: string;
  systemRole: boolean;
  compositeRoles: string[];
  permissions: string[];
}

export interface RoleManagementCatalog {
  availableCompositeRoles: string[];
  permissions: PermissionDefinition[];
}

export interface RolePayload {
  roleName: string;
  description: string;
  compositeRoles: string[];
  permissions: string[];
}

export interface ManagedUser {
  id: string;
  login: string;
  firstName: string;
  lastName: string;
  email: string;
  activated: boolean;
  roles: string[];
  requiredActions: string[];
}

export interface ManagedUserPayload {
  login: string;
  firstName: string;
  lastName: string;
  email: string;
  activated: boolean;
  roles: string[];
  password?: string;
  temporaryPassword: boolean;
  requiredActions: string[];
}

export const AUTH_REQUIRED_ACTIONS = [
  { value: 'UPDATE_PASSWORD', label: 'Solicitar cambio de contraseña al iniciar sesión' },
  { value: 'VERIFY_EMAIL', label: 'Solicitar verificación de correo' },
  { value: 'UPDATE_PROFILE', label: 'Solicitar actualización de perfil' },
] as const;

export const AdminSecurityService = {
  getRoles: async (): Promise<RoleDefinition[]> => {
    const response = await api.get<RoleDefinition[]>('/api/admin/roles');
    return response.data;
  },

  getRoleCatalog: async (): Promise<RoleManagementCatalog> => {
    const response = await api.get<RoleManagementCatalog>('/api/admin/roles/catalog');
    return response.data;
  },

  createRole: async (payload: RolePayload): Promise<RoleDefinition> => {
    const response = await api.post<RoleDefinition>('/api/admin/roles', payload);
    return response.data;
  },

  updateRole: async (roleName: string, payload: RolePayload): Promise<RoleDefinition> => {
    const response = await api.put<RoleDefinition>(`/api/admin/roles/${encodeURIComponent(roleName)}`, payload);
    return response.data;
  },

  deleteRole: async (roleName: string): Promise<void> => {
    await api.delete(`/api/admin/roles/${encodeURIComponent(roleName)}`);
  },

  exportRoles: async (): Promise<void> => {
    const response = await api.get('/api/admin/roles/export', { responseType: 'blob' });
    downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? 'roles.xlsx');
  },

  getUsers: async (): Promise<ManagedUser[]> => {
    const response = await api.get<ManagedUser[]>('/api/admin/users');
    return response.data;
  },

  createUser: async (payload: ManagedUserPayload): Promise<ManagedUser> => {
    const response = await api.post<ManagedUser>('/api/admin/users', payload);
    return response.data;
  },

  updateUser: async (userId: string, payload: ManagedUserPayload): Promise<ManagedUser> => {
    const response = await api.put<ManagedUser>(`/api/admin/users/${encodeURIComponent(userId)}`, payload);
    return response.data;
  },

  deleteUser: async (userId: string): Promise<void> => {
    await api.delete(`/api/admin/users/${encodeURIComponent(userId)}`);
  },

  exportUsers: async (): Promise<void> => {
    const response = await api.get('/api/admin/users/export', { responseType: 'blob' });
    downloadBlob(response.data, getFilenameFromDisposition(response.headers['content-disposition']) ?? 'usuarios.xlsx');
  },
};
