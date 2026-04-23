import { useEffect, useMemo, useRef, useState } from 'react';
import { PacienteService, type PacienteDTO } from '../../../services/paciente.service';
import { ExpedienteService, type ExpedienteClinicoDTO } from '../../../services/expediente.service';
import { LaboratorioService, type ResultadoLaboratorioDTO } from '../../../services/laboratorio.service';
import { ReporteService } from '../../../services/reporte.service';

interface TimelineEntry {
    consultaId?: number;
    fecha: string;
    profesional: string;
    motivo: string;
    diagnosticos: string[];
    recetas: string[];
}

const today = new Date().toISOString().slice(0, 10);
const lastMonth = new Date(Date.now() - 1000 * 60 * 60 * 24 * 30).toISOString().slice(0, 10);

const formatDate = (value: string) =>
    new Date(value).toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' });

const ReportsCenterView = () => {
    const [patientQuery, setPatientQuery] = useState('');
    const [patientResults, setPatientResults] = useState<PacienteDTO[]>([]);
    const [selectedPatient, setSelectedPatient] = useState<PacienteDTO | null>(null);
    const [expediente, setExpediente] = useState<ExpedienteClinicoDTO | null>(null);
    const [timeline, setTimeline] = useState<TimelineEntry[]>([]);
    const [labResults, setLabResults] = useState<ResultadoLaboratorioDTO[]>([]);
    const [fechaInicio, setFechaInicio] = useState(lastMonth);
    const [fechaFin, setFechaFin] = useState(today);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [downloading, setDownloading] = useState<string | null>(null);
    const dropdownRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleOutsideClick = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setPatientResults([]);
            }
        };
        document.addEventListener('mousedown', handleOutsideClick);
        return () => document.removeEventListener('mousedown', handleOutsideClick);
    }, []);

    useEffect(() => {
        if (patientQuery.trim().length < 2 || (selectedPatient && `${selectedPatient.nombres} ${selectedPatient.apellidos}`.trim() === patientQuery.trim())) {
            return;
        }

        const timeout = window.setTimeout(async () => {
            try {
                const query = patientQuery.trim();
                const [byNames, byLastNames] = await Promise.all([
                    PacienteService.getAll({ 'nombres.contains': query, size: 8, sort: 'nombres,asc' }),
                    PacienteService.getAll({ 'apellidos.contains': query, size: 8, sort: 'apellidos,asc' }),
                ]);
                const seen = new Set<number>();
                setPatientResults(
                    [...byNames, ...byLastNames].filter(patient => {
                        if (!patient.id || seen.has(patient.id)) return false;
                        seen.add(patient.id);
                        return true;
                    }),
                );
            } catch {
                setPatientResults([]);
            }
        }, 300);

        return () => window.clearTimeout(timeout);
    }, [patientQuery, selectedPatient]);

    useEffect(() => {
        const loadReportContext = async () => {
            if (!selectedPatient?.id) {
                setExpediente(null);
                setTimeline([]);
                setLabResults([]);
                return;
            }

            setLoading(true);
            setError('');
            try {
                const expedienteData = await ExpedienteService.getByPacienteId(selectedPatient.id);
                setExpediente(expedienteData);

                const [timelineData, laboratorioData] = await Promise.all([
                    expedienteData.id ? ExpedienteService.getTimeline(expedienteData.id) : Promise.resolve([]),
                    LaboratorioService.getAll({
                        'pacienteId.equals': selectedPatient.id,
                        'fechaExamen.greaterThanOrEqual': fechaInicio,
                        'fechaExamen.lessThanOrEqual': fechaFin,
                        sort: 'fechaExamen,desc',
                        size: 50,
                    }),
                ]);

                setTimeline(Array.isArray(timelineData) ? timelineData : []);
                setLabResults(laboratorioData);
            } catch {
                setError('No se pudieron cargar los datos del centro de reportes.');
                setExpediente(null);
                setTimeline([]);
                setLabResults([]);
            } finally {
                setLoading(false);
            }
        };

        void loadReportContext();
    }, [fechaFin, fechaInicio, selectedPatient]);

    const filteredTimeline = useMemo(() => {
        return timeline.filter(entry => {
            const entryDate = entry.fecha.slice(0, 10);
            return entryDate >= fechaInicio && entryDate <= fechaFin;
        });
    }, [fechaFin, fechaInicio, timeline]);

    const summary = useMemo(() => ({
        consultas: filteredTimeline.length,
        diagnosticos: filteredTimeline.reduce((total, entry) => total + entry.diagnosticos.length, 0),
        recetas: filteredTimeline.reduce((total, entry) => total + entry.recetas.length, 0),
        laboratorios: labResults.length,
    }), [filteredTimeline, labResults.length]);

    const handleDownload = async (key: string, action: () => Promise<void>) => {
        setDownloading(key);
        setError('');
        try {
            await action();
        } catch {
            setError('No se pudo generar el archivo solicitado.');
        } finally {
            setDownloading(null);
        }
    };

    const resetPatient = () => {
        setSelectedPatient(null);
        setPatientQuery('');
        setPatientResults([]);
        setExpediente(null);
        setTimeline([]);
        setLabResults([]);
    };

    return (
        <div className="p-4 md:p-8 space-y-6">
            <div className="flex flex-col gap-2">
                <h2 className="text-2xl md:text-3xl font-black text-slate-900 dark:text-white">Centro de reportes clínicos</h2>
                <p className="text-sm text-slate-500">
                    Centraliza descargas en PDF/Excel, consolida consultas, recetas y laboratorio, y filtra por fechas desde un solo lugar.
                </p>
            </div>

            <div className="grid gap-4 xl:grid-cols-[1.1fr_0.9fr]">
                <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm space-y-4">
                    <div ref={dropdownRef} className="space-y-2">
                        <label className="text-[10px] font-black uppercase tracking-widest text-slate-500">Paciente objetivo</label>
                        <div className="relative">
                            <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">person_search</span>
                            <input
                                value={patientQuery}
                                onChange={event => {
                                    setPatientQuery(event.target.value);
                                    if (selectedPatient && event.target.value !== `${selectedPatient.nombres} ${selectedPatient.apellidos}`.trim()) {
                                        setSelectedPatient(null);
                                    }
                                }}
                                placeholder="Buscar por nombre o apellido"
                                className="w-full rounded-2xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-950 px-12 py-3 text-sm outline-none focus:ring-2 focus:ring-primary"
                            />
                            {patientResults.length > 0 && (
                                <div className="absolute z-20 mt-2 w-full rounded-2xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 shadow-xl overflow-hidden">
                                    {patientResults.map(patient => (
                                        <button
                                            key={patient.id}
                                            type="button"
                                            onClick={() => {
                                                setSelectedPatient(patient);
                                                setPatientQuery(`${patient.nombres} ${patient.apellidos}`.trim());
                                                setPatientResults([]);
                                            }}
                                            className="flex w-full items-center justify-between gap-3 border-b border-slate-100 px-4 py-3 text-left text-sm hover:bg-slate-50 dark:border-slate-800 dark:hover:bg-slate-800/50 last:border-b-0"
                                        >
                                            <span className="font-bold text-slate-900 dark:text-white">{patient.nombres} {patient.apellidos}</span>
                                            <span className="text-xs text-slate-400">{patient.cedula ?? patient.codigo}</span>
                                        </button>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>

                    <div className="grid gap-4 md:grid-cols-2">
                        <div>
                            <label className="text-[10px] font-black uppercase tracking-widest text-slate-500">Fecha inicial</label>
                            <input
                                type="date"
                                value={fechaInicio}
                                max={fechaFin}
                                onChange={event => setFechaInicio(event.target.value)}
                                className="mt-2 w-full rounded-2xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-950 px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-primary"
                            />
                        </div>
                        <div>
                            <label className="text-[10px] font-black uppercase tracking-widest text-slate-500">Fecha final</label>
                            <input
                                type="date"
                                value={fechaFin}
                                min={fechaInicio}
                                onChange={event => setFechaFin(event.target.value)}
                                className="mt-2 w-full rounded-2xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-950 px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-primary"
                            />
                        </div>
                    </div>

                    <div className="flex flex-wrap gap-3">
                        <button
                            type="button"
                            onClick={resetPatient}
                            className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-bold text-slate-600 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-200 dark:hover:bg-slate-800"
                        >
                            Limpiar selección
                        </button>
                        <div className="rounded-xl bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-600 dark:bg-slate-800 dark:text-slate-200">
                            {selectedPatient ? `Paciente activo: ${selectedPatient.nombres} ${selectedPatient.apellidos}` : 'Seleccione un paciente para habilitar reportes'}
                        </div>
                    </div>
                </div>

                <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm">
                    <h3 className="text-lg font-black text-slate-900 dark:text-white">Resumen del rango</h3>
                    <div className="mt-4 grid grid-cols-2 gap-3">
                        {[
                            ['Consultas', summary.consultas, 'clinical_notes'],
                            ['Diagnósticos', summary.diagnosticos, 'diagnosis'],
                            ['Recetas', summary.recetas, 'prescriptions'],
                            ['Laboratorios', summary.laboratorios, 'science'],
                        ].map(([label, value, icon]) => (
                            <div key={label} className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 p-4">
                                <span className="material-symbols-outlined text-primary">{icon}</span>
                                <p className="mt-2 text-2xl font-black text-slate-900 dark:text-white">{value}</p>
                                <p className="text-xs font-bold uppercase tracking-widest text-slate-500">{label}</p>
                            </div>
                        ))}
                    </div>
                    {expediente && (
                        <p className="mt-4 rounded-2xl bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-300">
                            Expediente vinculado: <span className="font-black">{expediente.numeroExpediente}</span>
                        </p>
                    )}
                </div>
            </div>

            {error && <div className="rounded-2xl bg-red-100 px-4 py-3 text-sm font-semibold text-red-700">{error}</div>}

            <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm space-y-4">
                <div>
                    <h3 className="text-lg font-black text-slate-900 dark:text-white">Exportaciones centralizadas</h3>
                    <p className="text-sm text-slate-500">PDF y Excel para historial, expediente y resumen filtrado de consultas.</p>
                </div>
                <div className="flex flex-wrap gap-3">
                    <ActionButton
                        label="Historial PDF"
                        loading={downloading === 'historial-pdf'}
                        disabled={!selectedPatient?.id}
                        onClick={() => selectedPatient?.id && handleDownload('historial-pdf', () => ReporteService.descargarHistorialPdf(selectedPatient.id!))}
                    />
                    <ActionButton
                        label="Historial Excel"
                        loading={downloading === 'historial-xlsx'}
                        disabled={!selectedPatient?.id}
                        onClick={() => selectedPatient?.id && handleDownload('historial-xlsx', () => ReporteService.descargarHistorialExcel(selectedPatient.id!))}
                    />
                    <ActionButton
                        label="Expediente PDF"
                        loading={downloading === 'expediente-pdf'}
                        disabled={!expediente?.id}
                        onClick={() => expediente?.id && handleDownload('expediente-pdf', () => ReporteService.descargarExpedientePdf(expediente.id!))}
                    />
                    <ActionButton
                        label="Expediente Excel"
                        loading={downloading === 'expediente-xlsx'}
                        disabled={!expediente?.id}
                        onClick={() => expediente?.id && handleDownload('expediente-xlsx', () => ReporteService.descargarExpedienteExcel(expediente.id!))}
                    />
                    <ActionButton
                        label="Consultas PDF"
                        loading={downloading === 'consultas-pdf'}
                        disabled={!selectedPatient?.id}
                        onClick={() => selectedPatient?.id && handleDownload('consultas-pdf', () => ReporteService.descargarResumenConsultasPdf({
                            fechaInicio,
                            fechaFin,
                            pacienteId: selectedPatient.id!,
                        }))}
                    />
                    <ActionButton
                        label="Consultas Excel"
                        loading={downloading === 'consultas-xlsx'}
                        disabled={!selectedPatient?.id}
                        onClick={() => selectedPatient?.id && handleDownload('consultas-xlsx', () => ReporteService.descargarResumenConsultasExcel({
                            fechaInicio,
                            fechaFin,
                            pacienteId: selectedPatient.id!,
                        }))}
                    />
                </div>
            </div>

            <div className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
                <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm">
                    <div className="mb-4 flex items-center justify-between">
                        <div>
                            <h3 className="text-lg font-black text-slate-900 dark:text-white">Consultas filtradas</h3>
                            <p className="text-sm text-slate-500">Visualiza el periodo seleccionado y descarga recetas individuales.</p>
                        </div>
                        {loading && <span className="text-sm text-slate-400">Cargando…</span>}
                    </div>
                    <div className="space-y-4">
                        {filteredTimeline.length === 0 ? (
                            <EmptyState message="No hay consultas en el rango seleccionado." />
                        ) : (
                            filteredTimeline.map(entry => (
                                <div key={`${entry.consultaId ?? entry.fecha}-${entry.motivo}`} className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 p-4">
                                    <div className="flex flex-wrap items-start justify-between gap-3">
                                        <div>
                                            <p className="text-xs font-black uppercase tracking-widest text-primary">{formatDate(entry.fecha)}</p>
                                            <h4 className="text-base font-black text-slate-900 dark:text-white">{entry.motivo}</h4>
                                            <p className="text-sm text-slate-500">Profesional: {entry.profesional || 'Sin asignar'}</p>
                                        </div>
                                        {entry.consultaId && entry.recetas.length > 0 && (
                                            <button
                                                type="button"
                                                onClick={() => handleDownload(`receta-${entry.consultaId}`, () => ReporteService.descargarRecetaPdfPorConsulta(entry.consultaId!))}
                                                className="rounded-xl border border-slate-200 px-3 py-2 text-xs font-bold text-slate-700 hover:bg-white dark:border-slate-700 dark:text-slate-200 dark:hover:bg-slate-900"
                                            >
                                                {downloading === `receta-${entry.consultaId}` ? 'Generando…' : 'Receta PDF'}
                                            </button>
                                        )}
                                    </div>
                                    <div className="mt-3 grid gap-3 md:grid-cols-2">
                                        <InfoBlock title="Diagnósticos" values={entry.diagnosticos} />
                                        <InfoBlock title="Recetas" values={entry.recetas} />
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>

                <div className="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm">
                    <h3 className="text-lg font-black text-slate-900 dark:text-white">Resultados de laboratorio</h3>
                    <p className="mb-4 text-sm text-slate-500">Otra capa del módulo para revisar resultados por rango antes de exportar otros reportes.</p>
                    <div className="space-y-3">
                        {labResults.length === 0 ? (
                            <EmptyState message="No hay resultados de laboratorio para el rango indicado." />
                        ) : (
                            labResults.map(result => (
                                <div key={result.id ?? `${result.tipoExamen}-${result.fechaExamen}`} className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 p-4">
                                    <div className="flex items-center justify-between gap-3">
                                        <div>
                                            <p className="text-sm font-black text-slate-900 dark:text-white">{result.tipoExamen}</p>
                                            <p className="text-xs text-slate-500">{formatDate(result.fechaExamen)}</p>
                                        </div>
                                        <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-black text-primary">{result.resultado}</span>
                                    </div>
                                    <p className="mt-2 text-xs text-slate-500">
                                        Referencia: {result.valorReferencia || 'N/D'} · Unidad: {result.unidad || 'N/D'}
                                    </p>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

const ActionButton = ({
    label,
    disabled,
    loading,
    onClick,
}: {
    label: string;
    disabled?: boolean;
    loading?: boolean;
    onClick: () => void;
}) => (
    <button
        type="button"
        disabled={disabled || loading}
        onClick={onClick}
        className="rounded-xl bg-primary px-4 py-2.5 text-sm font-black text-white shadow-lg shadow-primary/20 transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
    >
        {loading ? 'Generando…' : label}
    </button>
);

const InfoBlock = ({ title, values }: { title: string; values: string[] }) => (
    <div>
        <p className="text-[10px] font-black uppercase tracking-widest text-slate-500">{title}</p>
        <div className="mt-2 flex flex-wrap gap-2">
            {values.length === 0 ? (
                <span className="text-xs text-slate-400">Sin datos</span>
            ) : (
                values.map(value => (
                    <span key={`${title}-${value}`} className="rounded-full border border-slate-200 px-2.5 py-1 text-xs font-medium text-slate-600 dark:border-slate-700 dark:text-slate-200">
                        {value}
                    </span>
                ))
            )}
        </div>
    </div>
);

const EmptyState = ({ message }: { message: string }) => (
    <div className="rounded-2xl border border-dashed border-slate-200 p-6 text-center text-sm font-medium text-slate-400 dark:border-slate-700">
        {message}
    </div>
);

export default ReportsCenterView;
