import { useEffect, useState } from 'react';
import { AppButton } from '../../ui/AppButton';
import { useAuth } from '../../../context/AuthContext';
import {
  DatabaseAdminService,
  type DatabaseBackupFrequency,
  type DatabaseBackupHistoryItem,
  type DatabaseBackupSettings,
  type DatabaseBackupSummary,
} from '../../../services/database-admin.service';

const DAY_OPTIONS = [
  { value: 'MONDAY', label: 'Lunes' },
  { value: 'TUESDAY', label: 'Martes' },
  { value: 'WEDNESDAY', label: 'Miércoles' },
  { value: 'THURSDAY', label: 'Jueves' },
  { value: 'FRIDAY', label: 'Viernes' },
  { value: 'SATURDAY', label: 'Sábado' },
  { value: 'SUNDAY', label: 'Domingo' },
] as const;

const FREQUENCY_OPTIONS: Array<{ value: DatabaseBackupFrequency; label: string; description: string }> = [
  { value: 'DAILY', label: 'Diario', description: 'Realiza un respaldo todos los días a la hora configurada.' },
  { value: 'WEEKLY', label: 'Semanal', description: 'Permite elegir el día de la semana y la hora del respaldo.' },
  { value: 'INTERVAL_HOURS', label: 'Intervalo', description: 'Ejecuta respaldos automáticos cada cierta cantidad de horas.' },
];

const MAX_INTERVAL_HOURS = 720;

const defaultSettings: DatabaseBackupSettings = {
  enabled: false,
  frequency: 'DAILY',
  intervalHours: 24,
  dayOfWeek: 'MONDAY',
  time: '02:00',
  lastBackupAt: null,
  lastAutomaticExecutionAt: null,
  nextExecutionAt: null,
  lastBackupFilename: null,
};

const normalizeTimeValue = (value?: string | null) => (value ? value.slice(0, 5) : '02:00');

const formatDateTime = (value?: string | null) =>
  value
    ? new Date(value).toLocaleString('es-NI', {
        dateStyle: 'medium',
        timeStyle: 'short',
      })
    : '—';

