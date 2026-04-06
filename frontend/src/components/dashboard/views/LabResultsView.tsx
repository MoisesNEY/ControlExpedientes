import { useState, useEffect, useCallback, useRef } from 'react';
import { PacienteService, type PacienteDTO } from '../../../services/paciente.service';
import { LaboratorioService, type ResultadoLaboratorioDTO } from '../../../services/laboratorio.service';

const TIPOS_EXAMEN = [
    'Hemograma Completo',
    'Química Sanguínea',
    'Examen de Orina',
    'Perfil Lipídico',
    'Glucosa en Ayunas',
    'Pruebas Hepáticas',
    'Creatinina',
    'Ácido Úrico',
    'TSH',
    'Otro',
];

const emptyForm: ResultadoLaboratorioDTO = {
    tipoExamen: '',
    resultado: '',
    valorReferencia: '',
    unidad: '',
    observaciones: '',
    fechaExamen: new Date().toISOString().slice(0, 10),
};

/** Simple heuristic: flag a result as abnormal when the numeric value falls outside the reference range (e.g. "70-100"). */
const isAbnormal = (resultado: string, valorReferencia?: string): boolean => {
    if (!valorReferencia) return false;

    const num = parseFloat(resultado);
    if (Number.isNaN(num)) {
        const lower = resultado.trim().toLowerCase();
        return ['positivo', 'anormal', 'alto', 'elevado', 'bajo', 'crítico'].some(k => lower.includes(k));
    }

    const rangeMatch = valorReferencia.match(/(\d+(?:\.\d+)?)\s*[-–]\s*(\d+(?:\.\d+)?)/);
    if (rangeMatch) {
        const lo = parseFloat(rangeMatch[1]);
        const hi = parseFloat(rangeMatch[2]);
        return num < lo || num > hi;
    }

    return false;
};

