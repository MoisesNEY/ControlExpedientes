import React from 'react';
import { NavLink } from 'react-router-dom';
import type { NavGroup } from '../../config/navigation';

interface SidebarProps {
  groups: NavGroup[];
  isCollapsed: boolean;
  onCloseMobile?: () => void;
}

/**
 * Componente (Presentational) puro de renderizado lateral.
 * Depende totalmente de las Props proporcionadas por su Contenedor.
 */
export const Sidebar: React.FC<SidebarProps> = ({ groups, isCollapsed, onCloseMobile }) => {

  return (
    <>
      {/* Backdrop (Fondo oscuro) manejado en Mobile */}
      {!isCollapsed && onCloseMobile && (
        <div 
          className="fixed inset-0 z-30 bg-slate-900/50 backdrop-blur-sm lg:hidden transition-opacity"
          onClick={onCloseMobile}
          aria-hidden="true"
        />
      )}

      {/* Contenedor del Sidebar con transiciones */}
      <aside 
        className={`fixed lg:static inset-y-0 left-0 z-40 flex flex-col bg-[#071e2b] text-slate-300 
          transition-all duration-300 ease-in-out border-r border-white/5 shadow-xl lg:shadow-none
          ${isCollapsed ? '-translate-x-full lg:translate-x-0 lg:w-20' : 'translate-x-0 w-72'}
        `}
      >
        {/* Cabecera Sidebar (Logo & Marca) */}
        <div className="h-16 flex items-center justify-center border-b border-white/5 shrink-0 relative overflow-hidden">
          <div className="absolute inset-0 bg-gradient-to-r from-sky-600/10 to-indigo-600/10 pointer-events-none"></div>
          
          <div className="w-8 h-8 rounded-lg bg-sky-500/10 border border-sky-500/20 flex items-center justify-center shrink-0 z-10 transition-transform hover:scale-105">
            <span className="material-symbols-outlined text-sky-400 text-[18px]">local_hospital</span>
          </div>
          
          <div className={`ml-3 overflow-hidden transition-all duration-300 flex flex-col justify-center z-10 ${isCollapsed ? 'lg:w-0 lg:opacity-0' : 'w-auto opacity-100'}`}>
            <span className="text-white text-base font-black tracking-tight leading-none">ClinData</span>
            <span className="text-sky-500/60 text-[9px] font-bold uppercase tracking-widest mt-[2px] leading-none">Health</span>
          </div>

          {/* Botón de cierre explícito para Móvil */}
          {onCloseMobile && !isCollapsed && (
            <button 
              onClick={onCloseMobile} 
              className="absolute right-4 lg:hidden text-slate-400 hover:text-white"
            >
              <span className="material-symbols-outlined">close</span>
            </button>
          )}
        </div>

        {/* Nodos de Navegación */}
        <div className="flex-1 overflow-y-auto overflow-x-hidden pt-6 pb-4 space-y-7 custom-scrollbar hide-scrollbar-mobile">
          {groups.map((group, groupIndex) => (
            <div key={groupIndex} className="px-3">
              {/* Etiqueta del módulo/grupo */}
              <h3 className={`mb-2.5 px-3 text-[10px] font-black text-slate-500/70 uppercase tracking-widest transition-opacity duration-300 ${isCollapsed ? 'lg:opacity-0 lg:hidden' : 'opacity-100'}`}>
                {group.groupName}
              </h3>
              
              <ul className="space-y-1">
                {group.items.map(item => (
                  <li key={item.id}>
                    <NavLink
                      to={item.path}
                      onClick={onCloseMobile} // Cierra en mobile
                      // Exact para evitar que '/admin' encienda a todas sus hijas al abrir el panel, etc.
                      // En tu caso es útil si hay jerarquías exactas, pero 'end' a veces interfiere con vistas anidadas.
                      // Simplificaremos dejando React Router resolver el `isActive`.
                      className={({ isActive }) => `
                        group flex items-center rounded-lg px-3 py-2.5 transition-all duration-200 relative cursor-pointer
                        ${isActive 
                          ? 'bg-sky-500/10 text-sky-400 shadow-[inset_2px_0_0_0_#38bdf8]' 
                          : 'hover:bg-white/5 hover:text-slate-100'
                        }
                        ${isCollapsed ? 'lg:justify-center' : 'justify-start'}
                      `}
                    >
                      {({ isActive }) => (
                        <>
                          <span className={`material-symbols-outlined shrink-0 text-[19px] transition-colors ${isActive ? 'text-sky-400' : 'text-slate-400 group-hover:text-slate-300'} ${isCollapsed ? '' : 'mr-3'}`}>
                            {item.icon}
                          </span>
                          
                          <span className={`text-[13px] font-medium whitespace-nowrap transition-all duration-300 ${isCollapsed ? 'lg:w-0 lg:opacity-0' : 'w-auto opacity-100'} ${isActive ? 'font-semibold' : ''}`}>
                            {item.label}
                          </span>

                          {/* Tooltip Dinámico (Aparece en Desktop colapsado al hacer Hover) */}
                          {isCollapsed && (
                            <div className="absolute left-full ml-4 px-2.5 py-1.5 bg-slate-800 text-white text-[11px] font-bold rounded-md opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all whitespace-nowrap z-50 shadow-xl border border-white/10 hidden lg:block">
                              {item.label}
                              <div className="absolute top-1/2 -left-1 -mt-1 w-2 h-2 bg-slate-800 border-l border-b border-white/10 origin-top-left -rotate-45"></div>
                            </div>
                          )}
                        </>
                      )}
                    </NavLink>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
        
        {/* Footer (Estado Operativo) */}
        <div className={`p-4 border-t border-white/5 transition-all duration-300 mt-auto ${isCollapsed ? 'lg:p-3 lg:flex lg:justify-center' : ''}`}>
           <div className={`flex items-center gap-2.5 ${isCollapsed ? 'lg:justify-center' : ''} px-2 py-1.5 rounded-md bg-white/[0.02] border border-white/[0.04]`}>
             <div className="w-2 h-2 rounded-full bg-emerald-500/80 shadow-[0_0_8px_rgba(16,185,129,0.5)] animate-pulse shrink-0"></div>
             <span className={`text-[10px] font-medium text-slate-400 truncate ${isCollapsed ? 'lg:hidden' : ''}`}>
               Sistema Conectado
             </span>
           </div>
        </div>
      </aside>
    </>
  );
};
