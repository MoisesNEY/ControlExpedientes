import React, { useState, useMemo, useEffect, useCallback, useRef } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Navbar } from './Navbar';
import { navigationConfig } from '../../config/navigation';
import { useAuth } from '../../context/AuthContext';
import { useWebSocket } from '../../hooks/useWebSocket';
import ToastNotification from '../ui/ToastNotification';
import { DatabaseAdminService } from '../../services/database-admin.service';
import { canAccessRequirement } from '../../utils/accessControl';

const SIDEBAR_STORAGE_KEY = 'scan-sidebar-collapsed';
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
  const downloadedBackupsRef = useRef<Set<string>>(new Set());

  const { hasAnyRole, hasAnyPermission, hasPermission, user, roles, permissions } = useAuth();
  const location = useLocation();

  const notificationTopics = useMemo(() => {
    const topics = new Set<string>(['/topic/espera']);
    if (user?.preferred_username && hasAnyRole(['ROLE_MEDICO'])) {
      topics.add(`/topic/medico/${user.preferred_username}`);
    }
    if (hasAnyRole(['ROLE_ADMIN']) || hasAnyPermission(['admin.users.manage', 'admin.roles.manage', 'admin.database.view'])) {
      topics.add('/topic/admin/system');
    }
    return Array.from(topics);
  }, [hasAnyPermission, hasAnyRole, user?.preferred_username]);

  const { notificaciones, clearNotificacion, clearAll } = useWebSocket(notificationTopics);

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
    const latestNotification = notificaciones[0];
    if (!latestNotification?.archivoDescarga || !hasPermission('admin.database.export')) {
      return;
    }

    if (latestNotification.tipo !== 'RESPALDO_AUTOMATICO') {
      return;
    }

    if (downloadedBackupsRef.current.has(latestNotification.archivoDescarga)) {
      return;
    }

    downloadedBackupsRef.current.add(latestNotification.archivoDescarga);

    void DatabaseAdminService.downloadStoredBackup(latestNotification.archivoDescarga).catch(() => undefined);
  }, [hasPermission, notificaciones]);

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
