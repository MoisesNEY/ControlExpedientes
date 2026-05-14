import { resolveFirstAccessiblePath } from './accessControl';

export const resolveAuthorizedHomePath = (roles: string[], permissions: string[]) => {
  return resolveFirstAccessiblePath(roles, permissions);
};

export const resolveAuthorizedModulePath = (roles: string[], permissions: string[], pathPrefix: string) => {
  return resolveFirstAccessiblePath(roles, permissions, pathPrefix);
};
