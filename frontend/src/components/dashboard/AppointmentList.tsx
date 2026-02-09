import React from 'react';

const AppointmentList = () => {
    const appointments = [
        { time: '10:30 AM', name: 'Juan Pérez', status: 'En Progreso', active: true },
        { time: '11:15 AM', name: 'Maria Garcia', status: 'En Espera', active: false },
        { time: '12:00 PM', name: 'Robert Smith', status: 'Programada', active: false },
    ];

    return (
        <div className="p-6 border-t border-slate-100 dark:border-slate-800 transition-colors duration-300">
            <h3 className="text-slate-900 dark:text-white text-sm font-bold mb-4 flex justify-between items-center">
                <span className="flex items-center gap-2">
                    <span className="material-symbols-outlined text-primary">event_note</span>
                    Citas Médicas
                </span>
                <span className="text-[10px] text-slate-400 font-black tracking-widest uppercase">Hoy</span>
            </h3>
            <div className="flex flex-col gap-3">
                {appointments.map((apt, idx) => (
                    <div
                        key={idx}
                        className={`p-3 rounded-lg border-l-4 transition-all hover:scale-[1.02] cursor-pointer ${apt.active
                                ? 'border-primary bg-primary/5'
                                : 'border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50'
                            }`}
                    >
                        <div className="flex justify-between items-start mb-1">
                            <p className={`text-xs font-bold ${apt.active ? 'text-primary' : 'text-slate-500'}`}>
                                {apt.time}
                            </p>
                            <span
                                className={`px-1.5 py-0.5 text-[9px] font-black rounded uppercase tracking-tight ${apt.active
                                        ? 'bg-primary/10 text-primary'
                                        : apt.status === 'En Espera'
                                            ? 'bg-yellow-100 text-yellow-700'
                                            : 'bg-slate-200 text-slate-600'
                                    }`}
                            >
                                {apt.status}
                            </span>
                        </div>
                        <p className="text-sm font-semibold text-slate-900 dark:text-white">{apt.name}</p>
                    </div>
                ))}
            </div>
            <button className="w-full mt-6 py-2 border border-dashed border-slate-300 dark:border-slate-700 text-slate-500 text-[10px] font-black uppercase tracking-widest rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors">
                Ver Agenda Completa
            </button>
        </div>
    );
};

export default AppointmentList;
