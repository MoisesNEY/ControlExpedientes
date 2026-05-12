import { useEffect, useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import { AppButton } from '../../ui/AppButton';
import { useAuth } from '../../../context/AuthContext';
import { useTheme } from '../../../context/ThemeContext';
import { DevicePreferencesService } from '../../../services/device-preferences.service';
import { UserService, type UserAccount } from '../../../services/userService';
import { getApiErrorMessage } from '../../../utils/apiError';

type AccountSection = 'perfil' | 'ajustes';

interface ProfileSettingsViewProps {
  initialSection?: AccountSection;
}

const roleLabels: Record<string, string> = {
  ROLE_ADMIN: 'Administrador',
  ROLE_MEDICO: 'Médico',
  ROLE_ENFERMERO: 'Enfermería',
  ROLE_RECEPCION: 'Recepción',
};

const ProfileSettingsView = ({ initialSection = 'perfil' }: ProfileSettingsViewProps) => {
  const { user, roles, permissions, hasAnyRole, hasPermission, applyAccount } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [activeSection, setActiveSection] = useState<AccountSection>(initialSection);
  const [account, setAccount] = useState<UserAccount | null>(null);
  const [loading, setLoading] = useState(true);
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [autoDownloadBackups, setAutoDownloadBackups] = useState(DevicePreferencesService.shouldAutoDownloadBackups);
  const [profileForm, setProfileForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
  });
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmationPassword: '',
  });

  const canManageDeviceBackups = hasAnyRole(['ROLE_ADMIN']) || hasPermission('admin.database.export');

  const displayRoles = useMemo(
    () => roles.map(role => roleLabels[role] ?? role.replace('ROLE_', '')).join(', ') || 'Sin rol asignado',
    [roles],
  );

  useEffect(() => {
    setActiveSection(initialSection);
  }, [initialSection]);

  useEffect(() => {
    let isMounted = true;

    const loadAccount = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await UserService.getAccount();
        if (!isMounted) return;
        setAccount(data);
        setProfileForm({
          firstName: data.firstName ?? '',
          lastName: data.lastName ?? '',
          email: data.email ?? '',
        });
      } catch (loadError) {
        if (!isMounted) return;
        setError(await getApiErrorMessage(loadError, 'No se pudo cargar tu perfil.'));
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    void loadAccount();

    return () => {
      isMounted = false;
    };
  }, []);

  const clearFeedback = () => {
    setMessage(null);
    setError(null);
  };

  const handleProfileSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    clearFeedback();
    setSavingProfile(true);
    try {
      const updatedAccount = await UserService.updateAccount({
        firstName: profileForm.firstName.trim(),
        lastName: profileForm.lastName.trim(),
        email: profileForm.email.trim(),
      });
      setAccount(updatedAccount);
      applyAccount(updatedAccount);
      setProfileForm({
        firstName: updatedAccount.firstName ?? '',
        lastName: updatedAccount.lastName ?? '',
        email: updatedAccount.email ?? '',
      });
      setMessage('Perfil actualizado correctamente.');
    } catch (saveError) {
      setError(await getApiErrorMessage(saveError, 'No se pudo actualizar tu perfil.'));
    } finally {
      setSavingProfile(false);
    }
  };

  const handlePasswordSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    clearFeedback();
    if (passwordForm.newPassword !== passwordForm.confirmationPassword) {
      setError('La nueva contraseña y su confirmación no coinciden.');
      return;
    }
    setSavingPassword(true);
    try {
      await UserService.changePassword(passwordForm);
      setPasswordForm({ currentPassword: '', newPassword: '', confirmationPassword: '' });
      setMessage('Contraseña actualizada correctamente.');
    } catch (passwordError) {
      setError(await getApiErrorMessage(passwordError, 'No se pudo cambiar tu contraseña.'));
    } finally {
      setSavingPassword(false);
    }
  };

  const handleAutoDownloadChange = (enabled: boolean) => {
    setAutoDownloadBackups(enabled);
    DevicePreferencesService.setAutoDownloadBackups(enabled);
    setMessage(enabled ? 'Las descargas automáticas en este dispositivo quedaron activas.' : 'Las descargas automáticas en este dispositivo quedaron desactivadas.');
    setError(null);
  };

  const toggleThemeIfNeeded = (nextTheme: 'light' | 'dark') => {
    if (theme !== nextTheme) {
      toggleTheme();
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-[360px] items-center justify-center">
        <div className="h-10 w-10 animate-spin rounded-full border-4 border-slate-200 border-t-sky-500"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 rounded-lg border border-slate-200 bg-white p-5 shadow-sm dark:border-white/10 dark:bg-slate-900 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-4">
          <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-lg bg-sky-600 text-xl font-black text-white shadow-sm">
            {(profileForm.firstName || user?.name || account?.login || 'U').charAt(0).toUpperCase()}
          </div>
          <div>
            <h1 className="text-xl font-black text-slate-950 dark:text-white">Mi cuenta</h1>
            <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{account?.login ?? user?.preferred_username}</p>
          </div>
        </div>

        <div className="inline-flex rounded-lg border border-slate-200 bg-slate-50 p-1 dark:border-white/10 dark:bg-slate-800">
          {(['perfil', 'ajustes'] as const).map(section => (
            <button
              key={section}
              onClick={() => setActiveSection(section)}
              className={`rounded-md px-4 py-2 text-sm font-bold capitalize transition-colors ${
                activeSection === section
                  ? 'bg-white text-sky-700 shadow-sm dark:bg-slate-950 dark:text-sky-300'
                  : 'text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-white'
              }`}
            >
              {section}
            </button>
          ))}
        </div>
      </div>

      {(message || error) && (
        <div
          className={`rounded-lg border px-4 py-3 text-sm font-semibold ${
            error
              ? 'border-rose-200 bg-rose-50 text-rose-700 dark:border-rose-500/20 dark:bg-rose-500/10 dark:text-rose-200'
              : 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-500/20 dark:bg-emerald-500/10 dark:text-emerald-200'
          }`}
        >
          {error ?? message}
        </div>
      )}

      {activeSection === 'perfil' ? (
        <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_360px]">
          <form onSubmit={handleProfileSubmit} className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm dark:border-white/10 dark:bg-slate-900">
            <div className="mb-5 flex items-center gap-3">
              <span className="material-symbols-outlined text-sky-600 dark:text-sky-300">badge</span>
              <div>
                <h2 className="text-base font-black text-slate-950 dark:text-white">Datos personales</h2>
                <p className="text-sm text-slate-500 dark:text-slate-400">Información visible en la sesión y reportes internos.</p>
              </div>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <label className="space-y-2">
                <span className="text-xs font-black uppercase tracking-widest text-slate-500">Nombres</span>
                <input
                  value={profileForm.firstName}
                  onChange={event => setProfileForm(current => ({ ...current, firstName: event.target.value }))}
                  maxLength={50}
                  className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-sky-400 focus:ring-4 focus:ring-sky-100 dark:border-white/10 dark:bg-slate-950 dark:text-white dark:focus:ring-sky-500/10"
                />
              </label>
              <label className="space-y-2">
                <span className="text-xs font-black uppercase tracking-widest text-slate-500">Apellidos</span>
                <input
                  value={profileForm.lastName}
                  onChange={event => setProfileForm(current => ({ ...current, lastName: event.target.value }))}
                  maxLength={50}
                  className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-sky-400 focus:ring-4 focus:ring-sky-100 dark:border-white/10 dark:bg-slate-950 dark:text-white dark:focus:ring-sky-500/10"
                />
              </label>
              <label className="space-y-2 md:col-span-2">
                <span className="text-xs font-black uppercase tracking-widest text-slate-500">Correo electrónico</span>
                <input
                  type="email"
                  value={profileForm.email}
                  onChange={event => setProfileForm(current => ({ ...current, email: event.target.value }))}
                  maxLength={254}
                  required
                  className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-sky-400 focus:ring-4 focus:ring-sky-100 dark:border-white/10 dark:bg-slate-950 dark:text-white dark:focus:ring-sky-500/10"
                />
              </label>
            </div>

            <div className="mt-6 flex justify-end">
              <AppButton type="submit" variant="primary" disabled={savingProfile}>
                {savingProfile ? 'Guardando...' : 'Guardar perfil'}
              </AppButton>
            </div>
          </form>

          <aside className="space-y-4">
            <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm dark:border-white/10 dark:bg-slate-900">
              <h2 className="text-sm font-black text-slate-950 dark:text-white">Resumen de acceso</h2>
              <div className="mt-4 space-y-3 text-sm">
                <div>
                  <p className="text-xs font-black uppercase tracking-widest text-slate-400">Usuario</p>
                  <p className="mt-1 font-semibold text-slate-700 dark:text-slate-200">{account?.login}</p>
                </div>
                <div>
                  <p className="text-xs font-black uppercase tracking-widest text-slate-400">Rol</p>
                  <p className="mt-1 font-semibold text-slate-700 dark:text-slate-200">{displayRoles}</p>
                </div>
                <div>
                  <p className="text-xs font-black uppercase tracking-widest text-slate-400">Permisos efectivos</p>
                  <p className="mt-1 font-semibold text-slate-700 dark:text-slate-200">{permissions.length}</p>
                </div>
              </div>
            </div>

            <form onSubmit={handlePasswordSubmit} className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm dark:border-white/10 dark:bg-slate-900">
              <div className="mb-4 flex items-center gap-3">
                <span className="material-symbols-outlined text-amber-600 dark:text-amber-300">lock_reset</span>
                <h2 className="text-sm font-black text-slate-950 dark:text-white">Contraseña</h2>
              </div>
              <div className="space-y-3">
                <input
                  type="password"
                  placeholder="Contraseña actual"
                  value={passwordForm.currentPassword}
                  onChange={event => setPasswordForm(current => ({ ...current, currentPassword: event.target.value }))}
                  className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-sky-400 focus:ring-4 focus:ring-sky-100 dark:border-white/10 dark:bg-slate-950 dark:text-white dark:focus:ring-sky-500/10"
                  required
                />
                <input
                  type="password"
                  placeholder="Nueva contraseña"
                  value={passwordForm.newPassword}
                  onChange={event => setPasswordForm(current => ({ ...current, newPassword: event.target.value }))}
                  minLength={8}
                  className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-sky-400 focus:ring-4 focus:ring-sky-100 dark:border-white/10 dark:bg-slate-950 dark:text-white dark:focus:ring-sky-500/10"
                  required
                />
                <input
                  type="password"
                  placeholder="Confirmar contraseña"
                  value={passwordForm.confirmationPassword}
                  onChange={event => setPasswordForm(current => ({ ...current, confirmationPassword: event.target.value }))}
                  minLength={8}
                  className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-sky-400 focus:ring-4 focus:ring-sky-100 dark:border-white/10 dark:bg-slate-950 dark:text-white dark:focus:ring-sky-500/10"
                  required
                />
              </div>
              <div className="mt-4">
                <AppButton type="submit" variant="secondary" disabled={savingPassword}>
                  {savingPassword ? 'Actualizando...' : 'Cambiar contraseña'}
                </AppButton>
              </div>
            </form>
          </aside>
        </div>
      ) : (
        <div className="grid gap-6 lg:grid-cols-2">
          <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm dark:border-white/10 dark:bg-slate-900">
            <div className="mb-5 flex items-center gap-3">
              <span className="material-symbols-outlined text-sky-600 dark:text-sky-300">palette</span>
              <div>
                <h2 className="text-base font-black text-slate-950 dark:text-white">Apariencia</h2>
                <p className="text-sm text-slate-500 dark:text-slate-400">Preferencia guardada en este navegador.</p>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <button
                onClick={() => toggleThemeIfNeeded('light')}
                className={`rounded-lg border px-4 py-3 text-left transition ${
                  theme === 'light'
                    ? 'border-sky-300 bg-sky-50 text-sky-800'
                    : 'border-slate-200 text-slate-600 hover:border-slate-300 dark:border-white/10 dark:text-slate-300'
                }`}
              >
                <span className="material-symbols-outlined block text-xl">light_mode</span>
                <span className="mt-2 block text-sm font-black">Claro</span>
              </button>
              <button
                onClick={() => toggleThemeIfNeeded('dark')}
                className={`rounded-lg border px-4 py-3 text-left transition ${
                  theme === 'dark'
                    ? 'border-sky-400 bg-sky-500/10 text-sky-200'
                    : 'border-slate-200 text-slate-600 hover:border-slate-300 dark:border-white/10 dark:text-slate-300'
                }`}
              >
                <span className="material-symbols-outlined block text-xl">dark_mode</span>
                <span className="mt-2 block text-sm font-black">Oscuro</span>
              </button>
            </div>
          </section>

          <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm dark:border-white/10 dark:bg-slate-900">
            <div className="mb-5 flex items-center gap-3">
              <span className="material-symbols-outlined text-emerald-600 dark:text-emerald-300">download_for_offline</span>
              <div>
                <h2 className="text-base font-black text-slate-950 dark:text-white">Respaldos en este dispositivo</h2>
                <p className="text-sm text-slate-500 dark:text-slate-400">
                  {canManageDeviceBackups ? 'Descarga una copia local cuando el servidor genere respaldos automáticos.' : 'Disponible para usuarios con acceso a exportación de base de datos.'}
                </p>
              </div>
            </div>

            <label className={`flex items-center justify-between gap-4 rounded-lg border p-4 ${canManageDeviceBackups ? 'border-slate-200 dark:border-white/10' : 'border-slate-200 opacity-60 dark:border-white/10'}`}>
              <span>
                <span className="block text-sm font-black text-slate-900 dark:text-white">Descarga automática local</span>
                <span className="mt-1 block text-xs text-slate-500 dark:text-slate-400">
                  {autoDownloadBackups ? 'Activa en este navegador.' : 'Desactivada en este navegador.'}
                </span>
              </span>
              <input
                type="checkbox"
                checked={autoDownloadBackups}
                disabled={!canManageDeviceBackups}
                onChange={event => handleAutoDownloadChange(event.target.checked)}
                className="h-5 w-5 accent-sky-600"
              />
            </label>
          </section>
        </div>
      )}
    </div>
  );
};

export default ProfileSettingsView;
