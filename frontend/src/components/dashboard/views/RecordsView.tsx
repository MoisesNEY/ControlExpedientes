import { useState, useEffect } from 'react';
import api from '../../../services/api';

interface Consulta {
    id: number;
    fechaConsulta: string;
    motivoConsulta: string;
    notasMedicas: string;
    expediente: {
        paciente: {
            nombre: string;
        };
    };
}

const RecordsView = () => {
    const [records, setRecords] = useState<Consulta[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        const fetchRecords = async () => {
            setLoading(true);
            try {
                // Using criteria search if supported, otherwise getting all and filtering
                // The backend ConsultaMedicaResource supports criteria
                const url = searchTerm
                    ? `/api/consulta-medicas?motivoConsulta.contains=${searchTerm}`
                    : '/api/consulta-medicas';
                const response = await api.get(url);
                setRecords(response.data);
            } catch (error) {
                console.error('Error fetching records:', error);
            } finally {
                setLoading(false);
            }
        };

        const timeoutId = setTimeout(fetchRecords, 300);
        return () => clearTimeout(timeoutId);
    }, [searchTerm]);

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('es-ES', {
            day: '2-digit',
            month: 'long',
            year: 'numeric'
        });
    };

    return (
        <div className="p-4 md:p-8 space-y-4 md:space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white transition-colors">Historial de Consultas</h2>
                    <p className="text-slate-500 text-xs md:text-sm font-medium">Registros históricos de todas las atenciones médicas realizadas.</p>
                </div>
            </div>

            <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden transition-colors">
                <div className="p-6 border-b border-slate-100 dark:border-slate-800">
                    <div className="relative w-full md:w-96">
                        <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400">search</span>
                        <input
                            type="text"
                            placeholder="Buscar por motivo de consulta..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-12 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-xs outline-none focus:ring-2 focus:ring-primary transition-all"
                        />
                    </div>
                </div>

                <div className="overflow-x-auto scrollbar-hide">
                    <table className="w-full text-left min-w-[900px]">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest transition-colors">
                            <tr>
                                <th className="px-6 py-4">Fecha</th>
                                <th className="px-6 py-4">Paciente</th>
                                <th className="px-6 py-4">Motivo Principal</th>
                                <th className="px-6 py-4">Notas Clínicas</th>
                                <th className="px-6 py-4 text-right">Acciones</th>
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
                                records.map(r => (
                                    <tr key={r.id} className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors">
                                        <td className="px-6 py-4">
                                            <span className="text-sm font-bold text-slate-900 dark:text-white">{formatDate(r.fechaConsulta)}</span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-2">
                                                <div className="w-8 h-8 rounded-lg bg-primary/10 text-primary flex items-center justify-center">
                                                    <span className="material-symbols-outlined text-sm">person</span>
                                                </div>
                                                <span className="text-sm font-semibold text-slate-700 dark:text-slate-300 italic">{r.expediente?.paciente?.nombre || 'Paciente N/A'}</span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="text-xs font-medium text-slate-600 dark:text-slate-400 block max-w-xs truncate">{r.motivoConsulta}</span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="text-[10px] text-slate-400 dark:text-slate-500 italic block max-w-xs truncate">{r.notasMedicas || 'Sin notas'}</span>
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <button className="p-2 text-slate-400 hover:text-primary transition-colors">
                                                <span className="material-symbols-outlined">description</span>
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                            {!loading && records.length === 0 && (
                                <tr>
                                    <td colSpan={5} className="px-6 py-20 text-center text-slate-400 italic font-medium">No hay registros de consultas disponibles.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default RecordsView;
