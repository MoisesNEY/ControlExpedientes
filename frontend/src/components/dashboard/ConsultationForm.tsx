import { useState, useEffect, useCallback, useRef } from 'react';
import type { Diagnostico, Medicamento, Receta } from '../../services/consultation.service';
import { ConsultationService } from '../../services/consultation.service';
import { usePatient } from '../../context/PatientContext';

const ConsultationForm = () => {
    const { selectedPatient, selectPatient } = usePatient();

    // Form State
    const [motivo, setMotivo] = useState('');
    const [notas, setNotas] = useState('');
    const [diagnosticoSeleccionado, setDiagnosticoSeleccionado] = useState<Diagnostico | null>(null);
    const [prescripciones, setPrescripciones] = useState<Receta[]>([]);

    // Vitals State
    const [vitals, setVitals] = useState({
        peso: 75.2,
        altura: 1.75,
        presionArterial: '120/80',
        temperatura: 36.6,
        frecuenciaCardiaca: 72
    });

    // ICD-10 Search State
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState<Diagnostico[]>([]);
    const [isSearching, setIsSearching] = useState(false);

    // Medication Search State
    const [medQuery, setMedQuery] = useState('');
    const [medResults, setMedResults] = useState<Medicamento[]>([]);
    const [showMedResults, setShowMedResults] = useState(false);
    const [isSearchingMed, setIsSearchingMed] = useState(false);

    // Simple debounce utility
    const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const debouncedSearch = useCallback((q: string, type: 'diagnosis' | 'medication') => {
        if (timeoutRef.current) clearTimeout(timeoutRef.current);

        timeoutRef.current = setTimeout(async () => {
            if (q.length < 3) {
                if (type === 'diagnosis') setSearchResults([]);
                else setMedResults([]);
                return;
            }

            if (type === 'diagnosis') {
                setIsSearching(true);
                try {
                    const results = await ConsultationService.searchDiagnosticos(q);
                    setSearchResults(results);
                } finally {
                    setIsSearching(false);
                }
            } else {
                setIsSearchingMed(true);
                try {
                    const results = await ConsultationService.searchMedicamentos(q);
                    setMedResults(results);
                    setShowMedResults(true);
                } finally {
                    setIsSearchingMed(false);
                }
            }
        }, 400);
    }, []);

    useEffect(() => {
        if (searchQuery) debouncedSearch(searchQuery, 'diagnosis');
    }, [searchQuery, debouncedSearch]);

    useEffect(() => {
        if (medQuery) debouncedSearch(medQuery, 'medication');
        else setShowMedResults(false);
    }, [medQuery, debouncedSearch]);

    const handleAddMed = (med: Medicamento) => {
        setPrescripciones([...prescripciones, {
            medicamento: med,
            dosis: '1 tableta',
            frecuencia: 'Cada 8 horas',
            duracion: '5 días'
        }]);
        setMedQuery('');
        setShowMedResults(false);
    };

    const handleSave = async () => {
        if (!selectedPatient || !diagnosticoSeleccionado) {
            alert('Por favor seleccione un diagnóstico antes de finalizar la consulta.');
            return;
        }

        const patientId = selectedPatient.id.includes('-')
            ? selectedPatient.id.split('-')[1]
            : selectedPatient.id;

        const payload = {
            consulta: {
                motivoConsulta: motivo,
                notasMedicas: notas,
                fechaConsulta: new Date().toISOString().split('T')[0],
                expediente: { id: parseInt(patientId) || 1 }
            },
            signosVitales: vitals,
            diagnostico: { id: diagnosticoSeleccionado.id },
            recetas: prescripciones.map(p => ({
                dosis: p.dosis,
                frecuencia: p.frecuencia,
                duracion: p.duracion,
                medicamento: { id: p.medicamento.id }
            }))
        };

        try {
            await ConsultationService.saveConsultation(payload as any);
            alert('¡Atención Médica guardada con éxito!');
            selectPatient(null);
        } catch (error) {
            console.error('Error al guardar consulta:', error);
            alert('Error en el servidor al intentar guardar el acto clínico.');
        }
    };

    return (
        <div className="p-8 max-w-5xl mx-auto w-full flex flex-col gap-6 transition-colors duration-300 pb-32">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="bg-white dark:bg-slate-900 p-5 rounded-lg shadow-sm border border-slate-200 dark:border-slate-800">
                    <label className="flex flex-col w-full">
                        <p className="text-slate-800 dark:text-slate-200 text-sm font-bold mb-2 flex items-center gap-2">
                            <span className="material-symbols-outlined text-primary text-[18px]">emergency</span>
                            Motivo de Consulta
                        </p>
                        <textarea
                            value={motivo}
                            onChange={(e) => setMotivo(e.target.value)}
                            className="w-full min-h-[120px] rounded-lg text-slate-800 dark:text-slate-200 border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50 focus:ring-primary focus:border-primary transition-all p-3 text-sm outline-none placeholder:text-slate-400 dark:placeholder:text-slate-500"
                            placeholder="Ej: Dolor lumbar agudo tras esfuerzo físico..."
                        ></textarea>
                    </label>
                </div>
                <div className="bg-white dark:bg-slate-900 p-5 rounded-lg shadow-sm border border-slate-200 dark:border-slate-800">
                    <label className="flex flex-col w-full">
                        <p className="text-slate-800 dark:text-slate-200 text-sm font-bold mb-2 flex items-center gap-2">
                            <span className="material-symbols-outlined text-primary text-[18px]">medical_information</span>
                            Notas Clínicas
                        </p>
                        <textarea
                            value={notas}
                            onChange={(e) => setNotas(e.target.value)}
                            className="w-full min-h-[120px] rounded-lg text-slate-800 dark:text-slate-200 border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50 focus:ring-primary focus:border-primary transition-all p-3 text-sm outline-none placeholder:text-slate-400 dark:placeholder:text-slate-500"
                            placeholder="Observaciones clínicas, historial relevante..."
                        ></textarea>
                    </label>
                </div>
            </div>

            {/* Diagnóstico */}
            <div className="bg-white dark:bg-slate-900 p-5 rounded-lg shadow-sm border border-slate-200 dark:border-slate-800">
                <h3 className="text-slate-800 dark:text-slate-200 text-sm font-bold mb-4 flex items-center gap-2">
                    <span className="material-symbols-outlined text-primary text-[18px]">diagnosis</span>
                    Diagnóstico Principal (ICD-10)
                </h3>

                {diagnosticoSeleccionado ? (
                    <div className="flex items-center justify-between bg-primary/10 border border-primary/20 p-4 rounded-xl">
                        <div className="flex items-center gap-4">
                            <div className="bg-primary text-white text-xs font-black px-2 py-1 rounded-md">{diagnosticoSeleccionado.codigoIcd10}</div>
                            <span className="text-slate-800 dark:text-slate-200 font-bold">{diagnosticoSeleccionado.nombre}</span>
                        </div>
                        <button onClick={() => setDiagnosticoSeleccionado(null)} className="text-slate-400 hover:text-red-500 transition-colors">
                            <span className="material-symbols-outlined">cancel</span>
                        </button>
                    </div>
                ) : (
                    <div className="relative">
                        <span className={`absolute left-3 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400 ${isSearching ? 'animate-spin' : ''}`}>
                            {isSearching ? 'sync' : 'search'}
                        </span>
                        <input
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="w-full pl-10 pr-4 py-3 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl text-sm outline-none focus:ring-2 focus:ring-primary focus:border-primary transition-all"
                            placeholder="Buscar código o descripción ICD-10..."
                        />
                        {searchResults.length > 0 && (
                            <div className="absolute z-30 w-full mt-2 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl shadow-2xl overflow-hidden divide-y divide-slate-50 dark:divide-slate-800 max-h-60 overflow-y-auto custom-scrollbar">
                                {searchResults.map(d => (
                                    <div key={d.id} onClick={() => { setDiagnosticoSeleccionado(d); setSearchResults([]); setSearchQuery(''); }} className="p-4 hover:bg-primary/5 cursor-pointer flex gap-4 transition-colors group">
                                        <span className="font-black text-primary text-xs bg-primary/10 px-2 py-1 rounded self-start">{d.codigoIcd10}</span>
                                        <span className="text-sm font-semibold text-slate-700 dark:text-slate-300 group-hover:text-primary">{d.nombre}</span>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </div>

            {/* Prescripciones */}
            <div className="bg-white dark:bg-slate-900 p-5 rounded-lg shadow-sm border border-slate-200 dark:border-slate-800">
                <div className="flex justify-between items-center mb-4">
                    <h3 className="text-slate-800 dark:text-slate-200 text-sm font-bold flex items-center gap-2">
                        <span className="material-symbols-outlined text-primary text-[18px]">prescriptions</span>
                        Plan de Tratamiento y Recetas
                    </h3>
                    <div className="relative">
                        <input
                            value={medQuery}
                            onChange={(e) => setMedQuery(e.target.value)}
                            className="text-xs bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg px-3 py-1.5 outline-none focus:ring-1 focus:ring-primary w-48"
                            placeholder="Añadir medicamento..."
                        />
                        {showMedResults && medResults.length > 0 && (
                            <div className="absolute right-0 z-30 mt-1 w-64 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl shadow-2xl overflow-hidden divide-y divide-slate-50 dark:divide-slate-800 max-h-60 overflow-y-auto">
                                {medResults.map(m => (
                                    <div key={m.id} onClick={() => handleAddMed(m)} className="p-3 hover:bg-primary/5 cursor-pointer flex flex-col gap-1">
                                        <div className="text-xs font-bold text-slate-800 dark:text-slate-200">{m.nombre}</div>
                                        <div className="text-[10px] text-slate-400">Stock: <span className={m.stock < 10 ? 'text-red-500 font-bold' : 'text-success'}>{m.stock}</span></div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                <div className="overflow-x-auto rounded-xl border border-slate-100 dark:border-slate-800">
                    <table className="w-full text-sm text-left">
                        <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 uppercase text-[10px] font-black tracking-widest transition-colors">
                            <tr>
                                <th className="px-6 py-4">Medicamento</th>
                                <th className="px-6 py-4">Dosis</th>
                                <th className="px-6 py-4">Frecuencia</th>
                                <th className="px-6 py-4">Duración</th>
                                <th className="px-6 py-4 text-right">Acción</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                            {prescripciones.map((p, idx) => (
                                <tr key={idx} className="group hover:bg-slate-50/50 dark:hover:bg-slate-800/30">
                                    <td className="px-6 py-4 font-bold text-slate-900 dark:text-white transition-colors">{p.medicamento.nombre}</td>
                                    <td><input value={p.dosis} onChange={e => { const n = [...prescripciones]; n[idx].dosis = e.target.value; setPrescripciones(n); }} className="w-full px-6 py-4 bg-transparent outline-none focus:text-primary font-medium" /></td>
                                    <td><input value={p.frecuencia} onChange={e => { const n = [...prescripciones]; n[idx].frecuencia = e.target.value; setPrescripciones(n); }} className="w-full px-6 py-4 bg-transparent outline-none focus:text-primary font-medium" /></td>
                                    <td><input value={p.duracion} onChange={e => { const n = [...prescripciones]; n[idx].duracion = e.target.value; setPrescripciones(n); }} className="w-full px-6 py-4 bg-transparent outline-none focus:text-primary font-medium" /></td>
                                    <td className="px-6 py-4 text-right">
                                        <button onClick={() => setPrescripciones(prescripciones.filter((_, i) => i !== idx))} className="text-slate-300 hover:text-red-500 transition-colors">
                                            <span className="material-symbols-outlined text-[18px]">delete</span>
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            {prescripciones.length === 0 && (
                                <tr><td colSpan={5} className="px-6 py-10 text-center text-slate-400 italic font-medium">No se han registrado medicamentos para este tratamiento.</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Vitals inputs */}
            <div className="bg-white dark:bg-slate-900 p-5 rounded-lg shadow-sm border border-slate-200 dark:border-slate-800 transition-colors">
                <h3 className="text-slate-800 dark:text-slate-200 text-sm font-bold mb-4 flex items-center gap-2">
                    <span className="material-symbols-outlined text-primary text-[18px]">monitoring</span>
                    Signos Vitales Seleccionados
                </h3>
                <div className="grid grid-cols-2 md:grid-cols-5 gap-6">
                    {Object.entries(vitals).map(([key, value]) => (
                        <div key={key} className="flex flex-col gap-2">
                            <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest">
                                {key === 'presionArterial' ? 'Presión Art.' : key.charAt(0).toUpperCase() + key.slice(1)}
                            </span>
                            <input
                                type={typeof value === 'number' ? 'number' : 'text'}
                                step="0.1"
                                value={value}
                                onChange={(e) => setVitals({ ...vitals, [key]: typeof value === 'number' ? parseFloat(e.target.value) : e.target.value })}
                                className="bg-slate-50 dark:bg-slate-800 border border-slate-100 dark:border-slate-700/50 rounded-xl px-4 py-2 text-sm font-black text-slate-900 dark:text-white transition-all focus:ring-2 focus:ring-primary outline-none"
                            />
                        </div>
                    ))}
                </div>
            </div>

            <div className="fixed bottom-8 right-[352px] z-20">
                <button
                    onClick={handleSave}
                    className="flex items-center gap-3 px-10 py-4 bg-primary text-white rounded-full font-black shadow-2xl shadow-primary/40 hover:bg-primary/95 hover:scale-105 active:scale-95 transition-all text-xs uppercase tracking-widest"
                >
                    <span className="material-symbols-outlined text-[20px]">task_alt</span>
                    Finalizar Acto Clínico
                </button>
            </div>
        </div>
    );
};

export default ConsultationForm;
