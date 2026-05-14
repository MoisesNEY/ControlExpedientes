import { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import { CitaService, type CitaMedicaDTO } from '../../../services/cita.service';
import { PacienteService, type PacienteDTO } from '../../../services/paciente.service';
import { UserService, type PublicUser } from '../../../services/userService';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import SearchableSelect, { type SearchableSelectOption } from '../../ui/SearchableSelect';

const emptyCita: CitaMedicaDTO = {
    fechaHora: '',
    estado: 'PROGRAMADA',
    observaciones: '',
    paciente: undefined,
    user: undefined,
};

// ─── Status display helpers ────────────────────────────────────────────────────
const estadoLabel = (estado: string) => {
    switch (estado) {
        case 'PROGRAMADA': return 'Programada';
        case 'EN_SALA_ESPERA': return 'En Sala de Espera';
        case 'EN_TRIAGE': return 'En Triage';
        case 'ESPERANDO_MEDICO': return 'Esperando Médico';
        case 'EN_CONSULTA': return 'En Consulta';
        case 'ATENDIDA': return 'Atendida';
        case 'CANCELADA': return 'Cancelada';
        default: return estado;
    }
};

const estadoColor = (estado: string) => {
    switch (estado) {
        case 'PROGRAMADA': return 'bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400';
        case 'EN_SALA_ESPERA': return 'bg-amber-100 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400';
        case 'EN_TRIAGE': return 'bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400';
        case 'ESPERANDO_MEDICO': return 'bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400';
        case 'EN_CONSULTA': return 'bg-cyan-100 dark:bg-cyan-900/30 text-cyan-600 dark:text-cyan-400';
        case 'ATENDIDA': return 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400';
        case 'CANCELADA': return 'bg-red-100 dark:bg-red-900/30 text-red-500 dark:text-red-400';
        default: return 'bg-slate-100 dark:bg-slate-800 text-slate-500';
    }
};

const estadoEmoji = (estado: string) => {
    switch (estado) {
        case 'PROGRAMADA': return '🔵';
        case 'EN_SALA_ESPERA': return '🟡';
        case 'EN_TRIAGE': return '🟣';
        case 'ESPERANDO_MEDICO': return '🟠';
        case 'EN_CONSULTA': return '🔷';
        case 'ATENDIDA': return '🟢';
        case 'CANCELADA': return '🔴';
        default: return '⚪';
    }
};

const ALL_ESTADOS = ['PROGRAMADA', 'EN_SALA_ESPERA', 'EN_TRIAGE', 'ESPERANDO_MEDICO', 'EN_CONSULTA', 'ATENDIDA', 'CANCELADA'] as const;

// ─── Main Component ────────────────────────────────────────────────────────────
const ReceptionAgendaView = () => {
    const location = useLocation();
    const { hasRole } = useAuth();

    const [citas, setCitas] = useState<CitaMedicaDTO[]>([]);
    const [pacientes, setPacientes] = useState<PacienteDTO[]>([]);
    const [medicos, setMedicos] = useState<PublicUser[]>([]);
    const [loading, setLoading] = useState(true);
    const [checkingIn, setCheckingIn] = useState<number | null>(null);
    const [checkInBanner, setCheckInBanner] = useState<string | null>(null);

    type FilterState = 'TODAS' | 'PROGRAMADA' | 'EN_SALA_ESPERA' | 'EN_TRIAGE' | 'ESPERANDO_MEDICO' | 'EN_CONSULTA' | 'ATENDIDA' | 'CANCELADA';
    type DateFilter = 'HOY' | 'SEMANA' | 'TODAS';

    const [statusFilter, setStatusFilter] = useState<FilterState>('TODAS');
    const [dateFilter, setDateFilter] = useState<DateFilter>('HOY');
    const [showForm, setShowForm] = useState(false);
    const [editing, setEditing] = useState<CitaMedicaDTO | null>(null);
    const [form, setForm] = useState<CitaMedicaDTO>(emptyCita);
    const [saving, setSaving] = useState(false);

    const pacienteOptions = useMemo<SearchableSelectOption[]>(() => {
        const options = new Map<string, SearchableSelectOption>();

        if (form.paciente?.id) {
            const label = form.paciente.nombres
                ? `${form.paciente.nombres} ${form.paciente.apellidos || ''}`.trim()
                : `Paciente #${form.paciente.id}`;
            options.set(String(form.paciente.id), {
                value: String(form.paciente.id),
                label,
                description: form.paciente.id ? `ID ${form.paciente.id}` : undefined,
            });
        }

        pacientes.forEach(paciente => {
            if (!paciente.id) return;
            options.set(String(paciente.id), {
                value: String(paciente.id),
                label: `${paciente.nombres} ${paciente.apellidos}`.trim(),
                description: paciente.cedula || paciente.codigo,
                keywords: [paciente.codigo, paciente.cedula || '', paciente.nombres, paciente.apellidos],
            });
        });

        return Array.from(options.values());
    }, [form.paciente, pacientes]);

    const medicoOptions = useMemo<SearchableSelectOption[]>(() => {
        const options = new Map<string, SearchableSelectOption>();

        if (form.user?.id) {
            options.set(form.user.id, {
                value: form.user.id,
                label: form.user.login || `Usuario ${form.user.id}`,
                description: form.user.id,
            });
        }

        medicos.forEach(medico => {
            options.set(medico.id, {
                value: medico.id,
                label: medico.login,
                description: medico.id,
                keywords: [medico.login, medico.id],
            });
        });

        return Array.from(options.values());
    }, [form.user, medicos]);

    const canDeleteAppointments = hasRole('ROLE_ADMIN');

    // Auto-open new appointment form if navigated with ?action=new
    const autoOpenedRef = useRef(false);
    useEffect(() => {
        if (!autoOpenedRef.current && location.search.includes('action=new')) {
            autoOpenedRef.current = true;
            openCreate();
        }
    }, [location.search]);

    // ── Date range helpers ──────────────────────────────────────────────────
    const getDateRange = useCallback(() => {
        const now = new Date();
        if (dateFilter === 'HOY') {
            const start = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0);
            const end = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59);
            return { start: start.toISOString(), end: end.toISOString() };
        }
        if (dateFilter === 'SEMANA') {
            const day = now.getDay();
            const diffToMonday = (day === 0 ? -6 : 1 - day);
            const start = new Date(now);
            start.setDate(now.getDate() + diffToMonday);
            start.setHours(0, 0, 0, 0);
            const end = new Date(start);
            end.setDate(start.getDate() + 6);
            end.setHours(23, 59, 59, 999);
            return { start: start.toISOString(), end: end.toISOString() };
        }
        return null;
    }, [dateFilter]);

    // ── Fetch ──────────────────────────────────────────────────────────────
    const fetchCitas = useCallback(async () => {
        setLoading(true);
        try {
            const params: Record<string, any> = { sort: 'fechaHora,asc', size: 100 };
            if (statusFilter !== 'TODAS') params['estado.equals'] = statusFilter;
            const range = getDateRange();
            if (range) {
                params['fechaHora.greaterThanOrEqual'] = range.start;
                params['fechaHora.lessThanOrEqual'] = range.end;
            }
            const data = await CitaService.getAll(params);
            setCitas(data);
        } finally {
            setLoading(false);
        }
    }, [statusFilter, getDateRange]);

    useEffect(() => { fetchCitas(); }, [fetchCitas]);

    useEffect(() => {
        PacienteService.getAll({ 'activo.equals': true, size: 300, sort: 'nombres,asc' })
            .then(setPacientes).catch(console.error);
        UserService.getMedicos({ size: 200, sort: 'login,asc' })
            .then(setMedicos).catch(console.error);
    }, []);

    // ── Form handlers ──────────────────────────────────────────────────────
    const openCreate = () => {
        setEditing(null);
        const now = new Date();
        now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
        setForm({ ...emptyCita, fechaHora: now.toISOString().slice(0, 16) });
        setShowForm(true);
    };

    const openEdit = (c: CitaMedicaDTO) => {
        setEditing(c);
        setForm({ ...c, fechaHora: c.fechaHora ? c.fechaHora.slice(0, 16) : '' });
        setShowForm(true);
    };

    const handleClose = () => { setShowForm(false); setEditing(null); setForm(emptyCita); };

    const handleSave = async () => {
        if (!form.fechaHora || !form.paciente?.id) {
            alert('Por favor seleccione un paciente y una fecha/hora.');
            return;
        }
        setSaving(true);
        try {
            const payload = { ...form, fechaHora: new Date(form.fechaHora).toISOString() };
            if (editing?.id) {
                await CitaService.update(editing.id, payload);
            } else {
                await CitaService.create(payload);
            }
            handleClose();
            fetchCitas();
        } catch (error: any) {
            alert(error?.response?.data?.message || 'Error al guardar la cita.');
        } finally {
            setSaving(false);
        }
    };

    // ╔══════════════════════════════════════════════════════╗
    // ║  CHECK-IN: Marca la cita como EN_SALA_ESPERA        ║
    // ║  Requiere que el backend tenga ese estado en el      ║
    // ║  enum EstadoCita. Ver instrucciones al pie del       ║
    // ║  archivo para activar esta funcionalidad.            ║
    // ╚══════════════════════════════════════════════════════╝
    const handleCheckIn = async (cita: CitaMedicaDTO) => {
        if (!cita.id) return;
        setCheckingIn(cita.id);
        try {
            await CitaService.patch(cita.id, { estado: 'EN_SALA_ESPERA' as any });
            const nombrePaciente = cita.paciente?.nombres
                ? `${cita.paciente.nombres} ${cita.paciente.apellidos || ''}`.trim()
                : 'Paciente';
            setCheckInBanner(nombrePaciente);
            setTimeout(() => setCheckInBanner(null), 4000);
            await fetchCitas();
        } catch (error: any) {
            alert(error?.response?.data?.message || 'Error al registrar el check-in.');
        } finally {
            setCheckingIn(null);
        }
    };

    const handleDelete = async (id: number) => {
        if (!confirm('¿Eliminar esta cita?')) return;
        try {
            await CitaService.delete(id);
            fetchCitas();
        } catch (error: any) {
            alert(error?.response?.data?.message || 'Error al eliminar.');
        }
    };

    const formatDateTime = (dateStr: string) => {
        const d = new Date(dateStr);
        return {
            date: d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' }),
            time: d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        };
    };

    // ── Status counts (computed from loaded citas) ────────────────────────
    const statusCounts = citas.reduce<Record<string, number>>((acc, c) => {
        acc[c.estado] = (acc[c.estado] || 0) + 1;
        return acc;
    }, {});

    // ── Filter label helper ─────────────────────────────────────────────
    const filterLabel = (f: FilterState) => {
        if (f === 'TODAS') return 'Todas';
        return estadoLabel(f);
    };

    // ─── Render ─────────────────────────────────────────────────────────────────
    return (
        <div className="p-4 md:p-8 space-y-6">
            {/* Check-in success banner */}
            {checkInBanner && (
                <div className="flex items-center gap-2 px-4 py-3 bg-emerald-100 dark:bg-emerald-900/30 border border-emerald-200 dark:border-emerald-800 text-emerald-700 dark:text-emerald-400 rounded-xl text-sm font-bold animate-pulse">
                    <span className="material-symbols-outlined text-lg">check_circle</span>
                    ✓ Check-in registrado para {checkInBanner}
                </div>
            )}

            {/* Header */}
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white">Agenda Médica</h2>
                    <p className="text-slate-500 text-xs md:text-sm font-medium">Gestiona citas y registra la llegada de pacientes (Check-In).</p>
                </div>
                <button
                    onClick={openCreate}
                    className="w-full md:w-auto flex items-center justify-center gap-2 px-6 py-3 bg-primary text-white rounded-xl font-bold shadow-lg shadow-primary/30 hover:scale-105 transition-transform"
                >
                    <span className="material-symbols-outlined">calendar_add_on</span>
                    Nueva Cita
                </button>
            </div>

            {/* Status count badges */}
            {!loading && citas.length > 0 && (
                <div className="flex flex-wrap gap-2">
                    {ALL_ESTADOS.map(estado => (
                        <span
                            key={estado}
                            className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold ${estadoColor(estado)}`}
                        >
                            {estadoEmoji(estado)} {estadoLabel(estado)}: {statusCounts[estado] || 0}
                        </span>
                    ))}
                </div>
            )}

            {/* Filters */}
            <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 p-4 flex flex-col sm:flex-row gap-4 shadow-sm">
                {/* Date filter */}
                <div className="flex bg-slate-100 dark:bg-slate-800 p-1 rounded-xl gap-1">
                    {(['HOY', 'SEMANA', 'TODAS'] as DateFilter[]).map(d => (
                        <button
                            key={d}
                            onClick={() => setDateFilter(d)}
                            className={`px-4 py-2 rounded-lg text-xs font-bold transition-all ${dateFilter === d ? 'bg-white dark:bg-slate-700 text-primary shadow-sm' : 'text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'}`}
                        >
                            {d === 'HOY' ? 'Hoy' : d === 'SEMANA' ? 'Esta Semana' : 'Todas'}
                        </button>
                    ))}
                </div>
                {/* Status filter */}
                <div className="flex flex-1 bg-slate-100 dark:bg-slate-800 p-1 rounded-xl gap-1 overflow-x-auto">
                    {(['TODAS', ...ALL_ESTADOS] as FilterState[]).map(f => (
                        <button
                            key={f}
                            onClick={() => setStatusFilter(f)}
                            className={`flex-none px-4 py-2 rounded-lg text-xs font-bold transition-all whitespace-nowrap ${statusFilter === f ? 'bg-white dark:bg-slate-700 text-primary shadow-sm' : 'text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'}`}
                        >
                            {filterLabel(f)}
                        </button>
                    ))}
                </div>
            </div>

            {/* Table */}
            <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left min-w-[750px]">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest">
                            <tr>
                                <th className="px-6 py-4">Hora</th>
                                <th className="px-6 py-4">Paciente</th>
                                <th className="px-6 py-4">Médico</th>
                                <th className="px-6 py-4">Estado</th>
                                <th className="px-6 py-4 text-right">Acciones</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                            {loading ? (
                                Array(5).fill(0).map((_, i) => (
                                    <tr key={i} className="animate-pulse">
                                        <td colSpan={5} className="px-6 py-5">
                                            <div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-full" />
                                        </td>
                                    </tr>
                                ))
                            ) : citas.length === 0 ? (
                                <tr>
                                    <td colSpan={5} className="px-6 py-20 text-center">
                                        <span className="material-symbols-outlined text-4xl text-slate-300 dark:text-slate-700 block mb-2">event_busy</span>
                                        <span className="text-slate-400 italic font-medium text-sm">No hay citas para los filtros seleccionados.</span>
                                    </td>
                                </tr>
                            ) : (
                                citas.map(c => {
                                    const { date, time } = formatDateTime(c.fechaHora);
                                    const isCheckingIn = c.id === checkingIn;
                                    return (
                                        <tr key={c.id} className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/30 transition-colors">
                                            <td className="px-6 py-4">
                                                <p className="text-sm font-black text-slate-900 dark:text-white">{time}</p>
                                                <p className="text-[10px] text-slate-400 font-bold uppercase">{date}</p>
                                            </td>
                                            <td className="px-6 py-4">
                                                <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">
                                                    {c.paciente?.nombres
                                                        ? `${c.paciente.nombres} ${c.paciente.apellidos || ''}`.trim()
                                                        : 'N/A'}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 text-xs text-slate-500">
                                                {c.user?.login || 'Sin asignar'}
                                            </td>
                                            <td className="px-6 py-4">
                                                <span className={`px-2.5 py-1 rounded-lg text-[10px] font-black uppercase ${estadoColor(c.estado)}`}>
                                                    {estadoLabel(c.estado)}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4">
                                                <div className="flex items-center justify-end gap-1">
                                                    {/* Check-In button — only for PROGRAMADA */}
                                                    {c.estado === 'PROGRAMADA' && (
                                                        <button
                                                            onClick={() => handleCheckIn(c)}
                                                            disabled={isCheckingIn}
                                                            title="Registrar llegada del paciente"
                                                            className="flex items-center gap-1.5 px-3 py-1.5 bg-amber-500 text-white rounded-lg text-[10px] font-black uppercase hover:bg-amber-600 transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
                                                        >
                                                            <span className="material-symbols-outlined text-sm">
                                                                {isCheckingIn ? 'hourglass_top' : 'how_to_reg'}
                                                            </span>
                                                            {isCheckingIn ? 'Cargando...' : 'Check-In'}
                                                        </button>
                                                    )}
                                                    <button onClick={() => openEdit(c)} className="p-2 text-slate-400 hover:text-primary transition-colors" title="Editar">
                                                        <span className="material-symbols-outlined text-sm">edit</span>
                                                    </button>
                                                    {canDeleteAppointments && (
                                                        <button onClick={() => c.id && handleDelete(c.id)} className="p-2 text-slate-400 hover:text-red-500 transition-colors" title="Eliminar">
                                                            <span className="material-symbols-outlined text-sm">delete</span>
                                                        </button>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Modal Form */}
            {showForm && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={handleClose} />
                    <div className="relative bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 w-full max-w-3xl">
                        <div className="p-6 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center">
                            <h3 className="text-lg font-black text-slate-900 dark:text-white">
                                {editing ? 'Editar Cita' : 'Nueva Cita'}
                            </h3>
                            <button onClick={handleClose} className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-300">
                                <span className="material-symbols-outlined">close</span>
                            </button>
                        </div>
                        <div className="grid grid-cols-1 gap-4 p-6 md:grid-cols-2">
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Paciente *</label>
                                <SearchableSelect
                                    value={form.paciente?.id ? String(form.paciente.id) : undefined}
                                    options={pacienteOptions}
                                    placeholder="Seleccionar paciente"
                                    searchPlaceholder="Buscar paciente por nombre, código o cédula..."
                                    emptyMessage="No se encontraron pacientes."
                                    onChange={selectedValue => {
                                        const selectedPaciente = pacientes.find(paciente => String(paciente.id) === selectedValue);
                                        setForm({
                                            ...form,
                                            paciente:
                                                selectedPaciente?.id != null
                                                    ? {
                                                        id: selectedPaciente.id,
                                                        nombres: selectedPaciente.nombres,
                                                        apellidos: selectedPaciente.apellidos,
                                                    }
                                                    : { id: parseInt(selectedValue, 10) },
                                        });
                                    }}
                                />
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Médico</label>
                                <SearchableSelect
                                    value={form.user?.id || undefined}
                                    options={medicoOptions}
                                    placeholder="Seleccionar médico"
                                    searchPlaceholder="Buscar médico por usuario..."
                                    emptyMessage="No se encontraron médicos."
                                    onChange={selectedValue => {
                                        const selectedMedico = medicos.find(medico => medico.id === selectedValue);
                                        setForm({
                                            ...form,
                                            user: selectedMedico
                                                ? { id: selectedMedico.id, login: selectedMedico.login }
                                                : { id: selectedValue },
                                        });
                                    }}
                                />
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Fecha y Hora *</label>
                                <input
                                    type="datetime-local"
                                    value={form.fechaHora}
                                    onChange={e => setForm({ ...form, fechaHora: e.target.value })}
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-primary text-slate-900 dark:text-white"
                                />
                            </div>
                            <div className="flex flex-col gap-1.5 md:col-span-2">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Observaciones</label>
                                <textarea
                                    value={form.observaciones || ''}
                                    onChange={e => setForm({ ...form, observaciones: e.target.value })}
                                    placeholder="Motivo de la cita..."
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-primary min-h-[80px] text-slate-900 dark:text-white resize-none"
                                />
                            </div>
                        </div>
                        <div className="p-6 border-t border-slate-100 dark:border-slate-800 flex justify-end gap-3">
                            <button onClick={handleClose} className="px-6 py-2.5 text-sm font-bold text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors">
                                Cancelar
                            </button>
                            <button
                                onClick={handleSave}
                                disabled={saving}
                                className={`px-6 py-2.5 text-sm font-bold text-white bg-primary rounded-xl shadow-lg shadow-primary/30 hover:scale-105 transition-all ${saving ? 'opacity-70 cursor-not-allowed' : ''}`}
                            >
                                {saving ? 'Guardando...' : editing ? 'Actualizar' : 'Crear Cita'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ReceptionAgendaView;
