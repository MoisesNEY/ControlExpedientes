import { navigationConfig } from '../config/navigation';

export interface AccessRequirement {
  requiredRoles?: string[];
  requiredPermissions?: string[];
}

export const canAccessRequirement = (
  roles: string[],
  permissions: string[],
  { requiredRoles = [], requiredPermissions = [] }: AccessRequirement,
) => {
  const hasRoleRestriction = requiredRoles.length > 0;
  const hasPermissionRestriction = requiredPermissions.length > 0;

  if (!hasRoleRestriction && !hasPermissionRestriction) {
    return true;
  }

  const roleAllowed = hasRoleRestriction && requiredRoles.some(role => roles.includes(role));
  const permissionAllowed = hasPermissionRestriction && requiredPermissions.some(permission => permissions.includes(permission));

  return roleAllowed || permissionAllowed;
};

export const resolveFirstAccessiblePath = (roles: string[], permissions: string[], pathPrefix?: string) => {
  for (const group of navigationConfig) {
    const firstAllowedItem = group.items.find(item =>
      (!pathPrefix || item.path.startsWith(pathPrefix)) &&
      canAccessRequirement(roles, permissions, item),
    );

    if (firstAllowedItem) {
      return firstAllowedItem.path;
    }
  }

  return '/unauthorized';
};
