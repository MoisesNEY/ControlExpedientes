import { useEffect, useState } from 'react';
import { AppButton } from '../../ui/AppButton';
import {
  AdminSecurityService,
  KEYCLOAK_REQUIRED_ACTIONS,
  type ManagedUser,
  type ManagedUserPayload,
  type RoleDefinition,
} from '../../../services/admin-security.service';

const emptyUserForm: ManagedUserPayload = {
  login: '',
  firstName: '',
  lastName: '',
  email: '',
  activated: true,
  roles: [],
  password: '',
  temporaryPassword: true,
  requiredActions: ['UPDATE_PASSWORD'],
};

const AdminUsersView = () => {
  const [users, setUsers] = useState<ManagedUser[]>([]);
  const [roles, setRoles] = useState<RoleDefinition[]>([]);
  const [form, setForm] = useState<ManagedUserPayload>(emptyUserForm);
  const [editingUser, setEditingUser] = useState<ManagedUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const loadData = async () => {
    setLoading(true);
    try {
      const [userList, roleList] = await Promise.all([AdminSecurityService.getUsers(), AdminSecurityService.getRoles()]);
      setUsers(userList);
      setRoles(roleList);
    } catch (error) {
      console.error('Error cargando usuarios:', error);
      setMessage('No se pudieron cargar los usuarios administrados.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadData();
  }, []);

  const toggleSelection = (collection: string[], value: string) =>
    collection.includes(value) ? collection.filter(item => item !== value) : [...collection, value];

  const resetForm = () => {
    setEditingUser(null);
    setForm(emptyUserForm);
  };

  const startEditing = (user: ManagedUser) => {
    setEditingUser(user);
    setForm({
      login: user.login,
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      activated: user.activated,
      roles: user.roles,
      password: '',
      temporaryPassword: true,
      requiredActions: user.requiredActions.length ? user.requiredActions : ['UPDATE_PASSWORD'],
    });
    setMessage(null);
  };

  const handleSubmit = async () => {
    setSaving(true);
    setMessage(null);
    try {
      if (editingUser) {
        await AdminSecurityService.updateUser(editingUser.id, form);
        setMessage(`Usuario ${form.login} actualizado correctamente.`);
      } else {
        await AdminSecurityService.createUser(form);
        setMessage(`Usuario ${form.login} creado correctamente.`);
      }
      resetForm();
      await loadData();
    } catch (error) {
      console.error('Error guardando usuario:', error);
      setMessage('No se pudo guardar el usuario en Keycloak.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="p-4 md:p-8 max-w-7xl mx-auto w-full flex flex-col gap-6 md:gap-8">
      <div className="flex flex-col gap-1">
        <h2 className="text-slate-900 dark:text-white text-3xl font-black tracking-tight">Gestión de usuarios</h2>
        <p className="text-slate-500 text-base font-medium">
          Crea usuarios directamente en Keycloak, asigna roles y define acciones obligatorias como cambio de contraseña al primer inicio.
        </p>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-[1.1fr_0.9fr] gap-6">
        <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm space-y-4">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h3 className="text-lg font-black text-slate-900 dark:text-white">Usuarios administrados</h3>
              <p className="text-sm text-slate-500">Listado consultado desde Keycloak con sus roles efectivos.</p>
            </div>
            <AppButton variant="outline" icon="refresh" onClick={() => void loadData()}>
              Actualizar
            </AppButton>
          </div>

          {loading ? (
            <p className="text-sm text-slate-500">Cargando usuarios...</p>
          ) : (
            <div className="space-y-3">
              {users.map(user => (
                <div key={user.id} className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-800/30 p-4">
                  <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-3">
                    <div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <p className="font-bold text-slate-900 dark:text-white">{user.login}</p>
                        <span className={`rounded-full px-2.5 py-1 text-[10px] font-black uppercase ${user.activated ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-300' : 'bg-rose-100 text-rose-700 dark:bg-rose-500/10 dark:text-rose-300'}`}>
                          {user.activated ? 'Activo' : 'Inactivo'}
                        </span>
                      </div>
                      <p className="mt-1 text-sm text-slate-500">
                        {[user.firstName, user.lastName].filter(Boolean).join(' ') || 'Sin nombre'} · {user.email || 'Sin correo'}
                      </p>
                      <div className="mt-3 flex flex-wrap gap-2 text-xs text-slate-500">
                        {user.roles.map(role => (
                          <span key={`${user.id}-${role}`} className="rounded-full border border-slate-200 dark:border-slate-700 px-2.5 py-1 bg-white dark:bg-slate-900">
                            {role}
                          </span>
                        ))}
                        {user.requiredActions.map(action => (
                          <span key={`${user.id}-${action}`} className="rounded-full border border-amber-200 px-2.5 py-1 bg-amber-50 text-amber-700 dark:border-amber-500/30 dark:bg-amber-500/10 dark:text-amber-200">
                            {action}
                          </span>
                        ))}
                      </div>
                    </div>
                    <AppButton variant="outline" size="sm" icon="edit" onClick={() => startEditing(user)}>
                      Editar
                    </AppButton>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm space-y-5">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h3 className="text-lg font-black text-slate-900 dark:text-white">{editingUser ? `Editar ${editingUser.login}` : 'Crear usuario'}</h3>
              <p className="text-sm text-slate-500">La contraseña solo se envía a Keycloak cuando la escribes.</p>
            </div>
            {editingUser && (
              <AppButton variant="ghost" icon="close" onClick={resetForm}>
                Cancelar
              </AppButton>
            )}
          </div>

          <div className="space-y-4">
            {[
              { key: 'login', label: 'Usuario', type: 'text' },
              { key: 'firstName', label: 'Nombres', type: 'text' },
              { key: 'lastName', label: 'Apellidos', type: 'text' },
              { key: 'email', label: 'Correo', type: 'email' },
            ].map(field => (
              <div key={field.key}>
                <label className="text-xs font-black uppercase tracking-widest text-slate-500">{field.label}</label>
                <input
                  type={field.type}
                  value={form[field.key as keyof ManagedUserPayload] as string}
                  onChange={event => setForm(current => ({ ...current, [field.key]: field.key === 'login' ? event.target.value.toLowerCase() : event.target.value }))}
                  className="mt-2 w-full rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-sky-500"
                />
              </div>
            ))}

            <div>
              <label className="text-xs font-black uppercase tracking-widest text-slate-500">Contraseña</label>
              <input
                type="password"
                value={form.password ?? ''}
                onChange={event => setForm(current => ({ ...current, password: event.target.value }))}
                className="mt-2 w-full rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-sky-500"
                placeholder={editingUser ? 'Déjala vacía para no cambiarla' : 'Contraseña inicial'}
              />
            </div>

            <label className="flex items-center gap-3 rounded-xl border border-slate-200 dark:border-slate-800 px-3 py-2.5 text-sm">
              <input
                type="checkbox"
                checked={form.activated}
                onChange={event => setForm(current => ({ ...current, activated: event.target.checked }))}
                className="h-4 w-4 accent-sky-500"
              />
              <span>Usuario activo</span>
            </label>

            <label className="flex items-center gap-3 rounded-xl border border-slate-200 dark:border-slate-800 px-3 py-2.5 text-sm">
              <input
                type="checkbox"
                checked={form.temporaryPassword}
                onChange={event => setForm(current => ({ ...current, temporaryPassword: event.target.checked }))}
                className="h-4 w-4 accent-sky-500"
              />
              <span>Marcar contraseña como temporal</span>
            </label>

            <div>
              <p className="text-xs font-black uppercase tracking-widest text-slate-500">Roles asignados</p>
              <div className="mt-3 grid grid-cols-1 gap-2">
                {roles.map(role => (
                  <label key={role.roleName} className="flex items-center gap-3 rounded-xl border border-slate-200 dark:border-slate-800 px-3 py-2.5 text-sm">
                    <input
                      type="checkbox"
                      checked={form.roles.includes(role.roleName)}
                      onChange={() =>
                        setForm(current => ({
                          ...current,
                          roles: toggleSelection(current.roles, role.roleName),
                        }))
                      }
                      className="h-4 w-4 accent-sky-500"
                    />
                    <span>{role.roleName}</span>
                  </label>
                ))}
              </div>
            </div>

            <div>
              <p className="text-xs font-black uppercase tracking-widest text-slate-500">Acciones obligatorias de Keycloak</p>
              <div className="mt-3 space-y-2">
                {KEYCLOAK_REQUIRED_ACTIONS.map(action => (
                  <label key={action.value} className="flex items-center gap-3 rounded-xl border border-slate-200 dark:border-slate-800 px-3 py-2.5 text-sm">
                    <input
                      type="checkbox"
                      checked={form.requiredActions.includes(action.value)}
                      onChange={() =>
                        setForm(current => ({
                          ...current,
                          requiredActions: toggleSelection(current.requiredActions, action.value),
                        }))
                      }
                      className="h-4 w-4 accent-sky-500"
                    />
                    <span>{action.label}</span>
                  </label>
                ))}
              </div>
            </div>
          </div>

          <AppButton variant="primary" icon="save" isLoading={saving} onClick={() => void handleSubmit()}>
            {editingUser ? 'Guardar cambios' : 'Crear usuario'}
          </AppButton>
        </div>
      </div>

      {message && (
        <div className="rounded-2xl border border-sky-100 bg-sky-50 px-4 py-3 text-sm font-medium text-sky-700 dark:border-sky-900/40 dark:bg-sky-950/30 dark:text-sky-200">
          {message}
        </div>
      )}
    </div>
  );
};

export default AdminUsersView;
