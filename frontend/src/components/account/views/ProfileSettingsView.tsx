import { useEffect, useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import { AppButton } from '../../ui/AppButton';
import { useAuth } from '../../../context/AuthContext';
import { UserService, type UserAccount } from '../../../services/userService';
import { getApiErrorMessage } from '../../../utils/apiError';
import { useSystemSettings } from '../../../context/SystemSettingsContext';
import { defaultSystemSettings, defaultUserPreferences, type SystemSettings, type UserPreferences } from '../../../services/system-settings.service';

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

const actionColors = ['#0284c7', '#0f766e', '#2563eb', '#7c3aed', '#be123c'];
const sidebarColors = ['#071e2b', '#111827', '#0f172a', '#164e63', '#312e81', '#3f1d2b'];

interface ColorSelectorProps {
  label: string;
  value: string;
  colors: string[];
  onChange: (color: string) => void;
  resetLabel?: string;
  onReset?: () => void;
}

const ColorSelector = ({ label, value, colors, onChange, resetLabel, onReset }: ColorSelectorProps) => (
  <div className="space-y-2">
    <span className="text-xs font-black uppercase tracking-widest text-slate-500">{label}</span>
    <div className="flex flex-wrap items-center gap-3">
      <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-black uppercase tracking-widest text-slate-600 shadow-sm hover:bg-slate-50 dark:border-white/10 dark:bg-slate-950 dark:text-slate-300 dark:hover:bg-white/5">
        <span className="h-6 w-6 rounded-full border border-slate-200 dark:border-white/10" style={{ backgroundColor: value }} />
        Color personalizado
        <input
          type="color"
          value={value}
          onChange={event => onChange(event.target.value)}
          className="h-0 w-0 opacity-0"
          aria-label={`Elegir ${label.toLowerCase()}`}
        />
      </label>
      {colors.map(color => (
        <button
          type="button"
          key={color}
          onClick={() => onChange(color)}
          className={`h-8 w-8 rounded-full border-2 ${value.toLowerCase() === color ? 'border-slate-950 dark:border-white' : 'border-transparent'}`}
          style={{ backgroundColor: color }}
          aria-label={`Seleccionar color ${color}`}
        />
      ))}
      {resetLabel && onReset && (
        <button
          type="button"
          onClick={onReset}
          className="rounded-lg border border-slate-200 px-3 py-2 text-xs font-black uppercase tracking-widest text-slate-600 hover:bg-slate-50 dark:border-white/10 dark:text-slate-300 dark:hover:bg-white/5"
        >
          {resetLabel}
        </button>
      )}
    </div>
  </div>
);

const ProfileSettingsView = ({ initialSection = 'perfil' }: ProfileSettingsViewProps) => {
  const { user, roles, permissions, hasAnyRole, hasPermission, applyAccount } = useAuth();
  const {
    systemSettings,
    userPreferences,
    effectivePreferences,
    updateSystemSettings,
    updateUserPreferences,
  } = useSystemSettings();
  const [activeSection, setActiveSection] = useState<AccountSection>(initialSection);
  const [account, setAccount] = useState<UserAccount | null>(null);
  const [loading, setLoading] = useState(true);
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [savingSystemSettings, setSavingSystemSettings] = useState(false);
  const [savingUserPreferences, setSavingUserPreferences] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [systemSettingsForm, setSystemSettingsForm] = useState<SystemSettings>(defaultSystemSettings);
  const [userPreferencesForm, setUserPreferencesForm] = useState<UserPreferences>({
    primaryColor: defaultSystemSettings.primaryColor,
    sidebarColor: defaultSystemSettings.sidebarColor,
    theme: defaultSystemSettings.defaultTheme,
    deviceBackupDownloadsEnabled: defaultSystemSettings.defaultDeviceBackupDownloadsEnabled,
  });
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

  const isAdmin = hasAnyRole(['ROLE_ADMIN']);
  const canSeeDeviceBackupPreference = isAdmin || hasPermission('admin.database.export');
  const canEnableDeviceBackups = systemSettings.defaultDeviceBackupDownloadsEnabled && canSeeDeviceBackupPreference;

  const displayRoles = useMemo(
    () => roles.map(role => roleLabels[role] ?? role.replace('ROLE_', '')).join(', ') || 'Sin rol asignado',
    [roles],
  );

  useEffect(() => {
    setActiveSection(initialSection);
  }, [initialSection]);

  useEffect(() => {
    setSystemSettingsForm(systemSettings);
  }, [systemSettings]);

  useEffect(() => {
    setUserPreferencesForm({
      primaryColor: userPreferences.primaryColor ?? effectivePreferences.primaryColor,
      sidebarColor: userPreferences.sidebarColor ?? effectivePreferences.sidebarColor,
      theme: userPreferences.theme ?? effectivePreferences.theme,
      deviceBackupDownloadsEnabled: userPreferences.deviceBackupDownloadsEnabled ?? effectivePreferences.deviceBackupDownloadsEnabled,
    });
  }, [effectivePreferences, userPreferences]);

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

  const handleSystemSettingsSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    clearFeedback();
    setSavingSystemSettings(true);
    try {
      const updated = await updateSystemSettings({
        ...systemSettingsForm,
        applicationName: systemSettingsForm.applicationName.trim() || defaultSystemSettings.applicationName,
        brandName: systemSettingsForm.brandName.trim() || defaultSystemSettings.brandName,
        primaryColor: systemSettingsForm.primaryColor || defaultSystemSettings.primaryColor,
        sidebarColor: systemSettingsForm.sidebarColor || defaultSystemSettings.sidebarColor,
      });
      setSystemSettingsForm(updated);
      setMessage('Ajustes globales actualizados correctamente.');
    } catch (settingsError) {
      setError(await getApiErrorMessage(settingsError, 'No se pudieron guardar los ajustes globales.'));
    } finally {
      setSavingSystemSettings(false);
    }
  };

  const handleUserPreferencesSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    clearFeedback();
    setSavingUserPreferences(true);
    try {
      const updated = await updateUserPreferences({
        primaryColor: userPreferencesForm.primaryColor || null,
        sidebarColor: userPreferencesForm.sidebarColor || null,
        theme: userPreferencesForm.theme || null,
        deviceBackupDownloadsEnabled: canSeeDeviceBackupPreference ? userPreferencesForm.deviceBackupDownloadsEnabled : null,
      });
      setUserPreferencesForm({
        primaryColor: updated.primaryColor ?? systemSettings.primaryColor,
        sidebarColor: updated.sidebarColor ?? systemSettings.sidebarColor,
        theme: updated.theme ?? systemSettings.defaultTheme,
        deviceBackupDownloadsEnabled: updated.deviceBackupDownloadsEnabled ?? systemSettings.defaultDeviceBackupDownloadsEnabled,
      });
      setMessage('Preferencias personales actualizadas correctamente.');
    } catch (preferencesError) {
      setError(await getApiErrorMessage(preferencesError, 'No se pudieron guardar tus preferencias.'));
    } finally {
      setSavingUserPreferences(false);
    }
  };

  const handleResetUserPreferences = async () => {
    clearFeedback();
    setSavingUserPreferences(true);
    try {
      const updated = await updateUserPreferences({
        ...defaultUserPreferences,
      });
      setUserPreferencesForm({
        primaryColor: updated.primaryColor ?? systemSettings.primaryColor,
        sidebarColor: updated.sidebarColor ?? systemSettings.sidebarColor,
        theme: updated.theme ?? systemSettings.defaultTheme,
        deviceBackupDownloadsEnabled: updated.deviceBackupDownloadsEnabled ?? systemSettings.defaultDeviceBackupDownloadsEnabled,
      });
      setMessage('Tus preferencias vuelven a usar los valores por defecto del sistema.');
    } catch (preferencesError) {
      setError(await getApiErrorMessage(preferencesError, 'No se pudieron restablecer tus preferencias.'));
    } finally {
      setSavingUserPreferences(false);
    }
  };

  const handleResetSystemSettings = async () => {
    clearFeedback();
    setSavingSystemSettings(true);
    try {
      const updated = await updateSystemSettings(defaultSystemSettings);
      setSystemSettingsForm(updated);
      setMessage('Las preferencias del sistema volvieron a los valores por defecto.');
    } catch (settingsError) {
      setError(await getApiErrorMessage(settingsError, 'No se pudieron restablecer los ajustes globales.'));
    } finally {
      setSavingSystemSettings(false);
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
                  className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-sky-400 focus:ring-4 focus:ring-sky-100 dark:border-white/10 dark:bg-slate-950 dark:text-white dark:focus:ring-sky-500/10"
                  required
                />
                <input
                  type="password"
                  placeholder="Confirmar contraseña"
                  value={passwordForm.confirmationPassword}
                  onChange={event => setPasswordForm(current => ({ ...current, confirmationPassword: event.target.value }))}
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
        <div className="space-y-6">
          <form onSubmit={handleUserPreferencesSubmit} className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm dark:border-white/10 dark:bg-slate-900">
            <div className="mb-5 flex items-center gap-3">
              <span className="material-symbols-outlined text-sky-600 dark:text-sky-300">tune</span>
              <div>
                <h2 className="text-base font-black text-slate-950 dark:text-white">Preferencias de usuario</h2>
                <p className="text-sm text-slate-500 dark:text-slate-400">Aplican solo a tu cuenta. Si las restableces, se usan los valores por defecto del sistema.</p>
              </div>
            </div>

            <div className="grid gap-5 lg:grid-cols-2">
              <ColorSelector
                label="Color personal de botones y enlaces"
                value={userPreferencesForm.primaryColor ?? effectivePreferences.primaryColor}
                colors={actionColors}
                onChange={color => setUserPreferencesForm(current => ({ ...current, primaryColor: color }))}
              />

              <ColorSelector
                label="Color personal del menú lateral"
                value={userPreferencesForm.sidebarColor ?? effectivePreferences.sidebarColor}
                colors={sidebarColors}
                onChange={color => setUserPreferencesForm(current => ({ ...current, sidebarColor: color }))}
              />

              <section className="space-y-3">
                <span className="text-xs font-black uppercase tracking-widest text-slate-500">Tema</span>
                <div className="grid grid-cols-2 gap-3">
                  {(['light', 'dark'] as const).map(nextTheme => (
                    <button
                      type="button"
                      key={nextTheme}
                      onClick={() => setUserPreferencesForm(current => ({ ...current, theme: nextTheme }))}
                      className={`rounded-lg border px-4 py-3 text-left transition ${
                        userPreferencesForm.theme === nextTheme
                          ? 'border-sky-300 bg-sky-50 text-sky-800 dark:border-sky-400 dark:bg-sky-500/10 dark:text-sky-200'
                          : 'border-slate-200 text-slate-600 hover:border-slate-300 dark:border-white/10 dark:text-slate-300'
                      }`}
                    >
                      <span className="material-symbols-outlined block text-xl">{nextTheme === 'light' ? 'light_mode' : 'dark_mode'}</span>
                      <span className="mt-2 block text-sm font-black">{nextTheme === 'light' ? 'Claro' : 'Oscuro'}</span>
                    </button>
                  ))}
                </div>
              </section>
            </div>

            {canSeeDeviceBackupPreference && (
              <div className="mt-5">
                <label className={`flex items-center justify-between gap-4 rounded-lg border p-4 ${canEnableDeviceBackups ? 'border-slate-200 dark:border-white/10' : 'border-slate-200 opacity-60 dark:border-white/10'}`}>
                  <span>
                    <span className="block text-sm font-black text-slate-900 dark:text-white">Descarga automática local de respaldos</span>
                    <span className="mt-1 block text-xs text-slate-500 dark:text-slate-400">
                      {canEnableDeviceBackups
                        ? 'Guarda en esta computadora el respaldo automático más reciente cuando esté disponible.'
                        : 'Las descargas locales de respaldos están desactivadas por la configuración global.'}
                    </span>
                  </span>
                  <input
                    type="checkbox"
                    checked={Boolean(userPreferencesForm.deviceBackupDownloadsEnabled)}
                    disabled={!canEnableDeviceBackups}
                    onChange={event => setUserPreferencesForm(current => ({ ...current, deviceBackupDownloadsEnabled: event.target.checked }))}
                    className="h-5 w-5 accent-sky-600"
                  />
                </label>
              </div>
            )}

            <div className="mt-6 flex flex-wrap justify-end gap-3">
              <AppButton type="button" variant="outline" disabled={savingUserPreferences} onClick={handleResetUserPreferences}>
                Usar valores por defecto
              </AppButton>
              <AppButton type="submit" variant="primary" disabled={savingUserPreferences}>
                {savingUserPreferences ? 'Guardando...' : 'Guardar preferencias'}
              </AppButton>
            </div>
          </form>

          {isAdmin && (
            <form onSubmit={handleSystemSettingsSubmit} className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm dark:border-white/10 dark:bg-slate-900">
              <div className="mb-5 flex items-center gap-3">
                <span className="material-symbols-outlined text-sky-600 dark:text-sky-300">admin_panel_settings</span>
                <div>
                  <h2 className="text-base font-black text-slate-950 dark:text-white">Preferencias del sistema</h2>
                  <p className="text-sm text-slate-500 dark:text-slate-400">Valores por defecto para todo el proyecto. Solo los administra el rol administrador.</p>
                </div>
              </div>

              <div className="grid gap-4 lg:grid-cols-2">
                <label className="space-y-2">
                  <span className="text-xs font-black uppercase tracking-widest text-slate-500">Nombre del sistema</span>
                  <input
                    value={systemSettingsForm.applicationName}
                    onChange={event => setSystemSettingsForm(current => ({ ...current, applicationName: event.target.value }))}
                    maxLength={80}
                    className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-sky-400 focus:ring-4 focus:ring-sky-100 dark:border-white/10 dark:bg-slate-950 dark:text-white dark:focus:ring-sky-500/10"
                  />
                </label>
                <label className="space-y-2">
                  <span className="text-xs font-black uppercase tracking-widest text-slate-500">Nombre corto de marca</span>
                  <input
                    value={systemSettingsForm.brandName}
                    onChange={event => setSystemSettingsForm(current => ({ ...current, brandName: event.target.value }))}
                    maxLength={80}
                    className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-sky-400 focus:ring-4 focus:ring-sky-100 dark:border-white/10 dark:bg-slate-950 dark:text-white dark:focus:ring-sky-500/10"
                  />
                </label>
                <ColorSelector
                  label="Color de botones y enlaces por defecto"
                  value={systemSettingsForm.primaryColor}
                  colors={actionColors}
                  onChange={color => setSystemSettingsForm(current => ({ ...current, primaryColor: color }))}
                  resetLabel="Azul predeterminado"
                  onReset={() => setSystemSettingsForm(current => ({ ...current, primaryColor: defaultSystemSettings.primaryColor }))}
                />
                <ColorSelector
                  label="Color del menú lateral por defecto"
                  value={systemSettingsForm.sidebarColor}
                  colors={sidebarColors}
                  onChange={color => setSystemSettingsForm(current => ({ ...current, sidebarColor: color }))}
                  resetLabel="Menú predeterminado"
                  onReset={() => setSystemSettingsForm(current => ({ ...current, sidebarColor: defaultSystemSettings.sidebarColor }))}
                />
              </div>

              <div className="mt-5 grid gap-3 lg:grid-cols-3">
                <div className="rounded-lg border border-slate-200 p-4 dark:border-white/10">
                  <span className="block text-sm font-black text-slate-900 dark:text-white">Tema por defecto</span>
                  <div className="mt-3 grid grid-cols-2 gap-2">
                    {(['light', 'dark'] as const).map(nextTheme => (
                      <button
                        type="button"
                        key={nextTheme}
                        onClick={() => setSystemSettingsForm(current => ({ ...current, defaultTheme: nextTheme }))}
                        className={`rounded-md border px-3 py-2 text-sm font-bold ${
                          systemSettingsForm.defaultTheme === nextTheme
                            ? 'border-sky-300 bg-sky-50 text-sky-800 dark:border-sky-400 dark:bg-sky-500/10 dark:text-sky-200'
                            : 'border-slate-200 text-slate-600 dark:border-white/10 dark:text-slate-300'
                        }`}
                      >
                        {nextTheme === 'light' ? 'Claro' : 'Oscuro'}
                      </button>
                    ))}
                  </div>
                </div>
                <label className="flex items-center justify-between gap-4 rounded-lg border border-slate-200 p-4 dark:border-white/10">
                  <span>
                    <span className="block text-sm font-black text-slate-900 dark:text-white">Descargas locales por defecto</span>
                    <span className="mt-1 block text-xs text-slate-500 dark:text-slate-400">Valor inicial para usuarios sin preferencia propia.</span>
                  </span>
                  <input
                    type="checkbox"
                    checked={systemSettingsForm.defaultDeviceBackupDownloadsEnabled}
                    onChange={event => setSystemSettingsForm(current => ({ ...current, defaultDeviceBackupDownloadsEnabled: event.target.checked }))}
                    className="h-5 w-5 accent-sky-600"
                  />
                </label>
                <label className="flex items-center justify-between gap-4 rounded-lg border border-slate-200 p-4 dark:border-white/10">
                  <span>
                    <span className="block text-sm font-black text-slate-900 dark:text-white">Recordatorios de descarga</span>
                    <span className="mt-1 block text-xs text-slate-500 dark:text-slate-400">Mantiene visible la preferencia de copias locales en dispositivos autorizados.</span>
                  </span>
                  <input
                    type="checkbox"
                    checked={systemSettingsForm.showBackupDownloadReminders}
                    onChange={event => setSystemSettingsForm(current => ({ ...current, showBackupDownloadReminders: event.target.checked }))}
                    className="h-5 w-5 accent-sky-600"
                  />
                </label>
              </div>

              <div className="mt-6 flex flex-wrap justify-end gap-3">
                <AppButton type="button" variant="outline" disabled={savingSystemSettings} onClick={handleResetSystemSettings}>
                  Usar valores por defecto
                </AppButton>
                <AppButton type="submit" variant="primary" disabled={savingSystemSettings}>
                  {savingSystemSettings ? 'Guardando...' : 'Guardar ajustes globales'}
                </AppButton>
              </div>
            </form>
          )}
        </div>
      )}
    </div>
  );
};

export default ProfileSettingsView;
