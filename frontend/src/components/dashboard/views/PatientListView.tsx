import { useState, useEffect } from 'react';
import api from '../../../services/api';
import { usePatient } from '../../../context/PatientContext';

import { PacienteService, type PacienteDTO } from '../../../services/paciente.service';

const PatientListView = () => {
    const { selectPatient } = usePatient();
    const [pacientes, setPacientes] = useState<PacienteDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        const fetchPatients = async () => {
            setLoading(true);
            try {
                // Using JHipster criteria for name filtering
                const params = searchTerm ? { 'nombres.contains': searchTerm } : {};
                const response = await PacienteService.getAll(params);
                setPacientes(response);
            } catch (error) {
                console.error('Error fetching patients:', error);
            } finally {
                setLoading(false);
            }
        };

        const timeoutId = setTimeout(fetchPatients, 300);
        return () => clearTimeout(timeoutId);
    }, [searchTerm]);

    const handleAttend = (p: PacienteDTO) => {
        selectPatient({
            id: `PX-${p.id}`,
            name: `${p.nombres} ${p.apellidos}`,
            age: calculateAge(p.fechaNacimiento),
            gender: p.sexo === 'MASCULINO' ? 'Masculino' : p.sexo === 'FEMENINO' ? 'Femenino' : 'Otro',
            status: p.activo ? 'Activo' : 'Inactivo',
            image: `https://i.pravatar.cc/150?u=${p.id}`
        });
    };

    const calculateAge = (birthday: string) => {
        if (!birthday) return 'N/A';
        const ageDifMs = Date.now() - new Date(birthday).getTime();
        const ageDate = new Date(ageDifMs);
        return Math.abs(ageDate.getUTCFullYear() - 1970) + ' años';
    };

    return (
        <div className="p-4 md:p-8 space-y-4 md:space-y-6">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white transition-colors">Base de Datos de Pacientes</h2>
                    <p className="text-slate-500 text-xs md:text-sm font-medium">Búsqueda global y gestión de expedientes.</p>
                </div>
                <button className="w-full md:w-auto flex items-center justify-center gap-2 px-6 py-3 bg-primary text-white rounded-xl font-bold shadow-lg shadow-primary/30 hover:scale-105 transition-transform">
                    <span className="material-symbols-outlined">person_add</span>
                    Nuevo Paciente
                </button>
            </div>

            <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden transition-colors">
                <div className="p-4 md:p-6 border-b border-slate-100 dark:border-slate-800 flex flex-col md:flex-row gap-4 items-center justify-between">
                    <div className="relative w-full md:w-96">
                        <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400">search</span>
                        <input
                            type="text"
                            placeholder="Buscar por nombre o identificación..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-12 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-xs outline-none focus:ring-2 focus:ring-primary transition-all"
                        />
                    </div>
                    <div className="flex gap-2 w-full md:w-auto justify-end">
                        <button className="p-3 bg-slate-100 dark:bg-slate-800 rounded-xl text-slate-500 hover:text-primary transition-colors">
                            <span className="material-symbols-outlined">filter_list</span>
                        </button>
                        <button className="p-3 bg-slate-100 dark:bg-slate-800 rounded-xl text-slate-500 hover:text-primary transition-colors">
                            <span className="material-symbols-outlined">download</span>
                        </button>
                    </div>
                </div>

                <div className="overflow-x-auto scrollbar-hide">
                    <table className="w-full text-left min-w-[800px]">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 text-[10px] font-black uppercase tracking-widest">
                            <tr>
                                <th className="px-6 py-4">Paciente</th>
                                <th className="px-6 py-4">Identificación</th>
                                <th className="px-6 py-4">Sexo</th>
                                <th className="px-6 py-4">Contacto</th>
                                <th className="px-6 py-4 text-right">Acciones</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                            {loading ? (
                                Array(5).fill(0).map((_, i) => (
                                    <tr key={i} className="animate-pulse">
                                        <td className="px-6 py-6"><div className="h-4 bg-slate-200 dark:bg-slate-700 rounded w-48"></div></td>
                                        <td className="px-6 py-6"><div className="h-4 bg-slate-200 dark:bg-slate-700 rounded w-24"></div></td>
                                        <td className="px-6 py-6"><div className="h-4 bg-slate-200 dark:bg-slate-700 rounded w-16"></div></td>
                                        <td className="px-6 py-6"><div className="h-4 bg-slate-200 dark:bg-slate-700 rounded w-32"></div></td>
                                        <td className="px-6 py-6 text-right"><div className="h-8 bg-slate-200 dark:bg-slate-700 rounded-lg w-20 ml-auto"></div></td>
                                    </tr>
                                ))
                            ) : (
                                pacientes.map(p => (
                                    <tr key={p.id} className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors">
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="w-10 h-10 bg-slate-200 dark:bg-slate-700 rounded-xl overflow-hidden shadow-sm">
                                                    <img src={`https://i.pravatar.cc/150?u=${p.id}`} alt="" />
                                                </div>
                                                <div>
                                                    <p className="font-bold text-slate-800 dark:text-white leading-tight">{p.nombres} {p.apellidos}</p>
                                                    <p className="text-[10px] text-slate-400 font-bold uppercase tracking-tighter">Expediente: XP-{2024}-{p.id}</p>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="text-sm font-medium text-slate-600 dark:text-slate-400">{p.cedula || 'N/A'}</span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className={`px-2 py-1 rounded-lg text-[10px] font-black uppercase ${p.sexo === 'MASCULINO' ? 'bg-blue-100 text-blue-600' : p.sexo === 'FEMENINO' ? 'bg-pink-100 text-pink-600' : 'bg-purple-100 text-purple-600'}`}>
                                                {p.sexo}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex flex-col">
                                                <span className="text-sm font-medium text-slate-700 dark:text-slate-300">{p.telefono || 'Sin teléfono'}</span>
                                                <span className="text-[11px] text-slate-400">{p.email || 'Sin email'}</span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <button
                                                onClick={() => handleAttend(p)}
                                                className="px-4 py-2 bg-primary/10 text-primary rounded-xl text-xs font-black uppercase hover:bg-primary hover:text-white transition-all"
                                            >
                                                Ver Expediente
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                            {!loading && pacientes.length === 0 && (
                                <tr>
                                    <td colSpan={5} className="px-6 py-20 text-center text-slate-400 italic font-medium">No se encontraron pacientes en la base de datos.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default PatientListView;
