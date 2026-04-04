import React, { useState, useMemo, useEffect, useCallback } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Navbar } from './Navbar';
import { navigationConfig } from '../../config/navigation';
import { useAuth } from '../../context/AuthContext';
import { useWebSocket } from '../../hooks/useWebSocket';
import ToastNotification from '../ui/ToastNotification';

const SIDEBAR_STORAGE_KEY = 'scan-sidebar-collapsed';

/**
 * Main Layout - El Director Responsable (Smart Component)
 * Controla el ciclo de vida del layout compartido, la lógica responsiva y la política del Sidebar.
 */
export const MainLayout: React.FC = () => {

  // ── Estado del Sidebar con persistencia en localStorage ──
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(() => {
    // En mobile siempre inicia colapsado
    if (typeof window !== 'undefined' && window.innerWidth < 1024) return true;
    // En desktop, leer preferencia guardada
    const saved = localStorage.getItem(SIDEBAR_STORAGE_KEY);
    return saved !== null ? saved === 'true' : false;
  });

  const { hasAnyRole, roles } = useAuth();
  const location = useLocation();

  // WebSocket: suscribirse a notificaciones de sala de espera
  const { notificaciones, clearNotificacion } = useWebSocket('/topic/espera');

  // ── Persistir estado del sidebar en localStorage (solo desktop) ──
  const toggleSidebar = useCallback(() => {
    setIsSidebarCollapsed(prev => {
      const newValue = !prev;
      // Solo persistir en desktop
      if (window.innerWidth >= 1024) {
        localStorage.setItem(SIDEBAR_STORAGE_KEY, String(newValue));
      }
      return newValue;
    });
  }, []);

  // ── Cerrar sidebar en mobile al cambiar de ruta ──
  useEffect(() => {
    if (window.innerWidth < 1024) {
      setIsSidebarCollapsed(true);
    }
  }, [location.pathname]);

  // ── Responsividad: solo aplica cambio automático al cruzar breakpoint ──
  useEffect(() => {
    let lastWasMobile = window.innerWidth < 1024;

    const handleResize = () => {
      const isMobile = window.innerWidth < 1024;

      // Solo actuar cuando se CRUZA el breakpoint (no en cada resize)
      if (isMobile && !lastWasMobile) {
        // Se hizo móvil: colapsar
        setIsSidebarCollapsed(true);
      } else if (!isMobile && lastWasMobile) {
        // Se hizo desktop: restaurar preferencia guardada
        const saved = localStorage.getItem(SIDEBAR_STORAGE_KEY);
        setIsSidebarCollapsed(saved === 'true');
      }

      lastWasMobile = isMobile;
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
        items: group.items.filter(item => hasAnyRole(item.requiredRoles))
      }))
      .filter(group => group.items.length > 0);
  }, [hasAnyRole, roles]); // roles como dependencia extra para reaccionar a cambios de sesión

  return (
    <div className="flex h-screen bg-slate-50 dark:bg-[#0b1a24] w-full overflow-hidden font-sans selection:bg-sky-500/30 selection:text-sky-900 dark:selection:text-sky-100">
      
      {/* Sidebar - Componente Tonto puramente renderizador */}
      <Sidebar 
        groups={allowedNavigation} 
        isCollapsed={isSidebarCollapsed}
        onCloseMobile={() => setIsSidebarCollapsed(true)}
      />

      {/* Contenedor Flex para Navbar Superior y Contenido Central */}
      <div className="flex-1 flex flex-col min-w-0 h-full overflow-hidden relative transition-all duration-300">
        
        {/* Navbar */}
        <Navbar 
          isSidebarCollapsed={isSidebarCollapsed} 
          onToggleSidebar={toggleSidebar} 
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