const LabResultsView = () => {
    // Patient search
    const [patientQuery, setPatientQuery] = useState('');
    const [patients, setPatients] = useState<PacienteDTO[]>([]);
    const [selectedPatient, setSelectedPatient] = useState<PacienteDTO | null>(null);
    const [showDropdown, setShowDropdown] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    // Lab results
    const [results, setResults] = useState<ResultadoLaboratorioDTO[]>([]);
    const [loading, setLoading] = useState(false);

    // Modal
    const [modalOpen, setModalOpen] = useState(false);
    const [form, setForm] = useState<ResultadoLaboratorioDTO>({ ...emptyForm });
    const [editingId, setEditingId] = useState<number | null>(null);
    const [saving, setSaving] = useState(false);

    // Delete confirmation
    const [deleteId, setDeleteId] = useState<number | null>(null);

    // Feedback
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Close dropdown when clicking outside
    useEffect(() => {
        const handler = (e: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
                setShowDropdown(false);
            }
        };
        document.addEventListener('mousedown', handler);
        return () => document.removeEventListener('mousedown', handler);
    }, []);

    // Search patients with debounce
    useEffect(() => {
        if (patientQuery.length < 2) {
            setPatients([]);
            return;
        }
        const timeout = setTimeout(async () => {
            try {
                const data = await PacienteService.getAll({
                    'nombres.contains': patientQuery,
                    size: 10,
                });
                setPatients(data);
                setShowDropdown(true);
            } catch {
                setPatients([]);
            }
        }, 300);
        return () => clearTimeout(timeout);
    }, [patientQuery]);

    // Fetch lab results for selected patient
    const fetchResults = useCallback(async () => {
        if (!selectedPatient?.id) return;
        setLoading(true);
        setError('');
        try {
            const data = await LaboratorioService.getByPaciente(selectedPatient.id);
            setResults(data.sort((a, b) => new Date(b.fechaExamen).getTime() - new Date(a.fechaExamen).getTime()));
        } catch {
            setError('No se pudieron cargar los resultados de laboratorio.');
            setResults([]);
        } finally {
            setLoading(false);
        }
    }, [selectedPatient]);

    useEffect(() => {
        fetchResults();
    }, [fetchResults]);

    // Helpers
    const clearFeedback = () => { setError(''); setSuccess(''); };

    const openAdd = () => {
        clearFeedback();
        setForm({ ...emptyForm });
        setEditingId(null);
        setModalOpen(true);
    };

    const openEdit = (r: ResultadoLaboratorioDTO) => {
        clearFeedback();
        setForm({ ...r, fechaExamen: r.fechaExamen?.slice(0, 10) ?? '' });
        setEditingId(r.id ?? null);
        setModalOpen(true);
    };

    const closeModal = () => { setModalOpen(false); setEditingId(null); };

    const handleChange = (field: keyof ResultadoLaboratorioDTO, value: string) => {
        setForm(prev => ({ ...prev, [field]: value }));
    };

    const handleSave = async () => {
        if (!form.tipoExamen || !form.resultado || !form.fechaExamen) {
            setError('Complete los campos obligatorios: Tipo de Examen, Resultado y Fecha.');
            return;
        }
        setSaving(true);
        clearFeedback();
        try {
            const payload: ResultadoLaboratorioDTO = {
                ...form,
                paciente: { id: selectedPatient!.id! },
            };
            if (editingId) {
                await LaboratorioService.update(editingId, payload);
                setSuccess('Resultado actualizado correctamente.');
            } else {
                await LaboratorioService.create(payload);
                setSuccess('Resultado creado correctamente.');
            }
            closeModal();
            await fetchResults();
        } catch {
            setError('Error al guardar el resultado.');
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async () => {
        if (deleteId == null) return;
        clearFeedback();
        try {
            await LaboratorioService.delete(deleteId);
            setSuccess('Resultado eliminado correctamente.');
            setDeleteId(null);
            await fetchResults();
        } catch {
            setError('Error al eliminar el resultado.');
        }
    };

    const formatDate = (dateString: string) =>
        new Date(dateString).toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' });

    const selectPatient = (p: PacienteDTO) => {
        setSelectedPatient(p);
        setPatientQuery(`${p.nombres} ${p.apellidos}`);
        setShowDropdown(false);
    };

    // ─── Render ────────────────────────────────────────────────────────────────

    return (
        <div className="p-4 md:p-8 space-y-4 md:space-y-6">
            {/* Header */}
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white">Resultados de Laboratorio</h2>
                    <p className="text-slate-500 text-xs md:text-sm font-medium">Gestión de exámenes y resultados clínicos por paciente.</p>
                </div>
            </div>

            {/* Patient search */}
            <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm p-6">
                <label className="text-xs font-black uppercase text-slate-400 tracking-widest mb-2 block">Buscar Paciente</label>
                <div className="relative" ref={dropdownRef}>
                    <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400">person_search</span>
                    <input
                        type="text"
                        placeholder="Escriba el nombre del paciente..."
                        value={patientQuery}
                        onChange={(e) => { setPatientQuery(e.target.value); setSelectedPatient(null); }}
                        className="w-full md:w-96 pl-12 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-xs outline-none focus:ring-2 focus:ring-primary transition-all"
                    />

                    {showDropdown && patients.length > 0 && (
                        <ul className="absolute z-20 top-full mt-1 w-full md:w-96 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl shadow-lg max-h-60 overflow-y-auto">
                            {patients.map((p) => (
                                <li
                                    key={p.id}
                                    onClick={() => selectPatient(p)}
                                    className="px-4 py-3 hover:bg-slate-50 dark:hover:bg-slate-700 cursor-pointer flex items-center gap-3 transition-colors"
                                >
                                    <span className="material-symbols-outlined text-primary text-base">person</span>
                                    <div>
                                        <span className="text-sm font-bold text-slate-800 dark:text-white">{p.nombres} {p.apellidos}</span>
                                        <span className="block text-[10px] text-slate-400">{p.cedula ?? p.codigo}</span>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                {selectedPatient && (
                    <div className="mt-3 flex items-center gap-2 text-xs">
                        <span className="material-symbols-outlined text-success text-base">check_circle</span>
                        <span className="font-semibold text-slate-700 dark:text-slate-300">
                            Paciente seleccionado: <span className="text-primary font-black">{selectedPatient.nombres} {selectedPatient.apellidos}</span>
                        </span>
                    </div>
                )}
            </div>

            {/* Feedback messages */}
            {error && (
                <div className="p-4 bg-red-100 text-red-700 rounded-xl text-sm font-semibold flex items-center gap-2">
                    <span className="material-symbols-outlined text-base">error</span>{error}
                </div>
            )}
            {success && (
                <div className="p-4 bg-green-100 text-green-700 rounded-xl text-sm font-semibold flex items-center gap-2">
                    <span className="material-symbols-outlined text-base">check_circle</span>{success}
                </div>
            )}

            {/* Results table */}
            {selectedPatient && (
                <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                    {/* Toolbar */}
                    <div className="p-6 border-b border-slate-100 dark:border-slate-800 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                        <span className="text-sm font-bold text-slate-700 dark:text-slate-300">
                            {results.length} resultado{results.length !== 1 ? 's' : ''} encontrado{results.length !== 1 ? 's' : ''}
                        </span>
                        <button
                            onClick={openAdd}
                            className="flex items-center gap-2 bg-primary hover:bg-primary/90 text-white text-xs font-black uppercase tracking-wider px-5 py-2.5 rounded-2xl transition-colors"
                        >
                            <span className="material-symbols-outlined text-base">add_circle</span>
                            Agregar Resultado
                        </button>
                    </div>

                    <div className="overflow-x-auto scrollbar-hide">
                        <table className="w-full text-left min-w-[900px]">
                            <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest">
                                <tr>
                                    <th className="px-6 py-4">Tipo de Examen</th>
                                    <th className="px-6 py-4">Resultado</th>
                                    <th className="px-6 py-4">Valor Referencia</th>
                                    <th className="px-6 py-4">Unidad</th>
                                    <th className="px-6 py-4">Fecha</th>
                                    <th className="px-6 py-4">Observaciones</th>
                                    <th className="px-6 py-4 text-center">Acciones</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                                {loading ? (
                                    Array(4).fill(0).map((_, i) => (
                                        <tr key={i} className="animate-pulse">
                                            <td colSpan={7} className="px-6 py-6">
                                                <div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-full"></div>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    results.map((r) => {
                                        const abnormal = isAbnormal(r.resultado, r.valorReferencia);
                                        return (
                                            <tr key={r.id} className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors">
                                                <td className="px-6 py-4">
                                                    <span className="font-bold text-slate-800 dark:text-white">{r.tipoExamen}</span>
                                                </td>
                                                <td className="px-6 py-4">
                                                    <span className={`text-sm font-black ${abnormal ? 'text-red-600' : 'text-slate-700 dark:text-slate-300'}`}>
                                                        {r.resultado}
                                                    </span>
                                                    {abnormal && (
                                                        <span className="ml-2 inline-flex items-center gap-1 px-2 py-0.5 rounded-lg bg-red-100 text-red-600 text-[10px] font-black uppercase">
                                                            <span className="material-symbols-outlined text-xs">warning</span>
                                                            Fuera de rango
                                                        </span>
                                                    )}
                                                </td>
                                                <td className="px-6 py-4 text-xs text-slate-500">{r.valorReferencia || '—'}</td>
                                                <td className="px-6 py-4 text-xs text-slate-500">{r.unidad || '—'}</td>
                                                <td className="px-6 py-4">
                                                    <span className="bg-primary/10 text-primary font-black text-[10px] px-2 py-1 rounded-full">
                                                        {formatDate(r.fechaExamen)}
                                                    </span>
                                                </td>
                                                <td className="px-6 py-4 text-xs text-slate-500 max-w-xs truncate">{r.observaciones || '—'}</td>
                                                <td className="px-6 py-4">
                                                    <div className="flex items-center justify-center gap-2">
                                                        <button
                                                            onClick={() => openEdit(r)}
                                                            title="Editar"
                                                            className="p-2 rounded-xl hover:bg-primary/10 text-primary transition-colors"
                                                        >
                                                            <span className="material-symbols-outlined text-base">edit</span>
                                                        </button>
                                                        <button
                                                            onClick={() => setDeleteId(r.id!)}
                                                            title="Eliminar"
                                                            className="p-2 rounded-xl hover:bg-red-100 text-red-500 transition-colors"
                                                        >
                                                            <span className="material-symbols-outlined text-base">delete</span>
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        );
                                    })
                                )}
                                {!loading && results.length === 0 && (
                                    <tr>
                                        <td colSpan={7} className="px-6 py-20 text-center text-slate-400 italic font-medium">
                                            <span className="material-symbols-outlined text-4xl mb-2 opacity-50 block">science</span>
                                            No se encontraron resultados de laboratorio para este paciente.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* No patient selected placeholder */}
            {!selectedPatient && (
                <div className="flex flex-col items-center justify-center py-20 text-slate-400 bg-slate-50 dark:bg-slate-950/50 rounded-2xl border border-dashed border-slate-200 dark:border-slate-800">
                    <span className="material-symbols-outlined text-6xl mb-4 opacity-50">biotech</span>
                    <p className="font-semibold text-lg text-slate-600 dark:text-slate-300">Seleccione un paciente</p>
                    <p className="text-sm mt-2 text-center max-w-sm">Busque y seleccione un paciente para ver y gestionar sus resultados de laboratorio.</p>
                </div>
            )}

            {/* ──── Add / Edit Modal ──── */}
            {modalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
                    <div className="bg-white dark:bg-slate-900 rounded-3xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto border border-slate-200 dark:border-slate-800">
                        <div className="p-6 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center">
                            <h3 className="text-lg font-black text-slate-900 dark:text-white">
                                {editingId ? 'Editar Resultado' : 'Nuevo Resultado de Laboratorio'}
                            </h3>
                            <button onClick={closeModal} className="p-2 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors">
                                <span className="material-symbols-outlined text-slate-400">close</span>
                            </button>
                        </div>

                        <div className="p-6 space-y-4">
                            {/* Tipo de Examen */}
                            <div>
                                <label className="text-xs font-black uppercase text-slate-400 tracking-widest mb-1 block">Tipo de Examen *</label>
                                <select
                                    value={form.tipoExamen}
                                    onChange={(e) => handleChange('tipoExamen', e.target.value)}
                                    className="w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-xs outline-none focus:ring-2 focus:ring-primary transition-all"
                                >
                                    <option value="">Seleccione...</option>
                                    {TIPOS_EXAMEN.map((t) => (
                                        <option key={t} value={t}>{t}</option>
                                    ))}
                                </select>
                            </div>

                            {/* Resultado */}
                            <div>
                                <label className="text-xs font-black uppercase text-slate-400 tracking-widest mb-1 block">Resultado *</label>
                                <input
                                    type="text"
                                    value={form.resultado}
                                    onChange={(e) => handleChange('resultado', e.target.value)}
                                    placeholder="Ej: 14.5"
                                    className="w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-xs outline-none focus:ring-2 focus:ring-primary transition-all"
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                {/* Valor Referencia */}
                                <div>
                                    <label className="text-xs font-black uppercase text-slate-400 tracking-widest mb-1 block">Valor Referencia</label>
                                    <input
                                        type="text"
                                        value={form.valorReferencia}
                                        onChange={(e) => handleChange('valorReferencia', e.target.value)}
                                        placeholder="Ej: 12.0 - 16.0"
                                        className="w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-xs outline-none focus:ring-2 focus:ring-primary transition-all"
                                    />
                                </div>

                                {/* Unidad */}
                                <div>
                                    <label className="text-xs font-black uppercase text-slate-400 tracking-widest mb-1 block">Unidad</label>
                                    <input
                                        type="text"
                                        value={form.unidad}
                                        onChange={(e) => handleChange('unidad', e.target.value)}
                                        placeholder="Ej: g/dL"
                                        className="w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-xs outline-none focus:ring-2 focus:ring-primary transition-all"
                                    />
                                </div>
                            </div>

                            {/* Fecha */}
                            <div>
                                <label className="text-xs font-black uppercase text-slate-400 tracking-widest mb-1 block">Fecha del Examen *</label>
                                <input
                                    type="date"
                                    value={form.fechaExamen}
                                    onChange={(e) => handleChange('fechaExamen', e.target.value)}
                                    className="w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-xs outline-none focus:ring-2 focus:ring-primary transition-all"
                                />
                            </div>

                            {/* Observaciones */}
                            <div>
                                <label className="text-xs font-black uppercase text-slate-400 tracking-widest mb-1 block">Observaciones</label>
                                <textarea
                                    value={form.observaciones}
                                    onChange={(e) => handleChange('observaciones', e.target.value)}
                                    rows={3}
                                    placeholder="Notas adicionales..."
                                    className="w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-xs outline-none focus:ring-2 focus:ring-primary transition-all resize-none"
                                />
                            </div>
                        </div>

                        <div className="p-6 border-t border-slate-100 dark:border-slate-800 flex justify-end gap-3">
                            <button
                                onClick={closeModal}
                                className="px-5 py-2.5 rounded-2xl text-xs font-black uppercase tracking-wider text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
                            >
                                Cancelar
                            </button>
                            <button
                                onClick={handleSave}
                                disabled={saving}
                                className="flex items-center gap-2 bg-primary hover:bg-primary/90 disabled:opacity-50 text-white text-xs font-black uppercase tracking-wider px-5 py-2.5 rounded-2xl transition-colors"
                            >
                                {saving ? (
                                    <>
                                        <span className="material-symbols-outlined text-base animate-spin">progress_activity</span>
                                        Guardando...
                                    </>
                                ) : (
                                    <>
                                        <span className="material-symbols-outlined text-base">save</span>
                                        {editingId ? 'Actualizar' : 'Guardar'}
                                    </>
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* ──── Delete Confirmation Modal ──── */}
            {deleteId != null && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
                    <div className="bg-white dark:bg-slate-900 rounded-3xl shadow-2xl w-full max-w-sm border border-slate-200 dark:border-slate-800 p-6 text-center space-y-4">
                        <span className="material-symbols-outlined text-red-500 text-5xl">delete_forever</span>
                        <h3 className="text-lg font-black text-slate-900 dark:text-white">¿Eliminar resultado?</h3>
                        <p className="text-sm text-slate-500">Esta acción no se puede deshacer.</p>
                        <div className="flex justify-center gap-3 pt-2">
                            <button
                                onClick={() => setDeleteId(null)}
                                className="px-5 py-2.5 rounded-2xl text-xs font-black uppercase tracking-wider text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
                            >
                                Cancelar
                            </button>
                            <button
                                onClick={handleDelete}
                                className="flex items-center gap-2 bg-red-600 hover:bg-red-700 text-white text-xs font-black uppercase tracking-wider px-5 py-2.5 rounded-2xl transition-colors"
                            >
                                <span className="material-symbols-outlined text-base">delete</span>
                                Eliminar
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default LabResultsView;
