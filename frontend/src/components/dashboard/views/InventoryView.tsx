import { useState, useEffect } from 'react';
import api from '../../../services/api';

interface Medicamento {
    id: number;
    nombre: string;
    descripcion: string;
    stock: number;
    precioVenta: number;
}

const InventoryView = () => {
    const [medications, setMedications] = useState<Medicamento[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        const fetchInventory = async () => {
            setLoading(true);
            try {
                const url = searchTerm
                    ? `/api/medicamentos?nombre.contains=${searchTerm}`
                    : '/api/medicamentos';
                const response = await api.get(url);
                setMedications(dateToSorted(response.data));
            } catch (error) {
                console.error('Error fetching inventory:', error);
            } finally {
                setLoading(false);
            }
        };

        const timeoutId = setTimeout(fetchInventory, 300);
        return () => clearTimeout(timeoutId);
    }, [searchTerm]);

    const dateToSorted = (data: Medicamento[]) => {
        return [...data].sort((a, b) => a.nombre.localeCompare(b.nombre));
    };

    return (
        <div className="p-8 space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-black text-slate-900 dark:text-white">Inventario Farmacéutico</h2>
                    <p className="text-slate-500 text-sm font-medium">Consulta de stock y disponibilidad de medicamentos.</p>
                </div>
                <div className="bg-primary/10 text-primary px-4 py-2 rounded-xl border border-primary/20 flex items-center gap-2">
                    <span className="material-symbols-outlined text-[18px]">info</span>
                    <span className="text-[10px] font-black uppercase tracking-widest">Modo Lectura - Médicos</span>
                </div>
            </div>

            <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                <div className="p-6 border-b border-slate-100 dark:border-slate-800">
                    <div className="relative w-full md:w-96">
                        <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400">search</span>
                        <input
                            type="text"
                            placeholder="Buscar medicamento..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-12 pr-4 py-3 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-sm outline-none focus:ring-2 focus:ring-primary transition-all"
                        />
                    </div>
                </div>

                <div className="overflow-x-auto">
                    <table className="w-full text-left">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest">
                            <tr>
                                <th className="px-6 py-4">Medicamento</th>
                                <th className="px-6 py-4">Descripción</th>
                                <th className="px-6 py-4">Disponibilidad</th>
                                <th className="px-6 py-4">Estado</th>
                                <th className="px-6 py-4 text-right">Precio Ref.</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                            {loading ? (
                                Array(5).fill(0).map((_, i) => (
                                    <tr key={i} className="animate-pulse">
                                        <td colSpan={5} className="px-6 py-6"><div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-full"></div></td>
                                    </tr>
                                ))
                            ) : (
                                medications.map(m => (
                                    <tr key={m.id} className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors">
                                        <td className="px-6 py-4">
                                            <span className="font-bold text-slate-800 dark:text-white">{m.nombre}</span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="text-xs text-slate-500 dark:text-slate-400 truncate max-w-xs block">{m.descripcion || 'Sin descripción'}</span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-2">
                                                <div className="w-16 h-1.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                                                    <div
                                                        className={`h-full rounded-full transition-all ${m.stock < 10 ? 'bg-red-500' : m.stock < 50 ? 'bg-orange-500' : 'bg-success'}`}
                                                        style={{ width: `${Math.min(100, (m.stock / 200) * 100)}%` }}
                                                    ></div>
                                                </div>
                                                <span className={`text-[10px] font-black ${m.stock < 10 ? 'text-red-500' : 'text-slate-500'}`}>{m.stock} uds.</span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className={`px-2 py-1 rounded-lg text-[10px] font-black uppercase ${m.stock > 10 ? 'bg-success/10 text-success' : 'bg-red-100 text-red-600'
                                                }`}>
                                                {m.stock > 10 ? 'En Existencia' : 'Stock Bajo'}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-right text-xs font-bold text-slate-600 dark:text-slate-400 italic">
                                            C$ {(m.precioVenta || 0).toFixed(2)}
                                        </td>
                                    </tr>
                                ))
                            )}
                            {!loading && medications.length === 0 && (
                                <tr>
                                    <td colSpan={5} className="px-6 py-20 text-center text-slate-400 italic font-medium">No se encontraron medicamentos.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default InventoryView;