const formatBytes = (value: number) => {
  if (value <= 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB'];
  const unitIndex = Math.min(Math.floor(Math.log(value) / Math.log(1024)), units.length - 1);
  const normalized = value / 1024 ** unitIndex;
  return `${normalized.toFixed(unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`;
};

const AdminDatabaseView = () => {
  const { hasAnyRole, hasPermission } = useAuth();
  const canView = hasAnyRole(['ROLE_ADMIN']) || hasPermission('admin.database.view');
  const canExport = hasAnyRole(['ROLE_ADMIN']) || hasPermission('admin.database.export');
  const canRestore = hasAnyRole(['ROLE_ADMIN']) || hasPermission('admin.database.restore');

  const [summary, setSummary] = useState<DatabaseBackupSummary | null>(null);
  const [settings, setSettings] = useState<DatabaseBackupSettings>(defaultSettings);
  const [backupFile, setBackupFile] = useState<File | null>(null);
  const [restorePassword, setRestorePassword] = useState('');
  const [confirmationText, setConfirmationText] = useState('');
  const [loading, setLoading] = useState(true);
  const [savingSettings, setSavingSettings] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [restoringUpload, setRestoringUpload] = useState(false);
  const [restoringStoredFile, setRestoringStoredFile] = useState<string | null>(null);
  const [downloadingStoredFile, setDownloadingStoredFile] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const loadSummary = async () => {
    if (!canView && !canExport && !canRestore) {
      return;
    }
    setLoading(true);
    try {
      const data = await DatabaseAdminService.getSummary();
      setSummary(data);
      setSettings({
        ...data.settings,
        frequency: data.settings.frequency ?? 'DAILY',
        intervalHours: data.settings.intervalHours ?? 24,
        dayOfWeek: data.settings.dayOfWeek ?? 'MONDAY',
        time: normalizeTimeValue(data.settings.time),
      });
    } catch (error) {
      console.error('Error cargando resumen de respaldos:', error);
      setMessage('No se pudo cargar la configuración de respaldos.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadSummary();
  }, [canExport, canRestore, canView]);

  const resetRestoreConfirmation = () => {
    setRestorePassword('');
    setConfirmationText('');
  };

  const validateRestoreAttempt = () => {
    if (!canRestore) {
      setMessage('No tienes permisos para restaurar respaldos.');
      return false;
    }
    if (!restorePassword.trim()) {
      setMessage('Debes escribir tu contraseña actual para restaurar.');
      return false;
    }
    if (confirmationText.trim().toUpperCase() !== 'RESTAURAR') {
      setMessage('Escribe RESTAURAR para confirmar la acción.');
      return false;
    }
    return true;
  };

  const handleExport = async () => {
    setMessage(null);
    setExporting(true);
    try {
      await DatabaseAdminService.exportDatabase();
      setMessage('Respaldo generado, almacenado y descargado correctamente.');
      await loadSummary();
    } catch (error) {
      console.error('Error exportando base de datos:', error);
      setMessage('No se pudo generar el respaldo.');
    } finally {
      setExporting(false);
    }
  };

  const handleSaveSettings = async () => {
    setMessage(null);
    setSavingSettings(true);
    try {
      await DatabaseAdminService.saveSettings({
        ...settings,
        time: normalizeTimeValue(settings.time),
      });
      setMessage('Programación automática actualizada.');
      await loadSummary();
    } catch (error) {
      console.error('Error guardando configuración de respaldos:', error);
      setMessage('No se pudo guardar la programación automática.');
    } finally {
      setSavingSettings(false);
    }
  };

  const handleRestoreUpload = async () => {
    if (!backupFile) {
      setMessage('Seleccione un archivo .backup o .sql antes de restaurar.');
      return;
    }
    if (!validateRestoreAttempt()) {
      return;
    }

    setMessage(null);
    setRestoringUpload(true);
    try {
      await DatabaseAdminService.restoreDatabase(backupFile, restorePassword);
      setBackupFile(null);
      resetRestoreConfirmation();
      setMessage('Restauración desde archivo ejecutada correctamente.');
      await loadSummary();
    } catch (error) {
      console.error('Error restaurando desde archivo:', error);
      setMessage('No se pudo restaurar el archivo seleccionado.');
    } finally {
      setRestoringUpload(false);
    }
  };

  const handleRestoreStoredBackup = async (backup: DatabaseBackupHistoryItem) => {
    if (!validateRestoreAttempt()) {
      return;
    }

    setMessage(null);
    setRestoringStoredFile(backup.filename);
    try {
      await DatabaseAdminService.restoreStoredBackup(backup.filename, restorePassword);
      resetRestoreConfirmation();
      setMessage(`Restauración ejecutada con ${backup.filename}.`);
      await loadSummary();
    } catch (error) {
      console.error('Error restaurando respaldo almacenado:', error);
      setMessage('No se pudo restaurar el respaldo almacenado.');
    } finally {
      setRestoringStoredFile(null);
    }
  };

  const handleDownloadStoredBackup = async (backup: DatabaseBackupHistoryItem) => {
    setDownloadingStoredFile(backup.filename);
    try {
      await DatabaseAdminService.downloadStoredBackup(backup.filename);
    } catch (error) {
      console.error('Error descargando respaldo almacenado:', error);
      setMessage('No se pudo descargar el respaldo seleccionado.');
    } finally {
      setDownloadingStoredFile(null);
    }
  };

  if (!canView && !canExport && !canRestore) {
    return (
      <div className="rounded-3xl border border-amber-200 bg-amber-50 px-6 py-5 text-sm font-medium text-amber-700">
        No tienes permisos para administrar respaldos de base de datos.
      </div>
    );
  }

  return (
    <div className="p-4 md:p-8 max-w-7xl mx-auto w-full flex flex-col gap-6 md:gap-8">
      <div className="flex flex-col gap-1">
        <h2 className="text-slate-900 dark:text-white text-3xl font-black tracking-tight">Administración de base de datos</h2>
        <p className="text-slate-500 text-base font-medium">
          La restauración ahora exige tu contraseña actual y la confirmación explícita RESTAURAR para evitar ejecuciones accidentales. Los respaldos automáticos se descargan automáticamente solo cuando tienes sesión activa, WebSocket conectado y permiso de exportación.
        </p>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-4 md:gap-6">
        <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm">
          <p className="text-xs font-black uppercase tracking-widest text-sky-500">Último respaldo</p>
          <p className="mt-3 text-lg font-bold text-slate-900 dark:text-white">{summary?.settings.lastBackupFilename ?? 'Aún no hay respaldos registrados'}</p>
          <p className="mt-2 text-sm text-slate-500">{formatDateTime(summary?.settings.lastBackupAt)}</p>
        </div>
        <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm">
          <p className="text-xs font-black uppercase tracking-widest text-emerald-500">Próxima ejecución</p>
          <p className="mt-3 text-lg font-bold text-slate-900 dark:text-white">{formatDateTime(summary?.settings.nextExecutionAt)}</p>
          <p className="mt-2 text-sm text-slate-500">{settings.enabled ? 'Programación automática activa.' : 'Automatización desactivada.'}</p>
        </div>
        <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm">
          <p className="text-xs font-black uppercase tracking-widest text-violet-500">Historial almacenado</p>
          <p className="mt-3 text-3xl font-black text-slate-900 dark:text-white">{summary?.backups.length ?? 0}</p>
          <p className="mt-2 text-sm text-slate-500">Cada respaldo automático queda disponible en el servidor y, si estás conectado, también se descarga automáticamente.</p>
        </div>
      </div>

      {canExport && (
        <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm space-y-4">
          <div>
            <h3 className="text-lg font-black text-slate-900 dark:text-white">Respaldo manual</h3>
            <p className="text-sm text-slate-500 mt-1">Genera un respaldo inmediato, lo descarga en tu equipo y lo conserva en el historial del servidor.</p>
          </div>
          <AppButton variant="primary" icon="download" isLoading={exporting} onClick={handleExport}>
            Exportar base de datos
          </AppButton>
        </div>
      )}

      {(canRestore || canView) && (
        <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm space-y-5">
          <div className="flex flex-col gap-1">
            <h3 className="text-lg font-black text-slate-900 dark:text-white">Confirmación reforzada para restaurar</h3>
            <p className="text-sm text-slate-500">Antes de restaurar, vuelve a escribir tu contraseña actual y confirma la palabra RESTAURAR.</p>
          </div>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50/80 dark:bg-slate-800/40 p-4">
              <label className="text-xs font-black uppercase tracking-widest text-slate-500">Contraseña actual</label>
              <input
                type="password"
                value={restorePassword}
                onChange={event => setRestorePassword(event.target.value)}
                className="mt-2 w-full rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-sky-500"
                placeholder="Escribe tu contraseña para confirmar"
              />
            </div>
            <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50/80 dark:bg-slate-800/40 p-4">
              <label className="text-xs font-black uppercase tracking-widest text-slate-500">Texto de confirmación</label>
              <input
                type="text"
                value={confirmationText}
                onChange={event => setConfirmationText(event.target.value)}
                className="mt-2 w-full rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-sky-500"
                placeholder="Escribe RESTAURAR"
              />
            </div>
          </div>
        </div>
      )}

      {canRestore && (
        <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm space-y-4">
          <div>
            <h3 className="text-lg font-black text-slate-900 dark:text-white">Restauración por archivo</h3>
            <p className="text-sm text-slate-500 mt-1">Carga un .backup o .sql y reemplaza el contenido actual de la base de datos.</p>
          </div>
          <label className="flex flex-col gap-2 rounded-2xl border border-dashed border-slate-300 dark:border-slate-700 px-4 py-4 text-sm text-slate-600 dark:text-slate-300 cursor-pointer hover:border-sky-400">
            <span className="font-semibold">{backupFile ? backupFile.name : 'Seleccionar archivo de respaldo'}</span>
            <span className="text-xs text-slate-400">Formatos soportados: .backup, .sql</span>
            <input type="file" accept=".backup,.sql" className="hidden" onChange={event => setBackupFile(event.target.files?.[0] ?? null)} />
          </label>
          <AppButton variant="danger" icon="upload" disabled={!backupFile} isLoading={restoringUpload} onClick={handleRestoreUpload}>
            Restaurar desde archivo
          </AppButton>
        </div>
      )}

      {canView && (
        <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm space-y-5">
          <div className="flex flex-col gap-1">
            <h3 className="text-lg font-black text-slate-900 dark:text-white">Programación automática</h3>
            <p className="text-sm text-slate-500">El servicio verifica la hora programada y genera respaldos automáticos que quedan listos para descarga.</p>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <label className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50/80 dark:bg-slate-800/40 p-4 flex items-center justify-between gap-4">
              <div>
                <p className="font-bold text-slate-900 dark:text-white">Activar respaldo automático</p>
                <p className="text-xs text-slate-500 mt-1">Cuando está activo, el servicio ejecuta exportaciones según la programación elegida.</p>
              </div>
              <input
                type="checkbox"
                className="h-5 w-5 accent-sky-500"
                checked={settings.enabled}
                onChange={event => setSettings(current => ({ ...current, enabled: event.target.checked }))}
              />
            </label>

            <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50/80 dark:bg-slate-800/40 p-4">
              <label className="text-xs font-black uppercase tracking-widest text-slate-500">Frecuencia</label>
              <select
                value={settings.frequency}
                onChange={event => setSettings(current => ({ ...current, frequency: event.target.value as DatabaseBackupFrequency }))}
                className="mt-2 w-full rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-sky-500"
              >
                {FREQUENCY_OPTIONS.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
              <p className="mt-2 text-xs text-slate-500">{FREQUENCY_OPTIONS.find(option => option.value === settings.frequency)?.description}</p>
            </div>

            <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50/80 dark:bg-slate-800/40 p-4">
              <label className="text-xs font-black uppercase tracking-widest text-slate-500">Hora de exportación</label>
              <input
                type="time"
                value={normalizeTimeValue(settings.time)}
                onChange={event => setSettings(current => ({ ...current, time: event.target.value }))}
                className="mt-2 w-full rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-sky-500"
              />
            </div>

            {settings.frequency === 'WEEKLY' && (
              <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50/80 dark:bg-slate-800/40 p-4">
                <label className="text-xs font-black uppercase tracking-widest text-slate-500">Día de la semana</label>
                <select
                  value={settings.dayOfWeek ?? 'MONDAY'}
                  onChange={event => setSettings(current => ({ ...current, dayOfWeek: event.target.value as DatabaseBackupSettings['dayOfWeek'] }))}
                  className="mt-2 w-full rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-sky-500"
                >
                  {DAY_OPTIONS.map(option => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>
            )}

            {settings.frequency === 'INTERVAL_HOURS' && (
              <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50/80 dark:bg-slate-800/40 p-4">
                <label className="text-xs font-black uppercase tracking-widest text-slate-500">Cada cuántas horas</label>
                <input
                  type="number"
                  min={1}
                  max={MAX_INTERVAL_HOURS}
                  value={settings.intervalHours ?? 24}
                  onChange={event => setSettings(current => ({ ...current, intervalHours: Number(event.target.value) || 24 }))}
                  className="mt-2 w-full rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-sky-500"
                />
              </div>
            )}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div className="rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-800 px-4 py-3">
              <p className="text-xs font-black uppercase tracking-widest text-slate-500">Última exportación automática</p>
              <p className="mt-2 text-sm font-semibold text-slate-900 dark:text-white">{formatDateTime(summary?.settings.lastAutomaticExecutionAt)}</p>
            </div>
            <div className="rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-800 px-4 py-3">
              <p className="text-xs font-black uppercase tracking-widest text-slate-500">Próxima exportación</p>
              <p className="mt-2 text-sm font-semibold text-slate-900 dark:text-white">{formatDateTime(summary?.settings.nextExecutionAt)}</p>
            </div>
            <div className="rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-800 px-4 py-3">
              <p className="text-xs font-black uppercase tracking-widest text-slate-500">Modo actual</p>
              <p className="mt-2 text-sm font-semibold text-slate-900 dark:text-white">
                {FREQUENCY_OPTIONS.find(option => option.value === settings.frequency)?.label ?? 'Diario'}
              </p>
            </div>
          </div>

          <AppButton variant="primary" icon="save" isLoading={savingSettings} onClick={handleSaveSettings}>
            Guardar programación
          </AppButton>
        </div>
      )}

      {canView && (
        <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm space-y-4">
          <div>
            <h3 className="text-lg font-black text-slate-900 dark:text-white">Respaldos disponibles</h3>
            <p className="text-sm text-slate-500 mt-1">Descargue un respaldo anterior o restaure directamente desde una exportación ya generada.</p>
          </div>

          {loading ? (
            <p className="text-sm text-slate-500">Cargando historial de respaldos...</p>
          ) : summary?.backups.length ? (
            <div className="space-y-3">
              {summary.backups.map(backup => (
                <div key={backup.filename} className="rounded-2xl border border-slate-200 dark:border-slate-800 px-4 py-4 bg-slate-50/70 dark:bg-slate-800/30">
                  <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
                    <div>
                      <p className="font-bold text-slate-900 dark:text-white break-all">{backup.filename}</p>
                      <div className="mt-2 flex flex-wrap gap-2 text-xs text-slate-500">
                        <span className="rounded-full bg-white dark:bg-slate-900 px-2.5 py-1 border border-slate-200 dark:border-slate-700">{backup.trigger}</span>
                        <span>{formatDateTime(backup.createdAt)}</span>
                        <span>{formatBytes(backup.sizeBytes)}</span>
                      </div>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      {canExport && (
                        <AppButton
                          variant="outline"
                          size="sm"
                          icon="download"
                          isLoading={downloadingStoredFile === backup.filename}
                          onClick={() => handleDownloadStoredBackup(backup)}
                        >
                          Descargar
                        </AppButton>
                      )}
                      {canRestore && (
                        <AppButton
                          variant="danger"
                          size="sm"
                          icon="restart_alt"
                          isLoading={restoringStoredFile === backup.filename}
                          onClick={() => handleRestoreStoredBackup(backup)}
                        >
                          Restaurar
                        </AppButton>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-slate-500">Todavía no hay respaldos almacenados.</p>
          )}
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

export default AdminDatabaseView;
