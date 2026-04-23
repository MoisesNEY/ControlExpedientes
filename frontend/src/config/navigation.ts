export interface NavItem {
  id: string;
  label: string;
  path: string;
  icon: string;
  requiredRoles: string[];
  requiredPermissions?: string[];
}

export interface NavGroup {
  groupName: string;
  items: NavItem[];
}

/**
 * Matriz Universal de Navegación (System Inventory)
 * La visibilidad de cada ruta será gobernada por roles y permisos.
 */
export const navigationConfig: NavGroup[] = [
  {
    groupName: 'Administración',
    items: [
      { id: 'admin-dashboard', label: 'Dashboard', path: '/admin/dashboard', icon: 'dashboard', requiredRoles: ['ROLE_ADMIN'] },
      { id: 'admin-pacientes', label: 'Gestión Pacientes', path: '/admin/pacientes', icon: 'groups', requiredRoles: ['ROLE_ADMIN'] },
      { id: 'admin-meds', label: 'Medicamentos', path: '/admin/medicamentos', icon: 'medication', requiredRoles: ['ROLE_ADMIN'] },
      { id: 'admin-interacciones', label: 'Interacciones', path: '/admin/interacciones', icon: 'warning', requiredRoles: ['ROLE_ADMIN'] },
      { id: 'admin-diagnosticos', label: 'Diagnósticos', path: '/admin/diagnosticos', icon: 'diagnosis', requiredRoles: ['ROLE_ADMIN'] },
      { id: 'admin-citas', label: 'Gestión Citas', path: '/admin/citas', icon: 'calendar_month', requiredRoles: ['ROLE_ADMIN'] },
      { id: 'admin-expedientes', label: 'Expedientes', path: '/admin/expedientes', icon: 'folder_open', requiredRoles: ['ROLE_ADMIN'] },
      { id: 'admin-users', label: 'Usuarios', path: '/admin/usuarios', icon: 'badge', requiredRoles: ['ROLE_ADMIN'], requiredPermissions: ['admin.users.manage'] },
      { id: 'admin-roles', label: 'Roles', path: '/admin/roles', icon: 'admin_panel_settings', requiredRoles: ['ROLE_ADMIN'], requiredPermissions: ['admin.roles.manage'] },
      { id: 'admin-database', label: 'Base de Datos', path: '/admin/base-datos', icon: 'database', requiredRoles: ['ROLE_ADMIN'], requiredPermissions: ['admin.database.view', 'admin.database.export', 'admin.database.restore'] },
      { id: 'admin-audit', label: 'Auditoría', path: '/admin/auditoria', icon: 'security', requiredRoles: ['ROLE_ADMIN'] },
    ]
  },
  {
    groupName: 'Módulo Médico',
    items: [
      { id: 'med-dashboard', label: 'Dashboard', path: '/medico/dashboard', icon: 'dashboard', requiredRoles: ['ROLE_MEDICO'] },
      { id: 'med-pacientes', label: 'Mis Pacientes', path: '/medico/pacientes', icon: 'personal_injury', requiredRoles: ['ROLE_MEDICO'] },
      { id: 'med-citas', label: 'Mis Citas', path: '/medico/citas', icon: 'event', requiredRoles: ['ROLE_MEDICO'] },
      { id: 'med-diagnosticos', label: 'Diagnósticos', path: '/medico/diagnosticos', icon: 'diagnosis', requiredRoles: ['ROLE_MEDICO'] },
      { id: 'med-inventario', label: 'Sala Insumos', path: '/medico/inventario', icon: 'inventory', requiredRoles: ['ROLE_MEDICO'] },
      { id: 'med-registros', label: 'Mis Registros', path: '/medico/registros', icon: 'clinical_notes', requiredRoles: ['ROLE_MEDICO'] },
      { id: 'med-laboratorio', label: 'Laboratorio', path: '/medico/laboratorio', icon: 'science', requiredRoles: ['ROLE_MEDICO'] },
    ]
  },
  {
    groupName: 'Enfermería',
    items: [
      { id: 'enf-dashboard', label: 'Dashboard', path: '/enfermeria/dashboard', icon: 'dashboard', requiredRoles: ['ROLE_ENFERMERO'] },
      { id: 'enf-sala-espera', label: 'Sala de Espera', path: '/enfermeria/sala-espera', icon: 'hourglass_top', requiredRoles: ['ROLE_ENFERMERO'] },
      { id: 'enf-inventario', label: 'Insumos', path: '/enfermeria/inventario', icon: 'inventory_2', requiredRoles: ['ROLE_ENFERMERO'] },
    ]
  },
  {
    groupName: 'Recepción',
    items: [
      { id: 'rec-dashboard', label: 'Dashboard', path: '/recepcion/dashboard', icon: 'dashboard', requiredRoles: ['ROLE_RECEPCION'] },
      { id: 'rec-pacientes', label: 'Registro Base', path: '/recepcion/pacientes', icon: 'how_to_reg', requiredRoles: ['ROLE_RECEPCION'] },
      { id: 'rec-expedientes', label: 'Expedientes', path: '/recepcion/expedientes', icon: 'folder_shared', requiredRoles: ['ROLE_RECEPCION'] },
      { id: 'rec-citas', label: 'Agenda Citas', path: '/recepcion/citas', icon: 'book_online', requiredRoles: ['ROLE_RECEPCION'] },
    ]
  }
];
