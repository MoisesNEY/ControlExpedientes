import { useEffect, useMemo, useState } from 'react';
import { InteraccionService, type InteraccionMedicamentosaDTO, type InteraccionMedicamentosaInput } from '../../../services/interaccion.service';
import { MedicamentoService, type MedicamentoDTO } from '../../../services/medicamento.service';

type Severidad = InteraccionMedicamentosaInput['severidad'];

const emptyInteractionForm = {
    medicamentoAId: '',
    medicamentoBId: '',
    severidad: 'MODERADA' as Severidad,
    descripcion: '',
    recomendacion: '',
};

const AdminInteraccionesView = () => {
    const [interacciones, setInteracciones] = useState<InteraccionMedicamentosaDTO[]>([]);
    const [medicamentos, setMedicamentos] = useState<MedicamentoDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [form, setForm] = useState(emptyInteractionForm);

    const loadData = async () => {
        setLoading(true);
        try {
            const [interaccionesData, medicamentosData] = await Promise.all([
                InteraccionService.getAll({ size: 200 }),
                MedicamentoService.getAll({ size: 200, sort: 'nombre,asc' }),
            ]);
            setInteracciones(interaccionesData);
            setMedicamentos(medicamentosData);
        } catch (error) {
            console.error('Error loading interaction catalog:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadData();
    }, []);

    const medicamentosOrdenados = useMemo(
        () => [...medicamentos].sort((a, b) => a.nombre.localeCompare(b.nombre)),
        [medicamentos]
    );

    const interaccionesFiltradas = useMemo(() => {
        const query = searchTerm.trim().toLowerCase();
        if (!query) {
            return interacciones;
        }

        return interacciones.filter(interaccion =>
            interaccion.medicamentoA.nombre.toLowerCase().includes(query) ||
            interaccion.medicamentoB.nombre.toLowerCase().includes(query) ||
            interaccion.descripcion.toLowerCase().includes(query)
        );
    }, [interacciones, searchTerm]);

    const resetForm = () => setForm(emptyInteractionForm);

    const handleSave = async () => {
        if (!form.medicamentoAId || !form.medicamentoBId || !form.descripcion.trim()) {
            window.alert('Seleccione ambos medicamentos y complete la descripción.');
            return;
        }

        setSaving(true);
        try {
            const payload: InteraccionMedicamentosaInput = {
                medicamentoA: { id: Number(form.medicamentoAId) },
                medicamentoB: { id: Number(form.medicamentoBId) },
                severidad: form.severidad,
                descripcion: form.descripcion.trim(),
                recomendacion: form.recomendacion.trim() || undefined,
            };

            await InteraccionService.create(payload);
            resetForm();
            await loadData();
        } catch (error) {
            console.error('Error saving interaction:', error);
            window.alert('No se pudo guardar la interacción. Revise que la pareja no exista ya.');
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm('¿Eliminar esta interacción medicamentosa?')) {
            return;
        }

        try {
            await InteraccionService.delete(id);
            await loadData();
        } catch (error) {
            console.error('Error deleting interaction:', error);
            window.alert('No se pudo eliminar la interacción.');
        }
    };

    return (
        <div className="p-4 md:p-8 space-y-4 md:space-y-6">
            <div>
                <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white">Interacciones Medicamentosas</h2>
                <p className="text-slate-500 text-xs md:text-sm font-medium">
                    Administración centralizada de alertas farmacológicas para el módulo médico.
                </p>
            </div>

            <div className="grid grid-cols-1 xl:grid-cols-[380px,1fr] gap-6">
                <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm">
                    <div className="p-6 border-b border-slate-100 dark:border-slate-800">
                        <h3 className="text-lg font-black text-slate-900 dark:text-white">Nueva interacción</h3>
                        <p className="text-sm text-slate-500 mt-1">Solo el administrador puede mantener este catálogo.</p>
                    </div>
                    <div className="p-6 space-y-4">
                        <div className="flex flex-col gap-1.5">
                            <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Medicamento A *</label>
                            <select
                                value={form.medicamentoAId}
                                onChange={event => setForm(current => ({ ...current, medicamentoAId: event.target.value }))}
                                className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600 text-slate-900 dark:text-white"
                            >
                                <option value="">Seleccione un medicamento</option>
                                {medicamentosOrdenados.map(medicamento => (
                                    <option key={`a-${medicamento.id}`} value={medicamento.id}>
                                        {medicamento.nombre}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="flex flex-col gap-1.5">
                            <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Medicamento B *</label>
                            <select
                                value={form.medicamentoBId}
                                onChange={event => setForm(current => ({ ...current, medicamentoBId: event.target.value }))}
                                className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600 text-slate-900 dark:text-white"
                            >
                                <option value="">Seleccione un medicamento</option>
                                {medicamentosOrdenados.map(medicamento => (
                                    <option key={`b-${medicamento.id}`} value={medicamento.id}>
                                        {medicamento.nombre}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="flex flex-col gap-1.5">
                            <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Severidad *</label>
                            <select
                                value={form.severidad}
                                onChange={event => setForm(current => ({ ...current, severidad: event.target.value as Severidad }))}
                                className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600 text-slate-900 dark:text-white"
                            >
                                <option value="LEVE">Leve</option>
                                <option value="MODERADA">Moderada</option>
                                <option value="GRAVE">Grave</option>
                            </select>
                        </div>

                        <div className="flex flex-col gap-1.5">
                            <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Descripción *</label>
                            <textarea
                                value={form.descripcion}
                                onChange={event => setForm(current => ({ ...current, descripcion: event.target.value }))}
                                className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600 min-h-[110px] text-slate-900 dark:text-white"
                                placeholder="Describe el riesgo clínico de esta combinación."
                            />
                        </div>

                        <div className="flex flex-col gap-1.5">
                            <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Recomendación</label>
                            <textarea
                                value={form.recomendacion}
                                onChange={event => setForm(current => ({ ...current, recomendacion: event.target.value }))}
                                className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-amber-600 min-h-[90px] text-slate-900 dark:text-white"
                                placeholder="Monitoreo, alternativa terapéutica o ajuste sugerido."
                            />
                        </div>

                        <button
                            onClick={handleSave}
                            disabled={saving}
                            className={`w-full px-6 py-3 text-sm font-bold text-white bg-amber-600 rounded-xl shadow-lg shadow-amber-600/30 hover:scale-105 transition-all ${saving ? 'opacity-70 cursor-not-allowed' : ''}`}
                        >
                            {saving ? 'Guardando...' : 'Guardar interacción'}
                        </button>
                    </div>
                </div>

                <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                    <div className="p-4 md:p-6 border-b border-slate-100 dark:border-slate-800">
                        <div className="relative w-full md:w-96">
                            <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400">search</span>
                            <input
                                type="text"
                                placeholder="Buscar por medicamento o descripción..."
                                value={searchTerm}
                                onChange={event => setSearchTerm(event.target.value)}
                                className="w-full pl-12 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-xs outline-none focus:ring-2 focus:ring-amber-600 transition-all"
                            />
                        </div>
                    </div>

                    <div className="overflow-x-auto scrollbar-hide">
                        <table className="w-full text-left min-w-[760px]">
                            <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest">
                                <tr>
                                    <th className="px-6 py-4">Pareja</th>
                                    <th className="px-6 py-4">Severidad</th>
                                    <th className="px-6 py-4">Descripción</th>
                                    <th className="px-6 py-4">Recomendación</th>
                                    <th className="px-6 py-4 text-right">Acciones</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                                {loading ? (
                                    Array.from({ length: 5 }, (_, index) => (
                                        <tr key={index} className="animate-pulse">
                                            <td colSpan={5} className="px-6 py-6">
                                                <div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-full"></div>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    interaccionesFiltradas.map(interaccion => (
                                        <tr key={interaccion.id} className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors">
                                            <td className="px-6 py-4">
                                                <div className="flex flex-col gap-1">
                                                    <span className="font-bold text-slate-800 dark:text-white">{interaccion.medicamentoA.nombre}</span>
                                                    <span className="text-xs text-slate-400">con {interaccion.medicamentoB.nombre}</span>
                                                </div>
                                            </td>
                                            <td className="px-6 py-4">
                                                <span
                                                    className={`px-2 py-1 rounded-lg text-[10px] font-black uppercase ${
                                                        interaccion.severidad === 'GRAVE'
                                                            ? 'bg-red-100 dark:bg-red-900/30 text-red-600'
                                                            : interaccion.severidad === 'MODERADA'
                                                                ? 'bg-amber-100 dark:bg-amber-900/30 text-amber-600'
                                                                : 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600'
                                                    }`}
                                                >
                                                    {interaccion.severidad}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 text-xs text-slate-500 dark:text-slate-300 max-w-sm">{interaccion.descripcion}</td>
                                            <td className="px-6 py-4 text-xs text-slate-500 dark:text-slate-300 max-w-sm">
                                                {interaccion.recomendacion || 'Sin recomendación registrada.'}
                                            </td>
                                            <td className="px-6 py-4 text-right">
                                                <button
                                                    onClick={() => handleDelete(interaccion.id)}
                                                    className="p-2 text-slate-400 hover:text-red-500 transition-colors"
                                                >
                                                    <span className="material-symbols-outlined text-sm">delete</span>
                                                </button>
                                            </td>
                                        </tr>
                                    ))
                                )}

                                {!loading && interaccionesFiltradas.length === 0 && (
                                    <tr>
                                        <td colSpan={5} className="px-6 py-20 text-center text-slate-400 italic font-medium">
                                            No se encontraron interacciones registradas.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminInteraccionesView;
