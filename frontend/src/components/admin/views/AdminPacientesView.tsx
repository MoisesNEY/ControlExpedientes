import { useState, useEffect } from 'react';
import { PacienteService, type PacienteDTO } from '../../../services/paciente.service';

const emptyPaciente: PacienteDTO = {
    codigo: '',
    nombres: '',
    apellidos: '',
    sexo: 'MASCULINO',
    fechaNacimiento: '',
    cedula: '',
    telefono: '',
    direccion: '',
    estadoCivil: undefined,
    email: '',
    activo: true,
};

const AdminPacientesView = () => {
    const [pacientes, setPacientes] = useState<PacienteDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [editing, setEditing] = useState<PacienteDTO | null>(null);
    const [form, setForm] = useState<PacienteDTO>(emptyPaciente);
    const [saving, setSaving] = useState(false);

    const fetchPacientes = async () => {
        setLoading(true);
        try {
            const params: Record<string, any> = { sort: 'id,desc' };
            if (searchTerm) params['nombres.contains'] = searchTerm;
            const data = await PacienteService.getAll(params);
            setPacientes(data);
        } catch (error) {
            console.error('Error fetching pacientes:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const timeout = setTimeout(fetchPacientes, 300);
        return () => clearTimeout(timeout);
    }, [searchTerm]);

    const openCreate = () => {
        setEditing(null);
        setForm(emptyPaciente);
        setShowForm(true);
    };

    const openEdit = (p: PacienteDTO) => {
        setEditing(p);
        setForm({ ...p });
        setShowForm(true);
    };

    const handleClose = () => {
        setShowForm(false);
        setEditing(null);
        setForm(emptyPaciente);
    };

    const handleSave = async () => {
        if (!form.codigo || !form.nombres || !form.apellidos || !form.fechaNacimiento) {
            alert('Por favor complete los campos obligatorios: código, nombres, apellidos y fecha de nacimiento.');
            return;
        }
        setSaving(true);
        try {
            if (editing?.id) {
                await PacienteService.update(editing.id, form);
            } else {
                await PacienteService.create(form);
            }
            handleClose();
            fetchPacientes();
        } catch (error: any) {
            console.error('Error saving paciente:', error);
            alert(error?.response?.data?.message || 'Error al guardar el paciente.');
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id: number) => {
        if (!confirm('¿Está seguro de eliminar este paciente? Esta acción no se puede deshacer.')) return;
        try {
            await PacienteService.delete(id);
            fetchPacientes();
        } catch (error: any) {
            console.error('Error deleting paciente:', error);
            alert(error?.response?.data?.message || 'Error al eliminar el paciente.');
        }
    };

    const calculateAge = (birthday: string) => {
        if (!birthday) return 'N/A';
        const ageDifMs = Date.now() - new Date(birthday).getTime();
        const ageDate = new Date(ageDifMs);
        return Math.abs(ageDate.getUTCFullYear() - 1970) + ' años';
    };

    return (
        <div className="p-4 md:p-8 space-y-4 md:space-y-6">
            {/* Header */}
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white">Gestión de Pacientes</h2>
                    <p className="text-slate-500 text-xs md:text-sm font-medium">Crear, editar y administrar los pacientes del sistema.</p>
                </div>
                <button
                    onClick={openCreate}
                    className="w-full md:w-auto flex items-center justify-center gap-2 px-6 py-3 bg-amber-600 text-white rounded-xl font-bold shadow-lg shadow-amber-600/30 hover:scale-105 transition-transform"
                >
                    <span className="material-symbols-outlined">person_add</span>
                    Nuevo Paciente
                </button>
            </div>

            {/* Table */}
            <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden transition-colors">
                <div className="p-4 md:p-6 border-b border-slate-100 dark:border-slate-800">
                    <div className="relative w-full md:w-96">
                        <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400">search</span>
                        <input
                            type="text"
                            placeholder="Buscar por nombre..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-12 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-xs outline-none focus:ring-2 focus:ring-amber-600 transition-all"
                        />
                    </div>
                </div>

                <div className="overflow-x-auto scrollbar-hide">
                    <table className="w-full text-left min-w-[900px]">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest">
                            <tr>
                                <th className="px-6 py-4">Código</th>
                                <th className="px-6 py-4">Paciente</th>
                                <th className="px-6 py-4">Cédula</th>
                                <th className="px-6 py-4">Sexo</th>
                                <th className="px-6 py-4">Edad</th>
                                <th className="px-6 py-4">Estado</th>
                                <th className="px-6 py-4 text-right">Acciones</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                            {loading ? (
                                Array(5).fill(0).map((_, i) => (
                                    <tr key={i} className="animate-pulse">
                                        <td colSpan={7} className="px-6 py-6"><div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-full"></div></td>
                                    </tr>
                                ))
                            ) : (
                                pacientes.map(p => (
                                    <tr key={p.id} className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors">
                                        <td className="px-6 py-4 text-xs font-bold text-amber-600">{p.codigo}</td>
                                        <td className="px-6 py-4">
                                            <div>
                                                <p className="font-bold text-slate-800 dark:text-white text-sm">{p.nombres} {p.apellidos}</p>
                                                <p className="text-[10px] text-slate-400">{p.email || 'Sin email'}</p>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-sm text-slate-600 dark:text-slate-400">{p.cedula || 'N/A'}</td>
                                        <td className="px-6 py-4">
                                            <span className={`px-2 py-1 rounded-lg text-[10px] font-black uppercase ${p.sexo === 'MASCULINO' ? 'bg-blue-100 dark:bg-blue-900/30 text-blue-600' : p.sexo === 'FEMENINO' ? 'bg-pink-100 dark:bg-pink-900/30 text-pink-600' : 'bg-slate-100 dark:bg-slate-800 text-slate-500'}`}>
                                                {p.sexo}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-sm text-slate-600 dark:text-slate-400">{calculateAge(p.fechaNacimiento)}</td>
                                        <td className="px-6 py-4">
                                            <span className={`px-2 py-1 rounded-lg text-[10px] font-black uppercase ${p.activo ? 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600' : 'bg-red-100 dark:bg-red-900/30 text-red-600'}`}>
                                                {p.activo ? 'Activo' : 'Inactivo'}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <div className="flex items-center justify-end gap-1">
                                                <button onClick={() => openEdit(p)} className="p-2 text-slate-400 hover:text-amber-600 transition-colors" title="Editar">
                                                    <span className="material-symbols-outlined text-sm">edit</span>
                                                </button>
                                                <button onClick={() => p.id && handleDelete(p.id)} className="p-2 text-slate-400 hover:text-red-500 transition-colors" title="Eliminar">
                                                    <span className="material-symbols-outlined text-sm">delete</span>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                            {!loading && pacientes.length === 0 && (
                                <tr>
                                    <td colSpan={7} className="px-6 py-20 text-center text-slate-400 italic font-medium">No se encontraron pacientes.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Modal Form */}
            {showForm && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={handleClose} />
                    <div className="relative bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                        <div className="sticky top-0 bg-white dark:bg-slate-900 p-6 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center z-10">
                            <h3 className="text-lg font-black text-slate-900 dark:text-white">
                                {editing ? 'Editar Paciente' : 'Nuevo Paciente'}
                            </h3>
                            <button onClick={handleClose} className="text-slate-400 hover:text-slate-600 transition-colors">
                                <span className="material-symbols-outlined">close</span>
                            </button>
                        </div>
                        <div className="p-6 space-y-4">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <FormField label="Código *" value={form.codigo} onChange={v => setForm({ ...form, codigo: v })} placeholder="PAC-001" />
                                <FormField label="Nombres *" value={form.nombres} onChange={v => setForm({ ...form, nombres: v })} placeholder="Juan Carlos" />
                                <FormField label="Apellidos *" value={form.apellidos} onChange={v => setForm({ ...form, apellidos: v })} placeholder="Pérez López" />
                                <div className="flex flex-col gap-1.5">
                                    <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Sexo *</label>
                                    <select
                                        value={form.sexo}
                                        onChange={e => setForm({ ...form, sexo: e.target.value as any })}
                                        className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600"
                                    >
                                        <option value="MASCULINO">Masculino</option>
                                        <option value="FEMENINO">Femenino</option>
                                        <option value="OTRO">Otro</option>
                                    </select>
                                </div>
                                <FormField label="Fecha de Nacimiento *" type="date" value={form.fechaNacimiento} onChange={v => setForm({ ...form, fechaNacimiento: v })} />
                                <FormField label="Cédula" value={form.cedula || ''} onChange={v => setForm({ ...form, cedula: v })} placeholder="001-010190-0001A" />
                                <FormField label="Teléfono" value={form.telefono || ''} onChange={v => setForm({ ...form, telefono: v })} placeholder="+505 8888-0000" />
                                <FormField label="Email" type="email" value={form.email || ''} onChange={v => setForm({ ...form, email: v })} placeholder="correo@ejemplo.com" />
                                <div className="flex flex-col gap-1.5">
                                    <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Estado Civil</label>
                                    <select
                                        value={form.estadoCivil || ''}
                                        onChange={e => setForm({ ...form, estadoCivil: e.target.value as any || undefined })}
                                        className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600"
                                    >
                                        <option value="">— Seleccionar —</option>
                                        <option value="SOLTERO">Soltero</option>
                                        <option value="CASADO">Casado</option>
                                        <option value="DIVORCIADO">Divorciado</option>
                                        <option value="VIUDO">Viudo</option>
                                    </select>
                                </div>
                                <div className="flex items-center gap-3 pt-6">
                                    <input
                                        type="checkbox"
                                        checked={form.activo}
                                        onChange={e => setForm({ ...form, activo: e.target.checked })}
                                        className="w-4 h-4 accent-amber-600"
                                    />
                                    <label className="text-sm font-bold text-slate-700 dark:text-slate-300">Paciente Activo</label>
                                </div>
                            </div>
                            <FormField label="Dirección" value={form.direccion || ''} onChange={v => setForm({ ...form, direccion: v })} placeholder="Barrio, calle, casa #..." />
                        </div>
                        <div className="sticky bottom-0 bg-white dark:bg-slate-900 p-6 border-t border-slate-100 dark:border-slate-800 flex justify-end gap-3">
                            <button onClick={handleClose} className="px-6 py-2.5 text-sm font-bold text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors">
                                Cancelar
                            </button>
                            <button
                                onClick={handleSave}
                                disabled={saving}
                                className={`px-6 py-2.5 text-sm font-bold text-white bg-amber-600 rounded-xl shadow-lg shadow-amber-600/30 hover:scale-105 transition-all ${saving ? 'opacity-70 cursor-not-allowed' : ''}`}
                            >
                                {saving ? 'Guardando...' : editing ? 'Actualizar' : 'Crear Paciente'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

const FormField = ({ label, value, onChange, placeholder, type = 'text' }: {
    label: string; value: string; onChange: (v: string) => void; placeholder?: string; type?: string;
}) => (
    <div className="flex flex-col gap-1.5">
        <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">{label}</label>
        <input
            type={type}
            value={value}
            onChange={e => onChange(e.target.value)}
            placeholder={placeholder}
            className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600 transition-all text-slate-900 dark:text-white"
        />
    </div>
);

export default AdminPacientesView;
