import { useState, useEffect, useRef, useCallback } from 'react';
import { usePatient } from '../../../context/PatientContext';
import { ExpedienteService } from '../../../services/expediente.service';
import { ReporteService } from '../../../services/reporte.service';
import { PacienteService, type PacienteDTO } from '../../../services/paciente.service';

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

const calculateAge = (birthday?: string) => {
    if (!birthday) return 'N/A';
    const b = new Date(birthday);
    if (Number.isNaN(b.getTime())) return 'N/A';
    const today = new Date();
    let years = today.getFullYear() - b.getFullYear();
    const m = today.getMonth() - b.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < b.getDate())) years--;
    return `${years} años`;
};

const RecordsView = () => {
    const { selectedPatient, selectPatient } = usePatient();
    const [timeline, setTimeline] = useState<TimelineEntry[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [downloading, setDownloading] = useState<string | null>(null);
    const [expedienteId, setExpedienteId] = useState<number | null>(null);

    // Inline patient search state
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState<PacienteDTO[]>([]);
    const [searching, setSearching] = useState(false);
    const [showSearch, setShowSearch] = useState(!selectedPatient);
    const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    useEffect(() => {
        setShowSearch(!selectedPatient);
    }, [selectedPatient]);

    const searchPatients = useCallback((query: string) => {
        if (debounceRef.current) clearTimeout(debounceRef.current);
        if (!query.trim()) {
            setSearchResults([]);
            setSearching(false);
            return;
        }
        setSearching(true);
        debounceRef.current = setTimeout(async () => {
            try {
                const results = await PacienteService.getAll({
                    'nombres.contains': query.trim(),
                    size: 10,
                    sort: 'nombres,asc',
                });
                setSearchResults(results);
            } catch {
                setSearchResults([]);
            } finally {
                setSearching(false);
            }
        }, 300);
    }, []);

    const handleSearchChange = (value: string) => {
        setSearchQuery(value);
        searchPatients(value);
    };

    const handleSelectPatient = (paciente: PacienteDTO) => {
        selectPatient({
            id: String(paciente.id),
            patientId: paciente.id,
            name: `${paciente.nombres} ${paciente.apellidos}`,
            age: calculateAge(paciente.fechaNacimiento),
            gender: paciente.sexo,
            status: paciente.activo ? 'Activo' : 'Inactivo',
        });
        setSearchQuery('');
        setSearchResults([]);
        setShowSearch(false);
    };

    const handleChangePatient = () => {
        selectPatient(null);
        setSearchQuery('');
        setSearchResults([]);
        setShowSearch(true);
    };

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
                    setExpedienteId(expediente.id);
                    const data = await ExpedienteService.getTimeline(expediente.id);
                    setTimeline(data);
                } else {
                    setExpedienteId(null);
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

    const handleDescargarHistorial = async (pacienteId: number) => {
        setDownloading('historial-' + pacienteId);
        try {
            await ReporteService.descargarHistorialPdf(pacienteId);
        } catch (error) {
            console.error('Error descargando historial:', error);
        } finally {
            setDownloading(null);
        }
    };

    const handleDescargarExpediente = async (expId: number) => {
        setDownloading('expediente-' + expId);
        try {
            await ReporteService.descargarExpedientePdf(expId);
        } catch (error) {
            console.error('Error descargando expediente:', error);
        } finally {
            setDownloading(null);
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('es-ES', {
            day: '2-digit',
            month: 'short',
            year: 'numeric'
        });
    };

    if (!selectedPatient || showSearch) {
        return (
            <div className="p-8 flex flex-col items-center justify-center h-full bg-slate-50 dark:bg-slate-950/50 rounded-2xl border border-dashed border-slate-200 dark:border-slate-800 m-8">
                <span className="material-symbols-outlined text-6xl mb-4 opacity-50 text-slate-400">quick_reference_all</span>
                <p className="font-semibold text-lg text-slate-600 dark:text-slate-300 mb-1">Buscar paciente</p>
                <p className="text-sm text-slate-400 mb-6 text-center max-w-sm">Escriba el nombre del paciente para buscar y ver su Historial Clínico.</p>

                <div className="w-full max-w-md relative">
                    <div className="relative">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">search</span>
                        <input
                            type="text"
                            placeholder="Nombre del paciente..."
                            value={searchQuery}
                            onChange={(e) => handleSearchChange(e.target.value)}
                            className="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all text-sm font-medium"
                            autoFocus
                        />
                        {searching && (
                            <span className="absolute right-3 top-1/2 -translate-y-1/2">
                                <span className="animate-spin inline-block w-4 h-4 border-2 border-primary border-t-transparent rounded-full"></span>
                            </span>
                        )}
                    </div>

                    {searchResults.length > 0 && (
                        <ul className="absolute z-20 mt-2 w-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl shadow-xl max-h-64 overflow-y-auto">
                            {searchResults.map((paciente) => (
                                <li key={paciente.id}>
                                    <button
                                        type="button"
                                        onClick={() => handleSelectPatient(paciente)}
                                        className="w-full text-left px-4 py-3 hover:bg-primary/10 dark:hover:bg-primary/20 transition-colors flex items-center gap-3 border-b border-slate-100 dark:border-slate-800 last:border-b-0"
                                    >
                                        <span className="bg-primary/10 text-primary rounded-full w-9 h-9 flex items-center justify-center font-bold text-sm shrink-0">
                                            {paciente.nombres?.charAt(0)}{paciente.apellidos?.charAt(0)}
                                        </span>
                                        <div className="min-w-0">
                                            <p className="text-sm font-semibold text-slate-900 dark:text-white truncate">
                                                {paciente.nombres} {paciente.apellidos}
                                            </p>
                                            <p className="text-xs text-slate-400 truncate">
                                                {paciente.cedula ? `Cédula: ${paciente.cedula}` : paciente.codigo} · {calculateAge(paciente.fechaNacimiento)}
                                            </p>
                                        </div>
                                        <span className={`ml-auto text-[10px] font-bold uppercase px-2 py-0.5 rounded-full shrink-0 ${paciente.activo ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'}`}>
                                            {paciente.activo ? 'Activo' : 'Inactivo'}
                                        </span>
                                    </button>
                                </li>
                            ))}
                        </ul>
                    )}

                    {searchQuery.trim() && !searching && searchResults.length === 0 && (
                        <div className="absolute z-20 mt-2 w-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl shadow-xl p-4 text-center">
                            <p className="text-sm text-slate-400 italic">No se encontraron pacientes</p>
                        </div>
                    )}
                </div>
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
                <div className="flex gap-2 flex-wrap">
                    <button
                        onClick={handleChangePatient}
                        className="bg-slate-200 hover:bg-slate-300 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 px-4 py-2 rounded-lg flex items-center gap-2 transition-colors text-sm font-semibold"
                    >
                        <span className="material-symbols-outlined text-base">swap_horiz</span>
                        Cambiar paciente
                    </button>
                    <button
                        onClick={() => {
                            const pacienteId = selectedPatient.patientId ??
                                (selectedPatient.id ? parseInt(selectedPatient.id, 10) : NaN);
                            if (pacienteId && !Number.isNaN(pacienteId)) handleDescargarHistorial(pacienteId);
                        }}
                        disabled={!!downloading}
                        className="bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-lg flex items-center gap-2 transition-colors disabled:opacity-50 text-sm font-semibold"
                    >
                        {downloading?.startsWith('historial') ? (
                            <span className="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full"></span>
                        ) : (
                            <span>📄</span>
                        )}
                        Descargar Historial Clínico (PDF)
                    </button>
                    {expedienteId && (
                        <button
                            onClick={() => handleDescargarExpediente(expedienteId)}
                            disabled={!!downloading}
                            className="bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-lg flex items-center gap-2 transition-colors disabled:opacity-50 text-sm font-semibold"
                        >
                            {downloading?.startsWith('expediente') ? (
                                <span className="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full"></span>
                            ) : (
                                <span>📋</span>
                            )}
                            Descargar Expediente (PDF)
                        </button>
                    )}
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
