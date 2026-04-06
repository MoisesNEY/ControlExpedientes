import type { InteraccionMedicamentosaDTO } from '../../services/interaccion.service';

interface DrugInteractionAlertProps {
    interacciones: InteraccionMedicamentosaDTO[];
    onDismiss?: () => void;
}

const severityConfig = {
    GRAVE: {
        icon: '⚠️',
        bg: 'bg-red-50 dark:bg-red-900/20',
        border: 'border-red-500',
        text: 'text-red-800 dark:text-red-300',
        badge: 'bg-red-600 text-white',
        animate: 'animate-pulse',
    },
    MODERADA: {
        icon: '⚡',
        bg: 'bg-amber-50 dark:bg-amber-900/20',
        border: 'border-amber-500',
        text: 'text-amber-800 dark:text-amber-300',
        badge: 'bg-amber-500 text-white',
        animate: '',
    },
    LEVE: {
        icon: 'ℹ️',
        bg: 'bg-blue-50 dark:bg-blue-900/20',
        border: 'border-blue-500',
        text: 'text-blue-800 dark:text-blue-300',
        badge: 'bg-blue-500 text-white',
        animate: '',
    },
};

export default function DrugInteractionAlert({ interacciones, onDismiss }: DrugInteractionAlertProps) {
    if (!interacciones || interacciones.length === 0) return null;

    return (
        <div className="mb-4 space-y-3">
            <div className="flex items-center justify-between">
                <h4 className="text-sm font-bold text-slate-700 dark:text-slate-200 flex items-center gap-2">
                    ⚠️ Alertas de Interacción Medicamentosa
                    <span className="inline-flex items-center justify-center px-2 py-0.5 text-xs font-bold rounded-full bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-300">
                        {interacciones.length}
                    </span>
                </h4>
                {onDismiss && (
                    <button
                        onClick={onDismiss}
                        className="text-xs text-slate-400 hover:text-slate-600 dark:hover:text-slate-300 transition-colors"
                    >
                        Descartar
                    </button>
                )}
            </div>

            {interacciones.map((interaccion) => {
                const config = severityConfig[interaccion.severidad];
                return (
                    <div
                        key={interaccion.id}
                        className={`border-l-4 rounded-lg p-3 ${config.bg} ${config.border} ${config.animate}`}
                    >
                        <div className="flex items-start gap-2">
                            <span className="text-lg leading-none">{config.icon}</span>
                            <div className="flex-1 min-w-0">
                                <div className="flex items-center gap-2 mb-1">
                                    <span className={`text-[10px] font-black uppercase tracking-wider px-1.5 py-0.5 rounded ${config.badge}`}>
                                        {interaccion.severidad}
                                    </span>
                                    <span className={`text-sm font-semibold ${config.text}`}>
                                        Interacción entre {interaccion.medicamentoA.nombre} y {interaccion.medicamentoB.nombre}
                                    </span>
                                </div>
                                <p className={`text-xs ${config.text} opacity-90`}>
                                    {interaccion.descripcion}
                                </p>
                                {interaccion.recomendacion && (
                                    <p className={`text-xs ${config.text} opacity-75 mt-1 italic`}>
                                        {interaccion.recomendacion}
                                    </p>
                                )}
                            </div>
                        </div>
                    </div>
                );
            })}
        </div>
    );
}
