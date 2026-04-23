import { useEffect, useMemo, useState } from 'react';
import { AppButton } from '../../ui/AppButton';
import {
  AdminSecurityService,
  type PermissionDefinition,
  type RoleDefinition,
  type RolePayload,
} from '../../../services/admin-security.service';
import { getApiErrorMessage } from '../../../utils/apiError';

const emptyRoleForm: RolePayload = {
  roleName: 'ROLE_',
  description: '',
  compositeRoles: [],
  permissions: [],
};

const AdminRolesView = () => {
  const [roles, setRoles] = useState<RoleDefinition[]>([]);
  const [availableCompositeRoles, setAvailableCompositeRoles] = useState<string[]>([]);
  const [permissions, setPermissions] = useState<PermissionDefinition[]>([]);
  const [form, setForm] = useState<RolePayload>(emptyRoleForm);
  const [editingRole, setEditingRole] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deletingRole, setDeletingRole] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const loadData = async () => {
    setLoading(true);
    try {
      const [roleList, catalog] = await Promise.all([AdminSecurityService.getRoles(), AdminSecurityService.getRoleCatalog()]);
      setRoles(roleList);
      setAvailableCompositeRoles(catalog.availableCompositeRoles);
      setPermissions(catalog.permissions);
    } catch (error) {
      console.error('Error cargando roles:', error);
      setMessage('No se pudieron cargar los roles dinámicos.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadData();
  }, []);

  const editableRoles = useMemo(() => roles.filter(role => !role.systemRole), [roles]);
  const rolePermissionMap = useMemo(
    () => new Map(roles.map(role => [role.roleName, role.permissions])),
    [roles]
  );
  const inheritedPermissions = useMemo(
    () =>
      Array.from(
        new Set(
          form.compositeRoles.flatMap(roleName => rolePermissionMap.get(roleName) ?? [])
        )
      ),
    [form.compositeRoles, rolePermissionMap]
  );
  const inheritedPermissionSet = useMemo(() => new Set(inheritedPermissions), [inheritedPermissions]);
  const effectivePermissions = useMemo(
    () => Array.from(new Set([...inheritedPermissions, ...form.permissions])),
    [form.permissions, inheritedPermissions]
  );
  const effectivePermissionSet = useMemo(() => new Set(effectivePermissions), [effectivePermissions]);

  const toggleSelection = (collection: string[], value: string) =>
    collection.includes(value) ? collection.filter(item => item !== value) : [...collection, value];

  const startEditing = (role: RoleDefinition) => {
    setEditingRole(role.roleName);
    const inheritedRolePermissions = new Set(role.compositeRoles.flatMap(roleName => rolePermissionMap.get(roleName) ?? []));
    setForm({
      roleName: role.roleName,
      description: role.description ?? '',
      compositeRoles: role.compositeRoles,
      permissions: role.permissions.filter(permission => !inheritedRolePermissions.has(permission)),
    });
    setMessage(null);
  };

  const resetForm = () => {
    setEditingRole(null);
    setForm(emptyRoleForm);
  };

  const handleSubmit = async () => {
    setSaving(true);
    setMessage(null);
    try {
      const payload: RolePayload = {
        ...form,
        permissions: effectivePermissions,
      };
      if (editingRole) {
        await AdminSecurityService.updateRole(editingRole, payload);
        setMessage(`Rol ${editingRole} actualizado correctamente.`);
      } else {
        await AdminSecurityService.createRole(payload);
        setMessage(`Rol ${form.roleName} creado correctamente.`);
      }
      resetForm();
      await loadData();
    } catch (error) {
      console.error('Error guardando rol:', error);
      setMessage(await getApiErrorMessage(error, 'No se pudo guardar el rol. Verifica el nombre y la conexión con Keycloak.'));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (roleName: string) => {
    if (!window.confirm(`Se eliminará el rol ${roleName} también en Keycloak. ¿Deseas continuar?`)) {
      return;
    }

    setDeletingRole(roleName);
    setMessage(null);
    try {
      await AdminSecurityService.deleteRole(roleName);
      setMessage(`Rol ${roleName} eliminado correctamente.`);
      if (editingRole === roleName) {
        resetForm();
      }
      await loadData();
    } catch (error) {
      console.error('Error eliminando rol:', error);
      setMessage(await getApiErrorMessage(error, 'No se pudo eliminar el rol seleccionado.'));
    } finally {
      setDeletingRole(null);
    }
  };

  return (
    <div className="p-4 md:p-8 max-w-7xl mx-auto w-full flex flex-col gap-6 md:gap-8">
      <div className="flex flex-col gap-1">
        <h2 className="text-slate-900 dark:text-white text-3xl font-black tracking-tight">Gestión dinámica de roles</h2>
        <p className="text-slate-500 text-base font-medium">
          Cada rol se sincroniza en tiempo real con Keycloak y la lista siempre refleja el estado actual del proveedor de identidad.
        </p>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-[1.1fr_0.9fr] gap-6">
        <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm space-y-4">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h3 className="text-lg font-black text-slate-900 dark:text-white">Roles registrados</h3>
              <p className="text-sm text-slate-500">Los roles marcados como sistema se reflejan desde Keycloak y no se editan aquí.</p>
            </div>
            <AppButton variant="outline" icon="refresh" onClick={() => void loadData()}>
              Actualizar
            </AppButton>
          </div>

          {loading ? (
            <p className="text-sm text-slate-500">Cargando roles...</p>
          ) : (
            <div className="space-y-3">
              {roles.map(role => (
                <div key={role.roleName} className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-800/30 p-4">
                  <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-3">
                    <div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <p className="font-bold text-slate-900 dark:text-white">{role.roleName}</p>
                        <span className={`rounded-full px-2.5 py-1 text-[10px] font-black uppercase ${role.systemRole ? 'bg-violet-100 text-violet-700 dark:bg-violet-500/10 dark:text-violet-300' : 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-300'}`}>
                          {role.systemRole ? 'Sistema' : 'Dinámico'}
                        </span>
                      </div>
                      <p className="mt-1 text-sm text-slate-500">{role.description || 'Sin descripción configurada.'}</p>
                      <div className="mt-3 flex flex-wrap gap-2 text-xs text-slate-500">
                        {role.compositeRoles.map(item => (
                          <span key={`${role.roleName}-${item}`} className="rounded-full border border-slate-200 dark:border-slate-700 px-2.5 py-1 bg-white dark:bg-slate-900">
                            {item}
                          </span>
                        ))}
                        {role.permissions.map(permission => (
                          <span key={`${role.roleName}-${permission}`} className="rounded-full border border-sky-200 px-2.5 py-1 bg-sky-50 text-sky-700 dark:border-sky-500/30 dark:bg-sky-500/10 dark:text-sky-200">
                            {permission}
                          </span>
                        ))}
                      </div>
                    </div>

                    {!role.systemRole && (
                      <div className="flex flex-wrap gap-2">
                        <AppButton variant="outline" size="sm" icon="edit" onClick={() => startEditing(role)}>
                          Editar
                        </AppButton>
                        <AppButton
                          variant="danger"
                          size="sm"
                          icon="delete"
                          isLoading={deletingRole === role.roleName}
                          onClick={() => void handleDelete(role.roleName)}
                        >
                          Eliminar
                        </AppButton>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm space-y-5">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h3 className="text-lg font-black text-slate-900 dark:text-white">{editingRole ? `Editar ${editingRole}` : 'Crear rol dinámico'}</h3>
              <p className="text-sm text-slate-500">Define el nombre del rol, sus roles base y los permisos del panel.</p>
            </div>
            {editingRole && (
              <AppButton variant="ghost" icon="close" onClick={resetForm}>
                Cancelar
              </AppButton>
            )}
          </div>

          <div className="space-y-4">
            <div>
              <label className="text-xs font-black uppercase tracking-widest text-slate-500">Nombre del rol</label>
              <input
                type="text"
                disabled={Boolean(editingRole)}
                value={form.roleName}
                onChange={event => setForm(current => ({ ...current, roleName: event.target.value.toUpperCase() }))}
                className="mt-2 w-full rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-sky-500 disabled:opacity-60"
                placeholder="ROLE_COORDINACION"
              />
            </div>

            <div>
              <label className="text-xs font-black uppercase tracking-widest text-slate-500">Descripción</label>
              <textarea
                value={form.description}
                onChange={event => setForm(current => ({ ...current, description: event.target.value }))}
                className="mt-2 w-full min-h-24 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-sky-500"
                placeholder="Describe el propósito del rol."
              />
            </div>

            <div>
              <p className="text-xs font-black uppercase tracking-widest text-slate-500">Roles base compuestos</p>
              <p className="mt-1 text-xs text-slate-500">Puedes componer el rol con cualquier otro rol sincronizado desde Keycloak; sus permisos se marcan automáticamente.</p>
              <div className="mt-3 grid grid-cols-1 gap-2">
                {availableCompositeRoles.filter(roleName => roleName !== form.roleName).map(roleName => (
                  <label key={roleName} className="flex items-center gap-3 rounded-xl border border-slate-200 dark:border-slate-800 px-3 py-2.5 text-sm">
                    <input
                      type="checkbox"
                      checked={form.compositeRoles.includes(roleName)}
                      onChange={() =>
                        setForm(current => ({
                          ...current,
                          compositeRoles: toggleSelection(current.compositeRoles, roleName),
                        }))
                      }
                      className="h-4 w-4 accent-sky-500"
                    />
                    <span>{roleName}</span>
                  </label>
                ))}
              </div>
            </div>

            <div>
              <p className="text-xs font-black uppercase tracking-widest text-slate-500">Permisos</p>
              <div className="mt-3 space-y-2">
                {permissions.map(permission => (
                  <label key={permission.code} className="block rounded-2xl border border-slate-200 dark:border-slate-800 px-4 py-3">
                    <div className="flex items-start gap-3">
                      <input
                        type="checkbox"
                        checked={effectivePermissionSet.has(permission.code)}
                        disabled={inheritedPermissionSet.has(permission.code)}
                        onChange={() =>
                          setForm(current => ({
                            ...current,
                            permissions: toggleSelection(current.permissions, permission.code),
                          }))
                        }
                        className="mt-1 h-4 w-4 accent-sky-500"
                      />
                      <div>
                        <p className="font-bold text-slate-900 dark:text-white text-sm">{permission.label}</p>
                        <p className="text-xs text-slate-500 mt-1">{permission.description}</p>
                        {inheritedPermissionSet.has(permission.code) && (
                          <p className="text-[11px] text-emerald-600 dark:text-emerald-300 mt-2">Incluido automáticamente por el rol base seleccionado.</p>
                        )}
                        <p className="text-[11px] font-mono text-slate-400 mt-2">{permission.code}</p>
                      </div>
                    </div>
                  </label>
                ))}
              </div>
            </div>
          </div>

          <AppButton variant="primary" icon="save" isLoading={saving} onClick={() => void handleSubmit()}>
            {editingRole ? 'Guardar cambios' : 'Crear rol'}
          </AppButton>
        </div>
      </div>

      {editableRoles.length === 0 && !loading && (
        <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 px-4 py-3 text-sm text-slate-500">
          Aún no se han creado roles dinámicos.
        </div>
      )}

      {message && (
        <div className="rounded-2xl border border-sky-100 bg-sky-50 px-4 py-3 text-sm font-medium text-sky-700 dark:border-sky-900/40 dark:bg-sky-950/30 dark:text-sky-200">
          {message}
        </div>
      )}
    </div>
  );
};

export default AdminRolesView;
