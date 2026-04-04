import React from 'react';

export type CitaStatus = 'PROGRAMADA' | 'EN_SALA_ESPERA' | 'EN_TRIAGE' | 'ESPERANDO_MEDICO' | 'EN_CONSULTA' | 'CANCELADA' | 'ATENDIDA';

interface StatusBadgeProps {
    status: CitaStatus | string;
    className?: string;
}

const statusConfig: Record<string, { label: string; color: string; icon: string }> = {
    PROGRAMADA: { 
        label: 'Programada', 
        color: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400', 
        icon: 'event' 
    },
    EN_SALA_ESPERA: { 
        label: 'En Recepción', 
        color: 'bg-purple-50 text-purple-600 dark:bg-purple-500/10 dark:text-purple-400', 
        icon: 'airline_seat_recline_normal' 
    },
    EN_TRIAGE: { 
        label: 'En Triage', 
        color: 'bg-indigo-50 text-indigo-600 dark:bg-indigo-500/10 dark:text-indigo-400', 
        icon: 'monitor_heart' 
    },
    ESPERANDO_MEDICO: { 
        label: 'Esperando Médico', 
        color: 'bg-amber-50 text-amber-600 dark:bg-amber-500/10 dark:text-amber-500', 
        icon: 'hourglass_empty' 
    },
    EN_CONSULTA: { 
        label: 'En Consulta', 
        color: 'bg-sky-50 text-sky-600 dark:bg-sky-500/10 dark:text-sky-400', 
        icon: 'stethoscope' 
    },
    ATENDIDA: { 
        label: 'Atendida', 
        color: 'bg-emerald-50 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400', 
        icon: 'check_circle' 
    },
    CANCELADA: { 
        label: 'Cancelada', 
        color: 'bg-rose-50 text-rose-600 dark:bg-rose-500/10 dark:text-rose-400', 
        icon: 'cancel' 
    }
};

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, className = '' }) => {
    // Normalizar a mayúsculas para emparejar con el Enum de Java
    const normalizedStatus = status?.toString().toUpperCase();
    const config = statusConfig[normalizedStatus] || { 
        label: status || 'Desconocido', 
        color: 'bg-slate-100 text-slate-500 dark:bg-slate-800', 
        icon: 'help' 
    };

    return (
        <span className={`px-2.5 py-1.5 rounded-lg text-xs font-black uppercase tracking-tight flex items-center gap-1.5 w-fit ${config.color} ${className}`}>
            <span className="material-symbols-outlined text-[14px]">{config.icon}</span>
            {config.label}
        </span>
    );
};
