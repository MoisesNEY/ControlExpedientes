import { useState, useEffect } from 'react';
import { MedicamentoService, type MedicamentoDTO } from '../../../services/medicamento.service';

const emptyMedicamento: MedicamentoDTO = { nombre: '', descripcion: '', stock: 0 };

const AdminMedicamentosView = () => {
    const [medicamentos, setMedicamentos] = useState<MedicamentoDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [editing, setEditing] = useState<MedicamentoDTO | null>(null);
    const [form, setForm] = useState<MedicamentoDTO>(emptyMedicamento);
    const [saving, setSaving] = useState(false);

    const fetchMedicamentos = async () => {
        setLoading(true);
        try {
            const params: Record<string, any> = { sort: 'nombre,asc' };
            if (searchTerm) params['nombre.contains'] = searchTerm;
            const data = await MedicamentoService.getAll(params);
            setMedicamentos(data);
        } catch (error) {
            console.error('Error fetching medicamentos:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const timeout = setTimeout(fetchMedicamentos, 300);
        return () => clearTimeout(timeout);
    }, [searchTerm]);

    const openCreate = () => { setEditing(null); setForm(emptyMedicamento); setShowForm(true); };
    const openEdit = (m: MedicamentoDTO) => { setEditing(m); setForm({ ...m }); setShowForm(true); };
    const handleClose = () => { setShowForm(false); setEditing(null); setForm(emptyMedicamento); };

    const handleSave = async () => {
        if (!form.nombre || form.stock < 0) {
            alert('Por favor complete el nombre y un stock válido.');
            return;
        }
        setSaving(true);
        try {
            if (editing?.id) {
                await MedicamentoService.update(editing.id, form);
            } else {
                await MedicamentoService.create(form);
            }
            handleClose();
            fetchMedicamentos();
        } catch (error: any) {
            alert(error?.response?.data?.message || 'Error al guardar el medicamento.');
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id: number) => {
        if (!confirm('¿Eliminar este medicamento?')) return;
        try {
            await MedicamentoService.delete(id);
            fetchMedicamentos();
        } catch (error: any) {
            alert(error?.response?.data?.message || 'Error al eliminar.');
        }
    };

    return (
        <div className="p-4 md:p-8 space-y-4 md:space-y-6">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white">Gestión de Medicamentos</h2>
                    <p className="text-slate-500 text-xs md:text-sm font-medium">Administrar inventario farmacéutico del sistema.</p>
                </div>
                <button onClick={openCreate} className="w-full md:w-auto flex items-center justify-center gap-2 px-6 py-3 bg-amber-600 text-white rounded-xl font-bold shadow-lg shadow-amber-600/30 hover:scale-105 transition-transform">
                    <span className="material-symbols-outlined">add_circle</span>
                    Nuevo Medicamento
                </button>
            </div>

            <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                <div className="p-4 md:p-6 border-b border-slate-100 dark:border-slate-800">
                    <div className="relative w-full md:w-96">
                        <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400">search</span>
                        <input type="text" placeholder="Buscar medicamento..." value={searchTerm} onChange={e => setSearchTerm(e.target.value)}
                            className="w-full pl-12 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-xs outline-none focus:ring-2 focus:ring-amber-600 transition-all" />
                    </div>
                </div>
                <div className="overflow-x-auto scrollbar-hide">
                    <table className="w-full text-left min-w-[700px]">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest">
                            <tr>
                                <th className="px-6 py-4">Medicamento</th>
                                <th className="px-6 py-4">Descripción</th>
                                <th className="px-6 py-4">Stock</th>
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
                                medicamentos.map(m => (
                                    <tr key={m.id} className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors">
                                        <td className="px-6 py-4 font-bold text-slate-800 dark:text-white">{m.nombre}</td>
                                        <td className="px-6 py-4 text-xs text-slate-500 dark:text-slate-400 truncate max-w-xs">{m.descripcion || 'Sin descripción'}</td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-2">
                                                <div className="w-16 h-1.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                                                    <div className={`h-full rounded-full ${m.stock < 10 ? 'bg-red-500' : m.stock < 50 ? 'bg-orange-500' : 'bg-emerald-500'}`}
                                                        style={{ width: `${Math.min(100, (m.stock / 200) * 100)}%` }}></div>
                                                </div>
                                                <span className={`text-[10px] font-black ${m.stock < 10 ? 'text-red-500' : 'text-slate-500'}`}>{m.stock} uds.</span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className={`px-2 py-1 rounded-lg text-[10px] font-black uppercase ${m.stock > 10 ? 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600' : 'bg-red-100 dark:bg-red-900/30 text-red-600'}`}>
                                                {m.stock > 10 ? 'En Stock' : 'Stock Bajo'}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <div className="flex items-center justify-end gap-1">
                                                <button onClick={() => openEdit(m)} className="p-2 text-slate-400 hover:text-amber-600 transition-colors"><span className="material-symbols-outlined text-sm">edit</span></button>
                                                <button onClick={() => m.id && handleDelete(m.id)} className="p-2 text-slate-400 hover:text-red-500 transition-colors"><span className="material-symbols-outlined text-sm">delete</span></button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                            {!loading && medicamentos.length === 0 && (
                                <tr><td colSpan={5} className="px-6 py-20 text-center text-slate-400 italic font-medium">No se encontraron medicamentos.</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {showForm && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={handleClose} />
                    <div className="relative bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 w-full max-w-md">
                        <div className="p-6 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center">
                            <h3 className="text-lg font-black text-slate-900 dark:text-white">{editing ? 'Editar Medicamento' : 'Nuevo Medicamento'}</h3>
                            <button onClick={handleClose} className="text-slate-400 hover:text-slate-600"><span className="material-symbols-outlined">close</span></button>
                        </div>
                        <div className="p-6 space-y-4">
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Nombre *</label>
                                <input value={form.nombre} onChange={e => setForm({ ...form, nombre: e.target.value })} placeholder="Acetaminofén 500mg"
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600 text-slate-900 dark:text-white" />
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Descripción</label>
                                <textarea value={form.descripcion || ''} onChange={e => setForm({ ...form, descripcion: e.target.value })} placeholder="Analgésico y antipirético..."
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600 min-h-[80px] text-slate-900 dark:text-white" />
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Stock *</label>
                                <input type="number" min="0" value={form.stock} onChange={e => setForm({ ...form, stock: parseInt(e.target.value) || 0 })}
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600 text-slate-900 dark:text-white" />
                            </div>
                        </div>
                        <div className="p-6 border-t border-slate-100 dark:border-slate-800 flex justify-end gap-3">
                            <button onClick={handleClose} className="px-6 py-2.5 text-sm font-bold text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors">Cancelar</button>
                            <button onClick={handleSave} disabled={saving}
                                className={`px-6 py-2.5 text-sm font-bold text-white bg-amber-600 rounded-xl shadow-lg shadow-amber-600/30 hover:scale-105 transition-all ${saving ? 'opacity-70 cursor-not-allowed' : ''}`}>
                                {saving ? 'Guardando...' : editing ? 'Actualizar' : 'Crear'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminMedicamentosView;
