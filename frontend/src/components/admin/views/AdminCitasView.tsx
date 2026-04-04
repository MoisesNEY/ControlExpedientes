import { useState, useEffect } from 'react';
import { CitaService, type CitaMedicaDTO } from '../../../services/cita.service';
import { PacienteService, type PacienteDTO } from '../../../services/paciente.service';
import { UserService, type PublicUser } from '../../../services/userService';

const emptyCita: CitaMedicaDTO = {
    fechaHora: '',
    estado: 'PROGRAMADA',
    observaciones: '',
    paciente: undefined,
    user: undefined,
};

const AdminCitasView = () => {
    const [citas, setCitas] = useState<CitaMedicaDTO[]>([]);
    const [pacientes, setPacientes] = useState<PacienteDTO[]>([]);
    const [medicos, setMedicos] = useState<PublicUser[]>([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState<'TODAS' | 'PROGRAMADA' | 'ATENDIDA' | 'CANCELADA'>('TODAS');
    const [showForm, setShowForm] = useState(false);
    const [editing, setEditing] = useState<CitaMedicaDTO | null>(null);
    const [form, setForm] = useState<CitaMedicaDTO>(emptyCita);
    const [saving, setSaving] = useState(false);

    const fetchCitas = async () => {
        setLoading(true);
        try {
            const params: Record<string, any> = { sort: 'fechaHora,desc', size: 50 };
            if (filter !== 'TODAS') params['estado.equals'] = filter;
            const data = await CitaService.getAll(params);
            setCitas(data);
        } catch (error) {
            console.error('Error fetching citas:', error);
        } finally {
            setLoading(false);
        }
    };

    const fetchPacientes = async () => {
        try {
            const data = await PacienteService.getAll({ 'activo.equals': true, size: 200, sort: 'nombres,asc' });
            setPacientes(data);
        } catch (error) {
            console.error('Error fetching pacientes:', error);
        }
    };

    const fetchMedicos = async () => {
        try {
            const data = await UserService.getMedicos({ size: 200, sort: 'login,asc' });
            setMedicos(data);
        } catch (error) {
            console.error('Error fetching medicos:', error);
        }
    };

    useEffect(() => { fetchCitas(); }, [filter]);
    useEffect(() => { fetchPacientes(); fetchMedicos(); }, []);

    const openCreate = () => {
        setEditing(null);
        const now = new Date();
        now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
        setForm({ ...emptyCita, fechaHora: now.toISOString().slice(0, 16) });
        setShowForm(true);
    };

    const openEdit = (c: CitaMedicaDTO) => {
        setEditing(c);
        setForm({
            ...c,
            fechaHora: c.fechaHora ? c.fechaHora.slice(0, 16) : '',
        });
        setShowForm(true);
    };

    const handleClose = () => { setShowForm(false); setEditing(null); setForm(emptyCita); };

    const handleSave = async () => {
        if (!form.fechaHora || !form.paciente?.id) {
            alert('Por favor seleccione un paciente y una fecha/hora.');
            return;
        }
        if (!form.user?.id) {
            alert('Por favor seleccione el médico responsable de la cita.');
            return;
        }
        setSaving(true);
        try {
            const payload = {
                ...form,
                fechaHora: new Date(form.fechaHora).toISOString(),
            };
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

    const estadoColor = (estado: string) => {
        switch (estado) {
            case 'ATENDIDA': return 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600';
            case 'PROGRAMADA': return 'bg-blue-100 dark:bg-blue-900/30 text-blue-600';
            case 'CANCELADA': return 'bg-red-100 dark:bg-red-900/30 text-red-600';
            default: return 'bg-slate-100 text-slate-500';
        }
    };

    return (
        <div className="p-4 md:p-8 space-y-4 md:space-y-6">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white">Gestión de Citas Médicas</h2>
                    <p className="text-slate-500 text-xs md:text-sm font-medium">Programar, editar y administrar citas del sistema.</p>
                </div>
                <button onClick={openCreate} className="w-full md:w-auto flex items-center justify-center gap-2 px-6 py-3 bg-amber-600 text-white rounded-xl font-bold shadow-lg shadow-amber-600/30 hover:scale-105 transition-transform">
                    <span className="material-symbols-outlined">calendar_add_on</span>
                    Nueva Cita
                </button>
            </div>

            <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                <div className="p-4 md:p-6 border-b border-slate-100 dark:border-slate-800">
                    <div className="flex bg-slate-100 dark:bg-slate-800 p-1 rounded-xl w-full md:w-auto overflow-x-auto scrollbar-hide">
                        {(['TODAS', 'PROGRAMADA', 'ATENDIDA', 'CANCELADA'] as const).map(f => (
                            <button key={f} onClick={() => setFilter(f)}
                                className={`flex-1 md:flex-none px-4 py-2 rounded-lg text-xs font-bold transition-all whitespace-nowrap ${
                                    filter === f ? 'bg-white dark:bg-slate-700 text-amber-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'
                                }`}>
                                {f}
                            </button>
                        ))}
                    </div>
                </div>
                <div className="overflow-x-auto scrollbar-hide">
                    <table className="w-full text-left min-w-[800px]">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest">
                            <tr>
                                <th className="px-6 py-4">Fecha / Hora</th>
                                <th className="px-6 py-4">Paciente</th>
                                <th className="px-6 py-4">Observaciones</th>
                                <th className="px-6 py-4">Estado</th>
                                <th className="px-6 py-4 text-right">Acciones</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                            {loading ? (
                                Array(5).fill(0).map((_, i) => (
                                    <tr key={i} className="animate-pulse"><td colSpan={5} className="px-6 py-6"><div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-full"></div></td></tr>
                                ))
                            ) : (
                                citas.map(c => {
                                    const { date, time } = formatDateTime(c.fechaHora);
                                    return (
                                        <tr key={c.id} className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors">
                                            <td className="px-6 py-4">
                                                <p className="text-sm font-bold text-slate-900 dark:text-white">{time}</p>
                                                <p className="text-[10px] text-slate-400 font-bold uppercase">{date}</p>
                                            </td>
                                            <td className="px-6 py-4">
                                                <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">
                                                    {c.paciente?.nombres ? `${c.paciente.nombres} ${c.paciente.apellidos || ''}` : 'N/A'}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 text-xs text-slate-500 dark:text-slate-400 truncate max-w-xs">{c.observaciones || 'Sin observaciones'}</td>
                                            <td className="px-6 py-4">
                                                <span className={`px-2 py-1 rounded-lg text-[10px] font-black uppercase ${estadoColor(c.estado)}`}>{c.estado}</span>
                                            </td>
                                            <td className="px-6 py-4 text-right">
                                                <div className="flex items-center justify-end gap-1">
                                                    <button onClick={() => openEdit(c)} className="p-2 text-slate-400 hover:text-amber-600 transition-colors"><span className="material-symbols-outlined text-sm">edit</span></button>
                                                    <button onClick={() => c.id && handleDelete(c.id)} className="p-2 text-slate-400 hover:text-red-500 transition-colors"><span className="material-symbols-outlined text-sm">delete</span></button>
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })
                            )}
                            {!loading && citas.length === 0 && (
                                <tr><td colSpan={5} className="px-6 py-20 text-center text-slate-400 italic font-medium">No se encontraron citas.</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Modal Form */}
            {showForm && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={handleClose} />
                    <div className="relative bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 w-full max-w-md">
                        <div className="p-6 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center">
                            <h3 className="text-lg font-black text-slate-900 dark:text-white">{editing ? 'Editar Cita' : 'Nueva Cita'}</h3>
                            <button onClick={handleClose} className="text-slate-400 hover:text-slate-600"><span className="material-symbols-outlined">close</span></button>
                        </div>
                        <div className="p-6 space-y-4">
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Paciente *</label>
                                <select
                                    value={form.paciente?.id || ''}
                                    onChange={e => setForm({ ...form, paciente: { id: parseInt(e.target.value) } })}
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600"
                                >
                                    <option value="">— Seleccionar paciente —</option>
                                    {pacientes.map(p => (
                                        <option key={p.id} value={p.id}>{p.nombres} {p.apellidos} ({p.codigo})</option>
                                    ))}
                                </select>
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Médico *</label>
                                <select
                                    value={form.user?.id || ''}
                                    onChange={e => setForm({ 
                                        ...form, 
                                        user: e.target.value ? { id: e.target.value } : undefined 
                                    })}
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600"
                                >
                                    <option value="">— Seleccionar médico —</option>
                                    {medicos.map(m => (
                                        <option key={m.id} value={m.id}>{m.login}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Fecha y Hora *</label>
                                <input type="datetime-local" value={form.fechaHora} onChange={e => setForm({ ...form, fechaHora: e.target.value })}
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600 text-slate-900 dark:text-white" />
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Estado</label>
                                <select value={form.estado} onChange={e => setForm({ ...form, estado: e.target.value as any })}
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600">
                                    <option value="PROGRAMADA">Programada</option>
                                    <option value="ATENDIDA">Atendida</option>
                                    <option value="CANCELADA">Cancelada</option>
                                </select>
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Observaciones</label>
                                <textarea value={form.observaciones || ''} onChange={e => setForm({ ...form, observaciones: e.target.value })} placeholder="Motivo de la cita..."
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600 min-h-[80px] text-slate-900 dark:text-white" />
                            </div>
                        </div>
                        <div className="p-6 border-t border-slate-100 dark:border-slate-800 flex justify-end gap-3">
                            <button onClick={handleClose} className="px-6 py-2.5 text-sm font-bold text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors">Cancelar</button>
                            <button onClick={handleSave} disabled={saving}
                                className={`px-6 py-2.5 text-sm font-bold text-white bg-amber-600 rounded-xl shadow-lg shadow-amber-600/30 hover:scale-105 transition-all ${saving ? 'opacity-70 cursor-not-allowed' : ''}`}>
                                {saving ? 'Guardando...' : editing ? 'Actualizar' : 'Crear Cita'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminCitasView;
