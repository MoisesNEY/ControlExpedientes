import React, { useState, useMemo, useEffect, useCallback, useRef } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Navbar } from './Navbar';
import { navigationConfig } from '../../config/navigation';
import { useAuth } from '../../context/AuthContext';
import { useWebSocket } from '../../hooks/useWebSocket';
import ToastNotification from '../ui/ToastNotification';
import { DatabaseAdminService } from '../../services/database-admin.service';
import { DevicePreferencesService } from '../../services/device-preferences.service';
import { canAccessRequirement } from '../../utils/accessControl';

const SIDEBAR_STORAGE_KEY = 'scan-sidebar-collapsed';
const DOWNLOADED_BACKUPS_STORAGE_KEY = 'control-expedientes-downloaded-device-backups';
const DESKTOP_BREAKPOINT = 1024;

const readSidebarPreference = () => {
  if (typeof window === 'undefined') return false;
  try {
    return localStorage.getItem(SIDEBAR_STORAGE_KEY) === 'true';
  } catch {
    return false;
  }
};

const writeSidebarPreference = (collapsed: boolean) => {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(SIDEBAR_STORAGE_KEY, String(collapsed));
  } catch {
    // Ignorar errores de almacenamiento del navegador
  }
};

const readDownloadedBackups = () => {
  if (typeof window === 'undefined') return new Set<string>();
  try {
    const parsed = JSON.parse(localStorage.getItem(DOWNLOADED_BACKUPS_STORAGE_KEY) || '[]') as unknown;
    return new Set(Array.isArray(parsed) ? parsed.filter((item): item is string => typeof item === 'string') : []);
  } catch {
    return new Set<string>();
  }
};

const rememberDownloadedBackup = (filename: string) => {
  const downloaded = readDownloadedBackups();
  downloaded.add(filename);
  try {
    localStorage.setItem(DOWNLOADED_BACKUPS_STORAGE_KEY, JSON.stringify(Array.from(downloaded).slice(-80)));
  } catch {
    // Ignorar errores de almacenamiento del navegador
  }
};

/**
 * Main Layout - El Director Responsable (Smart Component)
 * Controla el ciclo de vida del layout compartido, la lógica responsiva y la política del Sidebar.
 */
