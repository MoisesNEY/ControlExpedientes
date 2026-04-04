import { useState, useEffect } from 'react';
import { usePatient } from '../../../context/PatientContext';
import { ExpedienteService } from '../../../services/expediente.service';

interface TimelineEntry {
    fecha: string;
    profesional: string;
    motivo: string;
    diagnosticos: string[];
    recetas: string[];
    signosVitales?: {
        peso?: number;
        altura?: number;
        presionArterial?: string;
        temperatura?: number;
        frecuenciaCardiaca?: number;
    };
}

const RecordsView = () => {
    const { selectedPatient } = usePatient();
    const [timeline, setTimeline] = useState<TimelineEntry[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchTimeline = async () => {
            if (!selectedPatient) {
                setTimeline([]);
                return;
            }

            setLoading(true);
            setError('');
            try {
                // Obtenemos el ID del expediente base al paciente
                const pacienteId = selectedPatient.patientId ??
                    (selectedPatient.id ? parseInt(selectedPatient.id, 10) : NaN);
                if (!pacienteId || Number.isNaN(pacienteId)) {
                    setTimeline([]);
                    return;
                }

                const expediente = await ExpedienteService.getByPacienteId(pacienteId);
                if (expediente?.id) {
                    const data = await ExpedienteService.getTimeline(expediente.id);
                    setTimeline(data);
                } else {
                    setTimeline([]);
                }
            } catch (err) {
                console.error('Error fetching timeline:', err);
                setError('No se pudo cargar el historial clínico del paciente.');
                setTimeline([]);
            } finally {
                setLoading(false);
            }
        };

        fetchTimeline();
    }, [selectedPatient]);

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('es-ES', {
            day: '2-digit',
            month: 'short',
            year: 'numeric'
        });
    };

    if (!selectedPatient) {
        return (
            <div className="p-8 flex flex-col items-center justify-center h-full text-slate-400 bg-slate-50 dark:bg-slate-950/50 rounded-2xl border border-dashed border-slate-200 dark:border-slate-800 m-8">
                <span className="material-symbols-outlined text-6xl mb-4 opacity-50">quick_reference_all</span>
                <p className="font-semibold text-lg text-slate-600 dark:text-slate-300">Seleccione un paciente</p>
                <p className="text-sm mt-2 text-center max-w-sm">Busque y seleccione un paciente en el panel "Pacientes" para ver su Historial Clínico completo.</p>
            </div>
        );
    }

    return (
        <div className="p-4 md:p-8 space-y-4 md:space-y-6 pb-24 h-full flex flex-col">
            <div className="flex justify-between items-center shrink-0">
                <div>
                    <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white transition-colors">
                        Historial Clínico: <span className="text-primary">{selectedPatient.name}</span>
                    </h2>
                    <p className="text-slate-500 text-xs md:text-sm font-medium">Timeline cronológico de atenciones médicas y tratamientos.</p>
                </div>
            </div>

            {error && (
                <div className="p-4 bg-red-100 text-red-700 rounded-xl text-sm font-semibold shrink-0">
                    {error}
                </div>
            )}

            <div className="flex-1 overflow-y-auto custom-scrollbar relative">
                {loading ? (
                    <div className="space-y-8 pl-8 border-l-2 border-primary/20 ml-4 py-4 animate-pulse">
                        {[1, 2, 3].map(i => (
                            <div key={i} className="relative">
                                <div className="absolute -left-[41px] bg-white dark:bg-slate-900 border-2 border-slate-200 dark:border-slate-700 w-4 h-4 rounded-full"></div>
                                <div className="bg-slate-100 dark:bg-slate-800/50 rounded-2xl p-6 h-32 w-full max-w-2xl"></div>
                            </div>
                        ))}
                    </div>
                ) : timeline.length === 0 && !loading && !error ? (
                    <div className="text-center py-20 text-slate-400 italic">
                        <span className="material-symbols-outlined text-4xl mb-2 opacity-50 block">history_toggle_off</span>
                        El paciente no tiene consultas registradas aún.
                    </div>
                ) : (
                    <div className="space-y-8 pl-8 border-l-2 border-primary/20 ml-4 py-4">
                        {timeline.map((entry, idx) => (
                            <div key={idx} className="relative group">
                                {/* Timeline Dot */}
                                <div className="absolute -left-[41px] top-6 bg-white dark:bg-slate-900 border-4 border-primary/30 group-hover:border-primary group-hover:scale-125 transition-all w-5 h-5 rounded-full z-10"></div>

                                <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-lg transition-all rounded-2xl p-5 md:p-6 w-full max-w-4xl relative overflow-hidden">
                                    <div className="absolute top-0 right-0 p-4 opacity-5 pointer-events-none">
                                        <span className="material-symbols-outlined text-9xl">medical_services</span>
                                    </div>

                                    <div className="flex justify-between items-start mb-4">
                                        <div>
                                            <span className="bg-primary/10 text-primary font-black text-xs px-3 py-1 rounded-full uppercase tracking-wider inline-block mb-2">
                                                {formatDate(entry.fecha)}
                                            </span>
                                            <h3 className="text-lg font-bold text-slate-900 dark:text-white capitalize">{entry.motivo}</h3>
                                            <p className="text-slate-500 text-sm font-medium mt-1">Atendido por: Dr/a. {entry.profesional}</p>
                                        </div>
                                    </div>

                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                                        {/* Diagnósticos */}
                                        <div className="bg-slate-50 dark:bg-slate-800/30 rounded-xl p-4 border border-slate-100 dark:border-slate-800/50">
                                            <h4 className="text-xs font-black uppercase text-slate-400 mb-3 flex items-center gap-2">
                                                <span className="material-symbols-outlined text-sm text-amber-500">diagnosis</span>
                                                Diagnósticos
                                            </h4>
                                            {entry.diagnosticos && entry.diagnosticos.length > 0 ? (
                                                <ul className="list-disc pl-4 space-y-1">
                                                    {entry.diagnosticos.map((diag, i) => (
                                                        <li key={i} className="text-sm font-semibold text-slate-700 dark:text-slate-300">{diag}</li>
                                                    ))}
                                                </ul>
                                            ) : (
                                                <p className="text-xs text-slate-400 italic">No registrados</p>
                                            )}
                                        </div>

                                        {/* Recetas */}
                                        <div className="bg-slate-50 dark:bg-slate-800/30 rounded-xl p-4 border border-slate-100 dark:border-slate-800/50">
                                            <h4 className="text-xs font-black uppercase text-slate-400 mb-3 flex items-center gap-2">
                                                <span className="material-symbols-outlined text-sm text-green-500">prescriptions</span>
                                                Recetas
                                            </h4>
                                            {entry.recetas && entry.recetas.length > 0 ? (
                                                <ul className="list-disc pl-4 space-y-1">
                                                    {entry.recetas.map((rec, i) => (
                                                        <li key={i} className="text-sm font-semibold text-slate-700 dark:text-slate-300">{rec}</li>
                                                    ))}
                                                </ul>
                                            ) : (
                                                <p className="text-xs text-slate-400 italic">No se prescribieron medicamentos</p>
                                            )}
                                        </div>
                                    </div>

                                    {/* Signos Vitales Mini-panel */}
                                    {entry.signosVitales && Object.keys(entry.signosVitales).length > 0 && (
                                        <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-800 flex gap-4 md:gap-8 flex-wrap">
                                            {entry.signosVitales.presionArterial && (
                                                <div>
                                                    <span className="text-[10px] text-slate-400 font-bold uppercase block">P.A.</span>
                                                    <span className="text-sm font-black text-slate-800 dark:text-slate-200">{entry.signosVitales.presionArterial}</span>
                                                </div>
                                            )}
                                            {entry.signosVitales.peso && (
                                                <div>
                                                    <span className="text-[10px] text-slate-400 font-bold uppercase block">Peso</span>
                                                    <span className="text-sm font-black text-slate-800 dark:text-slate-200">{entry.signosVitales.peso} kg</span>
                                                </div>
                                            )}
                                            {entry.signosVitales.temperatura && (
                                                <div>
                                                    <span className="text-[10px] text-slate-400 font-bold uppercase block">Temp</span>
                                                    <span className="text-sm font-black text-slate-800 dark:text-slate-200">{entry.signosVitales.temperatura} °C</span>
                                                </div>
                                            )}
                                            {entry.signosVitales.frecuenciaCardiaca && (
                                                <div>
                                                    <span className="text-[10px] text-slate-400 font-bold uppercase block">F.C.</span>
                                                    <span className="text-sm font-black text-slate-800 dark:text-slate-200">{entry.signosVitales.frecuenciaCardiaca} bpm</span>
                                                </div>
                                            )}
                                        </div>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default RecordsView;
