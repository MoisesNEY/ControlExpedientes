import { useEffect, useState } from 'react';
import type { Notificacion } from '../../hooks/useWebSocket';

interface ToastNotificationProps {
    notificaciones: Notificacion[];
    onDismiss: (index: number) => void;
}

/**
 * Componente de Toast Notifications flotante.
 * Muestra notificaciones push del WebSocket con auto-dismiss.
 */
const ToastNotification = ({ notificaciones, onDismiss }: ToastNotificationProps) => {
    return (
        <div className="fixed bottom-6 right-6 z-50 flex flex-col gap-3 max-w-sm">
            {notificaciones.slice(0, 5).map((noti, idx) => (
                <ToastItem key={`${noti.citaId}-${noti.timestamp}-${idx}`} noti={noti} index={idx} onDismiss={onDismiss} />
            ))}
        </div>
    );
};

const ToastItem = ({ noti, index, onDismiss }: { noti: Notificacion; index: number; onDismiss: (i: number) => void }) => {
    const [isVisible, setIsVisible] = useState(false);

    useEffect(() => {
        // Animación de entrada
        requestAnimationFrame(() => setIsVisible(true));

        // Auto-dismiss después de 8 segundos
        const timer = setTimeout(() => {
            setIsVisible(false);
            setTimeout(() => onDismiss(index), 300);
        }, 8000);

        return () => clearTimeout(timer);
    }, [index, onDismiss]);

    const iconMap: Record<string, string> = {
        PACIENTE_LISTO: 'person_check',
        CONSULTA_FINALIZADA: 'task_alt',
    };

    const colorMap: Record<string, string> = {
        PACIENTE_LISTO: 'bg-teal-50 dark:bg-teal-500/10 border-teal-200 dark:border-teal-500/30 text-teal-800 dark:text-teal-200',
        CONSULTA_FINALIZADA: 'bg-blue-50 dark:bg-blue-500/10 border-blue-200 dark:border-blue-500/30 text-blue-800 dark:text-blue-200',
    };

    const icon = iconMap[noti.tipo] || 'notifications';
    const colorClasses = colorMap[noti.tipo] || 'bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 text-slate-800 dark:text-slate-200';

    return (
        <div
            className={`
                flex items-start gap-3 p-4 rounded-2xl shadow-xl border backdrop-blur-sm
                transition-all duration-300 ease-out cursor-pointer
                ${colorClasses}
                ${isVisible ? 'translate-x-0 opacity-100' : 'translate-x-full opacity-0'}
            `}
            onClick={() => {
                setIsVisible(false);
                setTimeout(() => onDismiss(index), 300);
            }}
        >
            <span className="material-symbols-outlined text-xl shrink-0 mt-0.5">{icon}</span>
            <div className="flex-1 min-w-0">
                <p className="font-bold text-sm">{noti.pacienteNombre || 'Notificación'}</p>
                <p className="text-xs opacity-80 mt-0.5">{noti.mensaje}</p>
            </div>
            <button
                className="shrink-0 opacity-50 hover:opacity-100 transition-opacity"
                onClick={(e) => {
                    e.stopPropagation();
                    setIsVisible(false);
                    setTimeout(() => onDismiss(index), 300);
                }}
            >
                <span className="material-symbols-outlined text-[16px]">close</span>
            </button>
        </div>
    );
};

export default ToastNotification;