export const MainLayout: React.FC = () => {
  const [isMobile, setIsMobile] = useState(() => {
    if (typeof window === 'undefined') return false;
    return window.innerWidth < DESKTOP_BREAKPOINT;
  });
  const [isDesktopSidebarCollapsed, setIsDesktopSidebarCollapsed] = useState(readSidebarPreference);
  const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false);
  const [autoDownloadBackups, setAutoDownloadBackups] = useState(DevicePreferencesService.shouldAutoDownloadBackups);
  const downloadedBackupsRef = useRef<Set<string>>(readDownloadedBackups());

  const { hasAnyRole, hasAnyPermission, hasPermission, user, roles, permissions } = useAuth();
  const location = useLocation();

  const notificationTopics = useMemo(() => {
    const topics = new Set<string>(['/topic/espera']);
    if (user?.preferred_username && hasAnyRole(['ROLE_MEDICO'])) {
      topics.add(`/topic/medico/${user.preferred_username}`);
    }
    if (
      hasAnyRole(['ROLE_ADMIN']) ||
      hasAnyPermission([
        'admin.users.view',
        'admin.users.manage',
        'admin.users.export',
        'admin.roles.view',
        'admin.roles.manage',
        'admin.roles.export',
        'admin.database.view',
        'admin.database.export',
        'admin.database.restore',
      ])
    ) {
      topics.add('/topic/admin/system');
    }
    return Array.from(topics);
  }, [hasAnyPermission, hasAnyRole, user?.preferred_username]);

  const { notificaciones, clearNotificacion, clearAll } = useWebSocket(notificationTopics);

  const canDownloadDatabaseBackups = hasAnyRole(['ROLE_ADMIN']) || hasPermission('admin.database.export');

  const downloadAutomaticBackupToDevice = useCallback(
    async (filename?: string | null) => {
      if (!filename || !canDownloadDatabaseBackups || !autoDownloadBackups) {
        return;
      }

      if (typeof navigator !== 'undefined' && !navigator.onLine) {
        return;
      }

      if (downloadedBackupsRef.current.has(filename)) {
        return;
      }

      try {
        await DatabaseAdminService.downloadStoredBackup(filename);
        downloadedBackupsRef.current.add(filename);
        rememberDownloadedBackup(filename);
      } catch (error) {
        console.error('No se pudo descargar el respaldo automático en este dispositivo:', error);
      }
    },
    [autoDownloadBackups, canDownloadDatabaseBackups],
  );

  const toggleSidebar = useCallback(() => {
    if (isMobile) {
      setIsMobileSidebarOpen(prev => !prev);
      return;
    }

    setIsDesktopSidebarCollapsed(prev => {
      const nextValue = !prev;
      writeSidebarPreference(nextValue);
      return nextValue;
    });
  }, [isMobile]);

  const isSidebarCollapsed = isMobile ? !isMobileSidebarOpen : isDesktopSidebarCollapsed;

  useEffect(() => {
    if (isMobile) return;
    writeSidebarPreference(isDesktopSidebarCollapsed);
  }, [isDesktopSidebarCollapsed, isMobile]);

  const closeMobileSidebar = useCallback(() => {
    setIsMobileSidebarOpen(false);
  }, []);

  // ── Cerrar sidebar en mobile al cambiar de ruta ──
  useEffect(() => {
    if (isMobile) {
      setIsMobileSidebarOpen(false);
    }
  }, [isMobile, location.pathname]);

  // ── Responsividad: separar estado móvil del mini-sidebar persistido en desktop ──
  useEffect(() => {
    const handleResize = () => {
      const nextIsMobile = window.innerWidth < DESKTOP_BREAKPOINT;

      setIsMobile(prevIsMobile => {
        if (prevIsMobile !== nextIsMobile && nextIsMobile) {
          setIsMobileSidebarOpen(false);
        }
        return nextIsMobile;
      });

      if (!nextIsMobile) {
        setIsDesktopSidebarCollapsed(readSidebarPreference());
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // ── Filtrado RBAC ──
  // Solo muestra los ítems de navegación que el rol actual del usuario permite ver.
  // Si roles cambia (ej. refresh de sesión), el memo se recalcula automáticamente.
  const allowedNavigation = useMemo(() => {
    return navigationConfig
      .map(group => ({
        ...group,
        items: group.items.filter(item => {
          return canAccessRequirement(roles, permissions, item);
        })
      }))
      .filter(group => group.items.length > 0);
  }, [permissions, roles]);

  useEffect(() => {
    const syncPreference = () => {
      setAutoDownloadBackups(DevicePreferencesService.shouldAutoDownloadBackups());
    };

    window.addEventListener(DevicePreferencesService.devicePreferencesEvent, syncPreference);
    return () => window.removeEventListener(DevicePreferencesService.devicePreferencesEvent, syncPreference);
  }, []);

  useEffect(() => {
    notificaciones
      .filter(notification => notification.tipo === 'RESPALDO_AUTOMATICO' && notification.archivoDescarga)
      .forEach(notification => {
        void downloadAutomaticBackupToDevice(notification.archivoDescarga);
      });
  }, [downloadAutomaticBackupToDevice, notificaciones]);

  useEffect(() => {
    if (!canDownloadDatabaseBackups || !autoDownloadBackups) {
      return;
    }

    let cancelled = false;

    const syncPendingAutomaticBackup = async () => {
      if (typeof navigator !== 'undefined' && !navigator.onLine) {
        return;
      }

      try {
        const summary = await DatabaseAdminService.getSummary();
        if (cancelled) {
          return;
        }
        const latestAutomaticBackup = summary.backups.find(backup => backup.automatic);
        if (latestAutomaticBackup && !downloadedBackupsRef.current.has(latestAutomaticBackup.filename)) {
          await downloadAutomaticBackupToDevice(latestAutomaticBackup.filename);
        }
      } catch {
        // Si el servidor no está disponible, se reintenta al recuperar conexión/foco.
      }
    };

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        void syncPendingAutomaticBackup();
      }
    };

    void syncPendingAutomaticBackup();
    const interval = window.setInterval(syncPendingAutomaticBackup, 60000);
    window.addEventListener('online', syncPendingAutomaticBackup);
    window.addEventListener('focus', syncPendingAutomaticBackup);
    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      cancelled = true;
      window.clearInterval(interval);
      window.removeEventListener('online', syncPendingAutomaticBackup);
      window.removeEventListener('focus', syncPendingAutomaticBackup);
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [autoDownloadBackups, canDownloadDatabaseBackups, downloadAutomaticBackupToDevice]);

  return (
    <div className="flex h-screen bg-slate-50 dark:bg-[#0b1a24] w-full overflow-hidden font-sans selection:bg-sky-500/30 selection:text-sky-900 dark:selection:text-sky-100">
      
      {/* Sidebar - Componente Tonto puramente renderizador */}
      <Sidebar 
        groups={allowedNavigation} 
        isCollapsed={isSidebarCollapsed}
        onCloseMobile={isMobile ? closeMobileSidebar : undefined}
      />

      {/* Contenedor Flex para Navbar Superior y Contenido Central */}
      <div className="flex-1 flex flex-col min-w-0 h-full overflow-hidden relative transition-all duration-300">
        
        {/* Navbar */}
        <Navbar 
          isSidebarCollapsed={isSidebarCollapsed} 
          onToggleSidebar={toggleSidebar} 
          notifications={notificaciones}
          onDismissNotification={clearNotificacion}
          onClearNotifications={clearAll}
        />

        {/* Slot dinámico donde caen las vistas internas de cada módulo */}
        <main className="flex-1 overflow-x-hidden overflow-y-auto bg-slate-50 dark:bg-[#0a151d] relative custom-scrollbar">
          <div className="container mx-auto max-w-[1600px] p-4 sm:p-6 lg:p-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <Outlet />
          </div>
        </main>

      </div>

      {/* Toast Notifications globales (WebSocket) */}
      <ToastNotification notificaciones={notificaciones} onDismiss={clearNotificacion} />
    </div>
  );
};
