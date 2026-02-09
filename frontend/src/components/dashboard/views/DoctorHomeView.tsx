import React from 'react';
import { usePatient } from '../../../context/PatientContext';

const StatCard = ({ label, value, icon, color }: any) => (
    <div className="bg-white dark:bg-slate-900 p-5 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 flex items-center gap-4 transition-colors">
        <div className={`${color} size-12 rounded-lg flex items-center justify-center text-white shadow-lg shadow-current/20`}>
            <span className="material-symbols-outlined">{icon}</span>
        </div>
        <div>
            <p className="text-slate-500 dark:text-slate-400 text-xs font-bold uppercase tracking-wider">{label}</p>
            <p className="text-2xl font-black text-slate-900 dark:text-white leading-none mt-1">{value}</p>
        </div>
    </div>
);

const DoctorHomeView = () => {
    const { selectPatient } = usePatient();

    const handleAttend = (pacienteName: string) => {
        selectPatient({
            id: 'PX-9928',
            name: pacienteName,
            age: '45 años',
            gender: 'Masculino',
            status: 'Activo',
            image: `https://i.pravatar.cc/150?u=${pacienteName}`
        });
    };

    const agenda = [
        { time: '09:00 AM', patient: 'Maria Garcia', reason: 'Control Hipertensión', status: 'Completado' },
        { time: '10:30 AM', patient: 'Juan Pérez', reason: 'Dolor Lumbar', status: 'En Espera', active: true },
        { time: '11:15 AM', patient: 'Ana Martínez', reason: 'Seguimiento Post-operatorio', status: 'Confirmado' },
        { time: '12:00 PM', patient: 'Roberto Smith', reason: 'Chequeo General', status: 'Confirmado' },
    ];

    return (
        <div className="p-8 max-w-5xl mx-auto w-full flex flex-col gap-8 transition-colors duration-300">
            {/* Bienvenida */}
            <div className="flex flex-col gap-1">
                <h2 className="text-slate-900 dark:text-white text-3xl font-black tracking-tight">¡Buen día, Dr. Morales!</h2>
                <p className="text-slate-500 font-medium tracking-tight">Aquí tienes el resumen de tu jornada para hoy, 09 de febrero.</p>
            </div>

            {/* Estadísticas Rápidas */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <StatCard label="Citas Hoy" value="12" icon="calendar_month" color="bg-primary" />
                <StatCard label="Pacientes Atendidos" value="5" icon="check_circle" color="bg-success" />
                <StatCard label="Nuevos Expedientes" value="2" icon="person_add" color="bg-orange-500" />
            </div>

            {/* Agenda Detallada */}
            <div className="bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 overflow-hidden">
                <div className="p-6 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center transition-colors">
                    <h3 className="text-slate-900 dark:text-white font-bold flex items-center gap-2">
                        <span className="material-symbols-outlined text-primary">list_alt</span>
                        Agenda del Día
                    </h3>
                    <button className="text-xs font-bold text-primary hover:underline">Ver calendario completo</button>
                </div>
                <div className="overflow-x-auto">
                    <table className="w-full text-left">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 uppercase text-[10px] font-black tracking-widest">
                            <tr>
                                <th className="px-6 py-4">Hora</th>
                                <th className="px-6 py-4">Paciente</th>
                                <th className="px-6 py-4">Motivo</th>
                                <th className="px-6 py-4">Estado</th>
                                <th className="px-6 py-4 text-right">Acción</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                            {agenda.map((item, idx) => (
                                <tr key={idx} className={`group hover:bg-slate-50 dark:hover:bg-slate-800/30 transition-colors ${item.active ? 'bg-primary/5' : ''}`}>
                                    <td className="px-6 py-4 text-sm font-bold text-slate-900 dark:text-white">{item.time}</td>
                                    <td className="px-6 py-4">
                                        <div className="flex items-center gap-2">
                                            <div className="size-8 rounded-full bg-slate-200 dark:bg-slate-700 overflow-hidden shadow-inner">
                                                <img src={`https://i.pravatar.cc/150?u=${item.patient}`} alt="" />
                                            </div>
                                            <span className="text-sm font-semibold text-slate-700 dark:text-slate-300 transition-colors">{item.patient}</span>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 text-sm text-slate-500 dark:text-slate-400 transition-colors">{item.reason}</td>
                                    <td className="px-6 py-4">
                                        <span className={`px-2 py-1 rounded-md text-[10px] font-black uppercase tracking-tight ${item.status === 'Completado' ? 'bg-success/10 text-success' :
                                                item.status === 'En Espera' ? 'bg-primary/20 text-primary animate-pulse' :
                                                    'bg-slate-100 dark:bg-slate-800 text-slate-500'
                                            }`}>
                                            {item.status}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 text-right">
                                        <button
                                            onClick={() => handleAttend(item.patient)}
                                            className={`px-4 py-1.5 rounded-lg text-xs font-bold transition-all ${item.active ? 'bg-primary text-white shadow-lg shadow-primary/30 hover:scale-105' : 'text-primary hover:bg-primary/10'
                                                }`}
                                        >
                                            {item.status === 'En Espera' ? 'Atender' : 'Ver Detalles'}
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default DoctorHomeView;
