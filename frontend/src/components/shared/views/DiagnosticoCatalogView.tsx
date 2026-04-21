import { useEffect, useMemo, useState } from 'react';
import { DiagnosticoService, type Diagnostico } from '../../../services/diagnostico.service';
import { useAuth } from '../../../context/AuthContext';

const emptyDiagnosticoForm = {
    codigoCIE: '',
    descripcion: '',
};

const DiagnosticoCatalogView = () => {
    const { hasRole } = useAuth();
    const [diagnosticos, setDiagnosticos] = useState<Diagnostico[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [editing, setEditing] = useState<Diagnostico | null>(null);
    const [form, setForm] = useState(emptyDiagnosticoForm);
    const [saving, setSaving] = useState(false);

    const canDelete = hasRole('ROLE_ADMIN');

    const fetchDiagnosticos = async (query: string) => {
        setLoading(true);
        try {
            const data = query.trim().length >= 2
                ? await DiagnosticoService.search(query.trim())
                : await DiagnosticoService.getAll({ size: 100 });
            setDiagnosticos(data);
        } catch (error) {
            console.error('Error fetching diagnósticos:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const timeout = window.setTimeout(() => {
            void fetchDiagnosticos(searchTerm);
        }, 300);

        return () => window.clearTimeout(timeout);
    }, [searchTerm]);

    const sortedDiagnosticos = useMemo(
        () => [...diagnosticos].sort((a, b) => a.descripcion.localeCompare(b.descripcion)),
        [diagnosticos]
    );

    const openCreate = () => {
        setEditing(null);
        setForm(emptyDiagnosticoForm);
        setShowForm(true);
    };

    const openEdit = (diagnostico: Diagnostico) => {
        setEditing(diagnostico);
        setForm({
            codigoCIE: diagnostico.codigoCIE,
            descripcion: diagnostico.descripcion,
        });
        setShowForm(true);
    };

    const closeForm = () => {
        setShowForm(false);
        setEditing(null);
        setForm(emptyDiagnosticoForm);
    };

    const handleSave = async () => {
        if (!form.codigoCIE.trim() || !form.descripcion.trim()) {
            window.alert('Código y descripción son obligatorios.');
            return;
        }

        setSaving(true);
        try {
            if (editing?.id) {
                await DiagnosticoService.update(editing.id, {
                    codigoCIE: form.codigoCIE.trim(),
                    descripcion: form.descripcion.trim(),
                });
            } else {
                await DiagnosticoService.create(form.codigoCIE.trim(), form.descripcion.trim());
            }

            closeForm();
            await fetchDiagnosticos(searchTerm);
        } catch (error) {
            console.error('Error saving diagnóstico:', error);
            window.alert('No se pudo guardar el diagnóstico.');
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm('¿Eliminar este diagnóstico del catálogo?')) {
            return;
        }

        try {
            await DiagnosticoService.delete(id);
            await fetchDiagnosticos(searchTerm);
        } catch (error) {
            console.error('Error deleting diagnóstico:', error);
            window.alert('No se pudo eliminar el diagnóstico.');
        }
    };

    return (
        <div className="p-4 md:p-8 space-y-4 md:space-y-6">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white">Gestión de Diagnósticos</h2>
                    <p className="text-slate-500 text-xs md:text-sm font-medium">
                        Catálogo compartido de diagnósticos para médico y administración.
                    </p>
                </div>
                <button
                    onClick={openCreate}
                    className="w-full md:w-auto flex items-center justify-center gap-2 px-6 py-3 bg-rose-600 text-white rounded-xl font-bold shadow-lg shadow-rose-600/30 hover:scale-105 transition-transform"
                >
                    <span className="material-symbols-outlined">add_circle</span>
                    Nuevo Diagnóstico
                </button>
            </div>

            <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                <div className="p-4 md:p-6 border-b border-slate-100 dark:border-slate-800">
                    <div className="relative w-full md:w-96">
                        <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400">search</span>
                        <input
                            type="text"
                            placeholder="Buscar por código o descripción..."
                            value={searchTerm}
                            onChange={event => setSearchTerm(event.target.value)}
                            className="w-full pl-12 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-xs outline-none focus:ring-2 focus:ring-rose-600 transition-all"
                        />
                    </div>
                </div>

                <div className="overflow-x-auto scrollbar-hide">
                    <table className="w-full text-left min-w-[640px]">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest">
                            <tr>
                                <th className="px-6 py-4">Código CIE-10</th>
                                <th className="px-6 py-4">Descripción</th>
                                <th className="px-6 py-4 text-right">Acciones</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                            {loading ? (
                                Array.from({ length: 5 }, (_, index) => (
                                    <tr key={index} className="animate-pulse">
                                        <td colSpan={3} className="px-6 py-6">
                                            <div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-full"></div>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                sortedDiagnosticos.map(diagnostico => (
                                    <tr key={diagnostico.id} className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors">
                                        <td className="px-6 py-4">
                                            <span className="inline-flex px-2.5 py-1 rounded-lg text-[10px] font-black uppercase bg-rose-100 dark:bg-rose-900/30 text-rose-600">
                                                {diagnostico.codigoCIE}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-sm font-semibold text-slate-800 dark:text-white">
                                            {diagnostico.descripcion}
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <div className="flex items-center justify-end gap-1">
                                                <button
                                                    onClick={() => openEdit(diagnostico)}
                                                    className="p-2 text-slate-400 hover:text-rose-600 transition-colors"
                                                >
                                                    <span className="material-symbols-outlined text-sm">edit</span>
                                                </button>
                                                {canDelete && (
                                                    <button
                                                        onClick={() => handleDelete(diagnostico.id)}
                                                        className="p-2 text-slate-400 hover:text-red-500 transition-colors"
                                                    >
                                                        <span className="material-symbols-outlined text-sm">delete</span>
                                                    </button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}

                            {!loading && sortedDiagnosticos.length === 0 && (
                                <tr>
                                    <td colSpan={3} className="px-6 py-20 text-center text-slate-400 italic font-medium">
                                        No se encontraron diagnósticos en el catálogo.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {showForm && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={closeForm} />
                    <div className="relative bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 w-full max-w-lg">
                        <div className="p-6 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center">
                            <h3 className="text-lg font-black text-slate-900 dark:text-white">
                                {editing ? 'Editar Diagnóstico' : 'Nuevo Diagnóstico'}
                            </h3>
                            <button onClick={closeForm} className="text-slate-400 hover:text-slate-600">
                                <span className="material-symbols-outlined">close</span>
                            </button>
                        </div>
                        <div className="p-6 space-y-4">
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Código CIE-10 *</label>
                                <input
                                    value={form.codigoCIE}
                                    onChange={event => setForm(current => ({ ...current, codigoCIE: event.target.value }))}
                                    placeholder="Ej: J06"
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-rose-600 text-slate-900 dark:text-white"
                                />
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Descripción *</label>
                                <textarea
                                    value={form.descripcion}
                                    onChange={event => setForm(current => ({ ...current, descripcion: event.target.value }))}
                                    placeholder="Ej: Faringitis aguda"
                                    className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-rose-600 min-h-[100px] text-slate-900 dark:text-white"
                                />
                            </div>
                        </div>
                        <div className="p-6 border-t border-slate-100 dark:border-slate-800 flex justify-end gap-3">
                            <button
                                onClick={closeForm}
                                className="px-6 py-2.5 text-sm font-bold text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors"
                            >
                                Cancelar
                            </button>
                            <button
                                onClick={handleSave}
                                disabled={saving}
                                className={`px-6 py-2.5 text-sm font-bold text-white bg-rose-600 rounded-xl shadow-lg shadow-rose-600/30 hover:scale-105 transition-all ${saving ? 'opacity-70 cursor-not-allowed' : ''}`}
                            >
                                {saving ? 'Guardando...' : editing ? 'Actualizar' : 'Crear'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default DiagnosticoCatalogView;
