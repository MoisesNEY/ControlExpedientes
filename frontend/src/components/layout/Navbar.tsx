import React, { useState, useEffect, useRef } from 'react';
import { useLocation, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { AppointmentService, type Appointment } from '../../services/appointment.service';
import type { Notificacion } from '../../hooks/useWebSocket';

interface NavbarProps {
  onToggleSidebar: () => void;
  isSidebarCollapsed: boolean;
  notifications: Notificacion[];
  onDismissNotification: (index: number) => void;
  onClearNotifications: () => void;
}

/**
 * Navbar (Header) con Breadcrumbs basados en las rutas y panel de usuario.
 */
export const Navbar: React.FC<NavbarProps> = ({
  onToggleSidebar,
  isSidebarCollapsed,
  notifications,
  onDismissNotification,
  onClearNotifications,
}) => {
  const { user, roles, logout, hasAnyRole } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);
  const [notificationPanelMessage, setNotificationPanelMessage] = useState<string | null>(null);
  const [activeConsultation, setActiveConsultation] = useState<Appointment | null>(null);
  const profileRef = useRef<HTMLDivElement>(null);
  const notificationsRef = useRef<HTMLDivElement>(null);

  // Generar breadcrumbs dinámicos básicos
  const getBreadcrumbs = () => {
    const paths = location.pathname.split('/').filter(p => p !== '');
    if (paths.length === 0) return [{ label: 'Inicio', path: '/', isLast: true }];
    
    let currentPath = '';
    return paths.map((p, index) => {
      currentPath += `/${p}`;
      const isLast = index === paths.length - 1;
      const label = p.charAt(0).toUpperCase() + p.slice(1).replace(/-/g, ' ');
      return { label, path: currentPath, isLast };
    });
  };

  const breadcrumbs = getBreadcrumbs();

  // Cerrar menú si clickean fuera
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (profileRef.current && !profileRef.current.contains(event.target as Node)) {
         setIsProfileOpen(false);
       }
      if (notificationsRef.current && !notificationsRef.current.contains(event.target as Node)) {
         setIsNotificationsOpen(false);
       }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  useEffect(() => {
    if (!user?.id || !hasAnyRole(['ROLE_MEDICO'])) {
      setActiveConsultation(null);
      return;
    }

    let isMounted = true;

    const syncActiveConsultation = async () => {
      try {
        const active = await AppointmentService.getActiveConsultation(user.id);
        if (!isMounted) return;

        setActiveConsultation(active);

        if (active?.id) {
          try {
            localStorage.setItem('activeConsultation', String(active.id));
          } catch {
            // ignore
          }
        } else {
          try {
            localStorage.removeItem('activeConsultation');
          } catch {
            // ignore
          }
        }
      } catch {
        if (!isMounted) return;
        setActiveConsultation(null);
      }
    };

    void syncActiveConsultation();

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        void syncActiveConsultation();
      }
    };

    window.addEventListener('focus', syncActiveConsultation);
    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      isMounted = false;
      window.removeEventListener('focus', syncActiveConsultation);
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [user?.id, location.pathname, hasAnyRole]);

  const handleLogout = () => {
      setIsProfileOpen(false);
      logout();
      navigate('/login');
  };

  const handleNotificationNavigate = (notification: Notificacion) => {
    if (!notification.citaId) {
      setNotificationPanelMessage('Esta notificación no tiene un destino asociado.');
      return;
    }

    setNotificationPanelMessage(null);
    setIsNotificationsOpen(false);

    if (hasAnyRole(['ROLE_MEDICO'])) {
      navigate(`/medico/consulta/${notification.citaId}`);
      return;
    }

    if (hasAnyRole(['ROLE_ENFERMERO', 'ROLE_ADMIN'])) {
      navigate('/enfermeria/sala-espera');
    }
  };

  return (
    <header className="h-[73px] bg-white dark:bg-[#0b1a24] border-b border-slate-200 dark:border-white/[0.05] flex items-center justify-between px-4 lg:px-6 shadow-sm z-20 shrink-0 transition-all duration-300">
      
      {/* Lado Izquierdo: Toggle Sidebar + Breadcrumbs */}
      <div className="flex items-center gap-4">
        <button 
          onClick={onToggleSidebar}
          className="p-2 -ml-2 rounded-lg text-slate-500 hover:text-sky-600 hover:bg-sky-50 dark:hover:bg-white/5 transition-colors focus:outline-none"
          aria-label="Alternar menú"
        >
          <span className="material-symbols-outlined text-[22px]">
            {isSidebarCollapsed ? 'menu' : 'menu_open'}
          </span>
        </button>

        {/* Breadcrumbs - Oculto en móbiles muy pequeños */}
        <nav className="hidden sm:flex items-center text-[13px] font-medium text-slate-400 dark:text-slate-500">
          <Link to="/" className="hover:text-sky-600 dark:hover:text-sky-400 transition-colors flex items-center">
            <span className="material-symbols-outlined text-[16px]">home</span>
          </Link>
          {breadcrumbs.map((crumb) => (
            <React.Fragment key={crumb.path}>
              <span className="mx-2 material-symbols-outlined text-[14px] text-slate-300 dark:text-slate-700">chevron_right</span>
              {crumb.isLast ? (
                <span className="text-slate-800 dark:text-slate-200 font-semibold" aria-current="page">{crumb.label}</span>
              ) : (
                <Link to={crumb.path} className="hover:text-sky-600 dark:hover:text-sky-400 transition-colors">
                  {crumb.label}
                </Link>
              )}
            </React.Fragment>
          ))}
        </nav>
      </div>

      {/* Lado Derecho: Acciones y Perfil */}
      <div className="flex items-center gap-3 lg:gap-5 relative">
        
        <div className="relative hidden sm:block" ref={notificationsRef}>
          <button
            onClick={() => setIsNotificationsOpen((current) => !current)}
            className="relative p-2 text-slate-400 hover:text-sky-600 hover:bg-sky-50 dark:hover:bg-slate-800 rounded-full transition-colors"
            aria-label="Abrir notificaciones"
          >
            <span className="material-symbols-outlined text-[22px]">notifications</span>
            {notifications.length > 0 && (
              <span className="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] px-1 bg-rose-500 text-white text-[10px] font-black rounded-full ring-2 ring-white dark:ring-[#0b1a24] flex items-center justify-center">
                {notifications.length > 9 ? '9+' : notifications.length}
              </span>
            )}
          </button>

          <div className={`
             absolute right-0 mt-2 w-[360px] rounded-2xl bg-white dark:bg-slate-800 shadow-[0_10px_40px_-10px_rgba(0,0,0,0.15)]
             border border-slate-100 dark:border-white/5 z-50 overflow-hidden transition-all duration-200 origin-top-right
             ${isNotificationsOpen ? 'scale-100 opacity-100' : 'scale-95 opacity-0 invisible'}
          `}>
            <div className="px-5 py-4 border-b border-slate-100 dark:border-white/5 bg-slate-50/50 dark:bg-white/[0.02] flex items-center justify-between gap-3">
              <div>
                <p className="text-[13px] font-bold text-slate-900 dark:text-white">Notificaciones</p>
                <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5">
                  {notifications.length ? `${notifications.length} pendiente(s)` : 'Sin pendientes'}
                </p>
              </div>
              {notifications.length > 0 && (
                <button
                  onClick={onClearNotifications}
                  className="text-[11px] font-black uppercase tracking-widest text-sky-500 hover:text-sky-600"
                >
                  Limpiar
                </button>
              )}
            </div>

            {notificationPanelMessage && (
              <div className="px-5 py-3 text-xs font-medium text-amber-700 bg-amber-50 border-b border-amber-100 dark:bg-amber-500/10 dark:text-amber-200 dark:border-amber-500/20">
                {notificationPanelMessage}
              </div>
            )}

            {notifications.length > 0 ? (
              <div className="max-h-[360px] overflow-y-auto">
                {notifications.slice(0, 8).map((notification, index) => (
                  <div key={`${notification.citaId}-${notification.timestamp}-${index}`} className="border-b border-slate-100 dark:border-white/5 last:border-b-0">
                    <div className="flex items-start gap-3 px-5 py-4 hover:bg-slate-50 dark:hover:bg-white/5 transition-colors">
                      <button
                        onClick={() => handleNotificationNavigate(notification)}
                        className="flex-1 min-w-0 text-left"
                      >
                        <p className="text-sm font-bold text-slate-900 dark:text-white truncate">{notification.pacienteNombre || 'Notificación'}</p>
                        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">{notification.mensaje}</p>
                        <p className="text-[11px] text-slate-400 mt-2">{new Date(notification.timestamp).toLocaleString('es-NI')}</p>
                      </button>
                      <button
                        onClick={() => onDismissNotification(index)}
                        className="text-slate-400 hover:text-rose-500 transition-colors"
                        aria-label="Descartar notificación"
                      >
                        <span className="material-symbols-outlined text-[18px]">close</span>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="px-5 py-8 text-center">
                <span className="material-symbols-outlined text-3xl text-slate-300 dark:text-slate-600">notifications_off</span>
                <p className="mt-3 text-sm font-medium text-slate-500">No hay notificaciones recientes.</p>
              </div>
            )}
          </div>
        </div>

        {/* Reanudar consulta (si existe) */}
        {hasAnyRole(['ROLE_MEDICO']) && activeConsultation?.id && (
          <button
            onClick={() => navigate(`/medico/consulta/${activeConsultation.id}`)}
            title={`Continuar consulta de ${activeConsultation.paciente?.nombres || 'paciente'}`}
            className="hidden sm:flex items-center gap-2 px-3 py-2 rounded-full border border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 dark:border-emerald-500/20 dark:bg-emerald-500/10 dark:text-emerald-300 transition-colors"
          >
            <span className="material-symbols-outlined text-[18px]">stethoscope</span>
            <span className="text-xs font-bold uppercase tracking-wide">Continuar consulta</span>
          </button>
        )}

        <div className="h-8 w-px bg-slate-200 dark:bg-white/10 hidden sm:block"></div>

        {/* User Profile Dropdown */}
        <div className="relative" ref={profileRef}>
          <button 
            onClick={() => setIsProfileOpen(!isProfileOpen)}
            className="flex items-center gap-3 focus:outline-none rounded-xl p-1.5 hover:bg-slate-50 dark:hover:bg-white/5 border border-transparent hover:border-slate-100 dark:hover:border-white/10 transition-all"
          >
            <div className="text-right hidden md:block">
              <p className="text-[13px] font-bold text-slate-800 dark:text-white leading-tight">
                {user?.name || 'Usuario'}
              </p>
              <p className="text-[10px] font-black uppercase tracking-widest text-sky-500 mt-0.5">
                {roles[0]?.replace('ROLE_', '') || 'Guest'}
              </p>
            </div>
            {/* Avatar Genérico */}
            <div className="w-9 h-9 rounded-[10px] bg-gradient-to-tr from-sky-500 to-indigo-500 flex items-center justify-center text-white font-black text-sm shadow-md ring-2 ring-white dark:ring-[#0b1a24]">
              {user?.name?.charAt(0) || 'U'}
            </div>
            <span className={`material-symbols-outlined text-slate-400 text-[18px] hidden sm:block transition-transform duration-200 ${isProfileOpen ? 'rotate-180' : ''}`}>
               expand_more
            </span>
          </button>

          {/* Menú Desplegable Estilizado */}
          <div className={`
             absolute right-0 mt-2 w-64 rounded-xl bg-white dark:bg-slate-800 shadow-[0_10px_40px_-10px_rgba(0,0,0,0.1)] dark:shadow-[0_10px_40px_-10px_rgba(0,0,0,0.5)] 
             border border-slate-100 dark:border-white/5 z-50 overflow-hidden transition-all duration-200 origin-top-right
             ${isProfileOpen ? 'scale-100 opacity-100' : 'scale-95 opacity-0 invisible'}
          `}>
            {/* Cabecera Menu */}
            <div className="px-5 py-4 border-b border-slate-100 dark:border-white/5 bg-slate-50/50 dark:bg-white/[0.02]">
              <p className="text-[13px] font-bold text-slate-900 dark:text-white truncate leading-tight">{user?.name}</p>
              <p className="text-[11px] text-slate-500 dark:text-slate-400 truncate mt-0.5">{user?.email}</p>
            </div>
            <div className="py-2">
              <a href="#perfil" className="group flex items-center px-5 py-2.5 text-[13px] font-medium text-slate-600 dark:text-slate-300 hover:bg-sky-50 dark:hover:bg-white/5 hover:text-sky-600 dark:hover:text-white transition-colors">
                <span className="material-symbols-outlined mr-3 text-[18px] text-slate-400 group-hover:text-sky-500">person</span>
                Mi Perfil
              </a>
              <a href="#ajustes" className="group flex items-center px-5 py-2.5 text-[13px] font-medium text-slate-600 dark:text-slate-300 hover:bg-sky-50 dark:hover:bg-white/5 hover:text-sky-600 dark:hover:text-white transition-colors">
                <span className="material-symbols-outlined mr-3 text-[18px] text-slate-400 group-hover:text-sky-500">settings</span>
                Ajustes
              </a>
            </div>
            <div className="py-2 border-t border-slate-100 dark:border-white/5">
              <button 
                onClick={handleLogout}
                className="group flex w-full items-center px-5 py-2.5 text-[13px] font-bold text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-500/10 transition-colors"
              >
                <span className="material-symbols-outlined mr-3 text-[18px] group-hover:scale-110 transition-transform">logout</span>
                Cerrar Sesión
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
};
