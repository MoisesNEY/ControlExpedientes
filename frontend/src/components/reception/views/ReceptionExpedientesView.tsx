import { useState, useEffect } from 'react';
import { ExpedienteService, type ExpedienteClinicoDTO } from '../../../services/expediente.service';
import { PacienteService, type PacienteDTO } from '../../../services/paciente.service';

interface TimelineEntry {
    fecha: string;
}

const ReceptionExpedientesView = () => {
    const [expedientes, setExpedientes] = useState<ExpedienteClinicoDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');

    // Detail modal state
    const [detailExpediente, setDetailExpediente] = useState<ExpedienteClinicoDTO | null>(null);
    const [detailPaciente, setDetailPaciente] = useState<PacienteDTO | null>(null);
    const [totalConsultas, setTotalConsultas] = useState(0);
    const [lastConsultaDate, setLastConsultaDate] = useState<string | null>(null);
    const [detailLoading, setDetailLoading] = useState(false);

    const fetchExpedientes = async () => {
        setLoading(true);
        try {
            const data = await ExpedienteService.getAll({ sort: 'fechaApertura,desc', size: 100 });
            setExpedientes(data);
        } catch (error) {
            console.error('Error fetching expedientes:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchExpedientes(); }, []);

    const filtered = expedientes.filter(e => {
        const q = search.toLowerCase();
        return !q
            || e.numeroExpediente.toLowerCase().includes(q)
            || (e.paciente?.nombres || '').toLowerCase().includes(q)
            || (e.paciente?.apellidos || '').toLowerCase().includes(q);
    });

    const openDetail = async (e: ExpedienteClinicoDTO) => {
        setDetailExpediente(e);
        setDetailPaciente(null);
        setTotalConsultas(0);
        setLastConsultaDate(null);
        setDetailLoading(true);

        try {
            const [paciente, timeline] = await Promise.all([
                e.paciente?.id ? PacienteService.getById(e.paciente.id) : Promise.resolve(null),
                e.id ? ExpedienteService.getTimeline(e.id) : Promise.resolve([]),
            ]);

            setDetailPaciente(paciente);

            const entries = timeline as TimelineEntry[];
            setTotalConsultas(entries.length);
            if (entries.length > 0 && entries[0]?.fecha) {
                setLastConsultaDate(entries[0].fecha);
            }
        } catch (error) {
            console.error('Error fetching detail:', error);
        } finally {
            setDetailLoading(false);
        }
    };

    const closeDetail = () => {
        setDetailExpediente(null);
        setDetailPaciente(null);
        setTotalConsultas(0);
        setLastConsultaDate(null);
    };

    const formatDate = (dateStr: string) => {
        try {
            const d = dateStr.includes('T') ? new Date(dateStr) : new Date(dateStr + 'T00:00:00');
            return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' });
        } catch {
            return dateStr;
        }
    };

    const sexoLabel = (sexo: string) => {
        switch (sexo) {
            case 'MASCULINO': return 'Masculino';
            case 'FEMENINO': return 'Femenino';
            case 'OTRO': return 'Otro';
            default: return sexo;
        }
    };

    return (
        <div className="p-4 md:p-8 space-y-4 md:space-y-6">
            {/* Read-only banner */}
            <div className="flex items-center gap-2 px-4 py-3 bg-amber-100 dark:bg-amber-900/30 border border-amber-200 dark:border-amber-800 text-amber-700 dark:text-amber-400 rounded-xl text-sm font-bold">
                <span className="material-symbols-outlined text-lg">visibility</span>
                Solo lectura — Vista administrativa
            </div>

            {/* Header */}
            <div>
                <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white">Expedientes Clínicos</h2>
                <p className="text-slate-500 text-xs md:text-sm font-medium">Consulta de expedientes (solo lectura).</p>
            </div>

            {/* Table card */}
            <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                {/* Search */}
                <div className="p-4 md:p-6 border-b border-slate-100 dark:border-slate-800">
                    <div className="relative">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-lg">search</span>
                        <input
                            type="text"
                            placeholder="Buscar por N° expediente o nombre del paciente..."
                            value={search}
                            onChange={e => setSearch(e.target.value)}
                            className="w-full pl-10 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl text-sm outline-none focus:ring-2 focus:ring-amber-600 text-slate-900 dark:text-white"
                        />
                    </div>
                </div>

                {/* Table */}
                <div className="overflow-x-auto scrollbar-hide">
                    <table className="w-full text-left min-w-[700px]">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest">
                            <tr>
                                <th className="px-6 py-4">N° Expediente</th>
                                <th className="px-6 py-4">Paciente</th>
                                <th className="px-6 py-4">Fecha Apertura</th>
                                <th className="px-6 py-4 text-right">Acciones</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                            {loading ? (
                                Array(5).fill(0).map((_, i) => (
                                    <tr key={i} className="animate-pulse">
                                        <td colSpan={4} className="px-6 py-6">
                                            <div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-full" />
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                filtered.map(e => (
                                    <tr key={e.id} className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors">
                                        <td className="px-6 py-4">
                                            <span className="text-sm font-bold text-amber-600">
                                                {e.numeroExpediente}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">
                                                {e.paciente?.nombres ? `${e.paciente.nombres} ${e.paciente.apellidos || ''}` : 'N/A'}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-sm text-slate-500">
                                            {formatDate(e.fechaApertura)}
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <button
                                                onClick={() => openDetail(e)}
                                                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-bold text-amber-600 hover:bg-amber-50 dark:hover:bg-amber-900/20 rounded-lg transition-colors"
                                                title="Ver Detalle"
                                            >
                                                <span className="material-symbols-outlined text-sm">visibility</span>
                                                Ver Detalle
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                            {!loading && filtered.length === 0 && (
                                <tr>
                                    <td colSpan={4} className="px-6 py-20 text-center text-slate-400 italic font-medium">
                                        No se encontraron expedientes.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Detail Modal (read-only) */}
            {detailExpediente && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={closeDetail} />
                    <div className="relative bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 w-full max-w-lg max-h-[90vh] flex flex-col">
                        {/* Header */}
                        <div className="p-6 border-b border-slate-100 dark:border-slate-800 flex-shrink-0">
                            <div className="flex justify-between items-start gap-4">
                                <div className="flex items-start gap-4 min-w-0">
                                    <div className="w-12 h-12 rounded-xl bg-amber-600/10 flex items-center justify-center flex-shrink-0">
                                        <span className="material-symbols-outlined text-amber-600 text-2xl">folder_open</span>
                                    </div>
                                    <div className="min-w-0">
                                        <h3 className="text-lg font-black text-slate-900 dark:text-white">
                                            Expediente {detailExpediente.numeroExpediente}
                                        </h3>
                                        <p className="text-sm text-slate-500 font-medium">
                                            {detailExpediente.paciente?.nombres
                                                ? `${detailExpediente.paciente.nombres} ${detailExpediente.paciente.apellidos || ''}`
                                                : 'Paciente no asignado'}
                                        </p>
                                    </div>
                                </div>
                                <button onClick={closeDetail} className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-300 flex-shrink-0">
                                    <span className="material-symbols-outlined">close</span>
                                </button>
                            </div>
                        </div>

                        {/* Content */}
                        <div className="flex-1 overflow-y-auto p-6 space-y-6">
                            {detailLoading ? (
                                <div className="space-y-4 animate-pulse">
                                    {Array(4).fill(0).map((_, i) => (
                                        <div key={i} className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-full" />
                                    ))}
                                </div>
                            ) : (
                                <>
                                    {/* Patient basic data */}
                                    <div>
                                        <h4 className="text-[10px] font-black text-slate-500 uppercase tracking-widest mb-3">
                                            Datos del Paciente
                                        </h4>
                                        {detailPaciente ? (
                                            <div className="grid grid-cols-2 gap-3">
                                                <InfoField icon="person" label="Nombre" value={`${detailPaciente.nombres} ${detailPaciente.apellidos}`} />
                                                <InfoField icon="badge" label="Código" value={detailPaciente.codigo} />
                                                <InfoField icon="wc" label="Sexo" value={sexoLabel(detailPaciente.sexo)} />
                                                <InfoField icon="cake" label="Fecha Nacimiento" value={formatDate(detailPaciente.fechaNacimiento)} />
                                                <InfoField icon="call" label="Teléfono" value={detailPaciente.telefono || '—'} />
                                                <InfoField icon="home" label="Dirección" value={detailPaciente.direccion || '—'} full />
                                            </div>
                                        ) : (
                                            <p className="text-sm text-slate-400 italic">Sin datos del paciente.</p>
                                        )}
                                    </div>

                                    {/* Expediente info */}
                                    <div>
                                        <h4 className="text-[10px] font-black text-slate-500 uppercase tracking-widest mb-3">
                                            Información del Expediente
                                        </h4>
                                        <div className="grid grid-cols-2 gap-3">
                                            <InfoField icon="tag" label="Número" value={detailExpediente.numeroExpediente} />
                                            <InfoField icon="calendar_today" label="Fecha Apertura" value={formatDate(detailExpediente.fechaApertura)} />
                                            <InfoField icon="notes" label="Observaciones" value={detailExpediente.observaciones || '—'} full />
                                        </div>
                                    </div>

                                    {/* Consultation summary */}
                                    <div>
                                        <h4 className="text-[10px] font-black text-slate-500 uppercase tracking-widest mb-3">
                                            Resumen de Consultas
                                        </h4>
                                        <div className="grid grid-cols-2 gap-3">
                                            <div className="bg-slate-50 dark:bg-slate-800/50 rounded-xl border border-slate-200 dark:border-slate-700 p-4 text-center">
                                                <span className="material-symbols-outlined text-2xl text-amber-600 mb-1">stethoscope</span>
                                                <p className="text-2xl font-black text-slate-900 dark:text-white">{totalConsultas}</p>
                                                <p className="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Total Consultas</p>
                                            </div>
                                            <div className="bg-slate-50 dark:bg-slate-800/50 rounded-xl border border-slate-200 dark:border-slate-700 p-4 text-center">
                                                <span className="material-symbols-outlined text-2xl text-amber-600 mb-1">event</span>
                                                <p className="text-sm font-black text-slate-900 dark:text-white mt-1">
                                                    {lastConsultaDate ? formatDate(lastConsultaDate) : '—'}
                                                </p>
                                                <p className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mt-1">Última Consulta</p>
                                            </div>
                                        </div>
                                    </div>
                                </>
                            )}
                        </div>

                        {/* Footer */}
                        <div className="p-4 border-t border-slate-100 dark:border-slate-800 flex justify-end flex-shrink-0">
                            <button onClick={closeDetail} className="px-6 py-2.5 text-sm font-bold text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors">
                                Cerrar
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

/* Reusable field display for the detail modal */
function InfoField({ icon, label, value, full }: { icon: string; label: string; value: string; full?: boolean }) {
    return (
        <div className={`bg-slate-50 dark:bg-slate-800/50 rounded-lg p-3 ${full ? 'col-span-2' : ''}`}>
            <div className="flex items-center gap-1.5 mb-1">
                <span className="material-symbols-outlined text-xs text-slate-400">{icon}</span>
                <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest">{label}</span>
            </div>
            <p className="text-sm font-semibold text-slate-700 dark:text-slate-300">{value}</p>
        </div>
    );
}

export default ReceptionExpedientesView;
