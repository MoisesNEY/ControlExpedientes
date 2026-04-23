import { navigationConfig } from '../config/navigation';

const canAccessItem = (roles: string[], permissions: string[], requiredRoles: string[], requiredPermissions?: string[]) => {
  const hasRoleRestriction = requiredRoles.length > 0;
  const hasPermissionRestriction = (requiredPermissions?.length ?? 0) > 0;

  if (!hasRoleRestriction && !hasPermissionRestriction) {
    return true;
  }

  const roleAllowed = hasRoleRestriction && requiredRoles.some(role => roles.includes(role));
  const permissionAllowed = hasPermissionRestriction && (requiredPermissions ?? []).some(permission => permissions.includes(permission));

  return roleAllowed || permissionAllowed;
};

export const resolveAuthorizedHomePath = (roles: string[], permissions: string[]) => {
  for (const group of navigationConfig) {
    const firstAllowedItem = group.items.find(item => canAccessItem(roles, permissions, item.requiredRoles, item.requiredPermissions));
    if (firstAllowedItem) {
      return firstAllowedItem.path;
    }
  }

  return '/unauthorized';
};