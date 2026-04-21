import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AppointmentService, type Appointment } from '../../../services/appointment.service';
import { DiagnosticoService, type Diagnostico as DiagnosticoCatalogo } from '../../../services/diagnostico.service';
import { MedicamentoService, type MedicamentoDTO } from '../../../services/medicamento.service';
import api from '../../../services/api';
import { useAuth } from '../../../context/AuthContext';
import PrintableReceta from './PrintableReceta';
import { AppButton } from '../../ui/AppButton';
import { PatientCard } from '../../ui/PatientCard';
import { InteraccionService, type InteraccionMedicamentosaDTO } from '../../../services/interaccion.service';
import { ReporteService } from '../../../services/reporte.service';
import DrugInteractionAlert from '../DrugInteractionAlert';

interface Prescription {
    medicamento: MedicamentoDTO;
    dosis: string;
    frecuencia: string;
    duracion: string;
}

const DoctorConsultationView = () => {
    const { citaId } = useParams<{ citaId: string }>();
    const navigate = useNavigate();
    const { account, hasAnyRole } = useAuth();
    const doctorName = account ? `Dr. ${account.firstName} ${account.lastName}`.trim() : 'Médico Tratante';
    const [appointment, setAppointment] = useState<Appointment | null>(null);
    const [loading, setLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [saveError, setSaveError] = useState<string | null>(null);

    // Notas médicas
    const [notasMedicas, setNotasMedicas] = useState('');

    // Diagnóstico Autocomplete
    const [diagQuery, setDiagQuery] = useState('');
    const [diagResults, setDiagResults] = useState<DiagnosticoCatalogo[]>([]);
    const [selectedDiagnosis, setSelectedDiagnosis] = useState<DiagnosticoCatalogo | null>(null);
    const [isDiagSearching, setIsDiagSearching] = useState(false);
    const diagDropdownRef = useRef<HTMLDivElement>(null);

    // Create diagnosis modal state (permitir crear desde la vista de consulta)
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [newDiagCodigo, setNewDiagCodigo] = useState('');
    const [newDiagDescripcion, setNewDiagDescripcion] = useState('');
    const [creatingDiag, setCreatingDiag] = useState(false);
    const [createDiagError, setCreateDiagError] = useState<string | null>(null);

    // Medicamento Autocomplete & Recetas
    const [medQuery, setMedQuery] = useState('');
    const [medResults, setMedResults] = useState<MedicamentoDTO[]>([]);
    const [selectedMed, setSelectedMed] = useState<MedicamentoDTO | null>(null);
    const [isMedSearching, setIsMedSearching] = useState(false);
    const medDropdownRef = useRef<HTMLDivElement>(null);
    const [dosisValue, setDosisValue] = useState('');
    const [frecuenciaValue, setFrecuenciaValue] = useState('');
    const [duracionValue, setDuracionValue] = useState('');
    const [prescriptions, setPrescriptions] = useState<Prescription[]>([]);
    const [interacciones, setInteracciones] = useState<InteraccionMedicamentosaDTO[]>([]);
    const [signosVitales, setSignosVitales] = useState<Record<string, string | number> | null>(null);

    // Cargar datos de la cita y signos vitales
    useEffect(() => {
        if (!citaId) return;
        setLoading(true);
        AppointmentService.getById(parseInt(citaId))
            .then(async (citaData) => {
                setAppointment(citaData);
                try { localStorage.setItem('activeConsultation', String(citaData.id)); } catch {}

                // Si la cita apenas se abre, cambiar a EN_CONSULTA
                if (citaData.estado === 'ESPERANDO_MEDICO') {
                    try {
                        import('../../../services/cita.service').then(m => {
                            m.CitaService.patch(citaData.id, { estado: 'EN_CONSULTA' });
                        });
                    } catch (e) {
                        console.error("No se pudo actualizar estado a EN_CONSULTA", e);
                    }
                }

                // Obtener signos vitales del paciente de hoy
                if (citaData.paciente?.id) {
                    try {
                        const { SignosVitalesService } = await import('../../../services/signosVitales.service');
                        const vitals = await SignosVitalesService.getTodayByPacienteId(citaData.paciente.id);
                        if (vitals && vitals.length > 0) {
                            setSignosVitales(vitals[0]);
                        }
                    } catch (e) {
                        console.error('Error cargando signos vitales:', e);
                    }
                }
            })
            .catch((err) => console.error('Error cargando cita:', err))
            .finally(() => setLoading(false));
    }, [citaId]);

    // Búsqueda de diagnósticos con debounce
    useEffect(() => {
        const timer = setTimeout(async () => {
            if (diagQuery.length >= 2) {
                setIsDiagSearching(true);
                try {
                    const results = await DiagnosticoService.search(diagQuery);
                    setDiagResults(results);
                } catch (e) {
                    console.error('Error buscando diagnóstico:', e);
                } finally {
                    setIsDiagSearching(false);
                }
            } else {
                setDiagResults([]);
            }
        }, 300);
        return () => clearTimeout(timer);
    }, [diagQuery]);

    const handleCreateDiagnostico = async () => {
        if (!newDiagCodigo.trim() || !newDiagDescripcion.trim()) {
            setCreateDiagError('Código y descripción son obligatorios.');
            return;
        }
        setCreatingDiag(true);
        setCreateDiagError(null);
        try {
            const created = await DiagnosticoService.create(newDiagCodigo.trim(), newDiagDescripcion.trim());
            setSelectedDiagnosis(created);
            setShowCreateModal(false);
            setDiagResults([]);
            setDiagQuery('');
        } catch (err: unknown) {
            console.error('Error creando diagnóstico:', err);
            const message = typeof err === 'object' && err !== null && 'response' in err
                ? ((err as { response?: { data?: { message?: string } } }).response?.data?.message ?? 'Error creando diagnóstico.')
                : 'Error creando diagnóstico.';
            setCreateDiagError(message);
        } finally {
            setCreatingDiag(false);
        }
    };

    // Búsqueda de medicamentos con debounce
    useEffect(() => {
        const timer = setTimeout(async () => {
            if (medQuery.length >= 2) {
                setIsMedSearching(true);
                try {
                    const results = await MedicamentoService.search(medQuery);
                    setMedResults(results);
                } catch (e) {
                    console.error('Error buscando medicamento:', e);
                } finally {
                    setIsMedSearching(false);
                }
            } else {
                setMedResults([]);
            }
        }, 300);
        return () => clearTimeout(timer);
    }, [medQuery]);

    // Cerrar dropdowns al hacer clic fuera
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (diagDropdownRef.current && !diagDropdownRef.current.contains(event.target as Node)) {
                setDiagResults([]);
            }
            if (medDropdownRef.current && !medDropdownRef.current.contains(event.target as Node)) {
                setMedResults([]);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    // Verificar interacciones medicamentosas cuando cambian las prescripciones
    useEffect(() => {
        const ids = prescriptions.map((rx) => rx.medicamento.id).filter((id): id is number => id != null);
        if (ids.length < 2) {
            setInteracciones([]);
            return;
        }
        InteraccionService.verificarInteracciones(ids)
            .then(setInteracciones)
            .catch((e) => console.error('Error verificando interacciones:', e));
    }, [prescriptions]);

    const handleAddPrescription = () => {
        if (!selectedMed || !dosisValue || !frecuenciaValue || !duracionValue) return;
        setPrescriptions([...prescriptions, {
            medicamento: selectedMed,
            dosis: dosisValue,
            frecuencia: frecuenciaValue,
            duracion: duracionValue,
        }]);
        setSelectedMed(null);
        setMedQuery('');
        setDosisValue('');
        setFrecuenciaValue('');
        setDuracionValue('');
    };

    const handleRemovePrescription = (index: number) => {
        setPrescriptions(prescriptions.filter((_, i) => i !== index));
    };

    const handleFinalizarConsulta = async () => {
        if (!citaId || !selectedDiagnosis) return;
        setSaveError(null);
        setIsSaving(true);
        try {
            await api.post(`/api/cita-medicas/${citaId}/finalizar`, {
                motivoConsulta: appointment?.observaciones || 'Consulta médica',
                notasMedicas,
                diagnosticoPrincipalId: selectedDiagnosis.id,
                recetas: prescriptions.map((rx) => ({
                    medicamentoId: rx.medicamento.id,
                    dosis: rx.dosis,
                    frecuencia: rx.frecuencia,
                    duracion: rx.duracion,
                    cantidad: 1,
                })),
            });
            try { localStorage.removeItem('activeConsultation'); } catch {}
            navigate('/medico');
        } catch (err: unknown) {
            console.error('Error al finalizar consulta:', err);
            const message = typeof err === 'object' && err !== null && 'response' in err
                ? ((err as { response?: { data?: { title?: string } } }).response?.data?.title ?? 'Error al guardar. Intente de nuevo.')
                : 'Error al guardar. Intente de nuevo.';
            setSaveError(message);
        } finally {
            setIsSaving(false);
        }
    };

    const [isDownloadingPdf, setIsDownloadingPdf] = useState(false);

    const handleDownloadPdf = async () => {
        if (!citaId) return;
        setIsDownloadingPdf(true);
        setSaveError(null);
        try {
            if (!appointment?.paciente || !selectedDiagnosis) {
                setSaveError('Seleccione un diagnóstico y verifique los datos del paciente antes de descargar la receta.');
                return;
            }
            await ReporteService.descargarRecetaPreviewPdf({
                citaId: Number(citaId),
                fechaConsulta: new Date().toISOString().slice(0, 10),
                nombrePaciente: `${appointment.paciente.nombres ?? ''} ${appointment.paciente.apellidos ?? ''}`.trim(),
                codigoPaciente: `PAC-${String(appointment.paciente.id).padStart(4, '0')}`,
                motivoConsulta: appointment.observaciones || 'Consulta médica',
                codigoDiagnostico: selectedDiagnosis.codigoCIE,
                descripcionDiagnostico: selectedDiagnosis.descripcion,
                notasMedicas,
                doctorName,
                recetas: prescriptions.map((rx) => ({
                    medicamento: rx.medicamento.nombre,
                    dosis: rx.dosis,
                    frecuencia: rx.frecuencia,
                    duracion: rx.duracion,
                })),
            });
        } catch (err: unknown) {
            console.error('Error al descargar PDF:', err);
            setSaveError('Error al generar el PDF de la receta.');
        } finally {
            setIsDownloadingPdf(false);
        }
    };

    const handlePrint = () => {
        if (!selectedDiagnosis) {
            setSaveError('Seleccione un diagnóstico antes de imprimir la receta.');
            return;
        }
        window.print();
    };

    if (loading || !appointment) {
        return (
            <div className="p-8 flex items-center justify-center h-[calc(100vh-100px)]">
                <div className="flex flex-col items-center gap-4">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                    <p className="text-slate-500 font-medium">Cargando datos del paciente...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="p-4 md:p-8 max-w-5xl mx-auto w-full flex flex-col gap-6 transition-colors duration-300">

            {/* Header / Acciones */}
            <div className="flex items-center justify-between">
                <AppButton
                    variant="ghost"
                    size="sm"
                    icon="arrow_back"
                    onClick={() => { try { localStorage.removeItem('activeConsultation'); } catch {} navigate('/medico'); }}
                >
                    Volver a Sala de Espera
                </AppButton>
                <div className="flex gap-3">
                    <AppButton
                        variant="outline"
                        size="md"
                        icon="picture_as_pdf"
                        onClick={handleDownloadPdf}
                        isLoading={isDownloadingPdf}
                        disabled={isDownloadingPdf}
                        title="Descargar Receta en PDF desde el servidor"
                    >
                        {isDownloadingPdf ? 'Generando...' : 'Descargar PDF'}
                    </AppButton>
                    <AppButton
                        variant="outline"
                        size="md"
                        icon="print"
                        onClick={handlePrint}
                        disabled={!selectedDiagnosis}
                        title={!selectedDiagnosis ? 'Seleccione un diagnóstico para imprimir' : 'Imprimir receta'}
                    >
                        Imprimir Receta
                    </AppButton>
                    <AppButton
                        variant="primary"
                        size="md"
                        icon={isSaving ? undefined : "check_circle"}
                        isLoading={isSaving}
                        onClick={handleFinalizarConsulta}
                        disabled={!selectedDiagnosis || isSaving}
                        title={!selectedDiagnosis ? 'Seleccione un diagnóstico principal primero' : ''}
                    >
                        {isSaving ? 'Guardando...' : 'Finalizar Consulta'}
                    </AppButton>
                </div>
            </div>

            {/* Banner del Paciente */}
            <div className="bg-white dark:bg-slate-900 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 p-6 flex flex-col xl:flex-row gap-6 items-start xl:items-center">
                <div className="flex items-center gap-4 flex-1 w-full">
                    <PatientCard
                        id={appointment.paciente?.id || 'N/A'}
                        fullName={`${appointment.paciente?.nombres} ${appointment.paciente?.apellidos}`}
                    />
                </div>
                {/* Signos Vitales del Triage */}
                <div className="flex items-center gap-4 bg-slate-50 dark:bg-slate-800/50 p-4 rounded-xl border border-slate-100 dark:border-slate-800 w-full md:w-auto overflow-x-auto scrollbar-hide">
                    <div className="flex flex-col items-center px-4 border-r border-slate-200 dark:border-slate-700">
                        <span className="text-xs font-bold text-slate-500 uppercase tracking-widest text-center whitespace-nowrap">P. Arterial</span>
                        <span className="text-lg font-black text-slate-900 dark:text-white whitespace-nowrap">
                            {signosVitales?.presionArterial || '--'} <span className="text-xs font-normal text-slate-400">mmHg</span>
                        </span>
                    </div>
                    <div className="flex flex-col items-center px-4 border-r border-slate-200 dark:border-slate-700">
                        <span className="text-xs font-bold text-slate-500 uppercase tracking-widest text-center whitespace-nowrap">F. Cardíaca</span>
                        <span className="text-lg font-black text-slate-900 dark:text-white whitespace-nowrap">
                            {signosVitales?.frecuenciaCardiaca || '--'} <span className="text-xs font-normal text-slate-400">lpm</span>
                        </span>
                    </div>
                    <div className="flex flex-col items-center px-4 border-r border-slate-200 dark:border-slate-700">
                        <span className="text-xs font-bold text-slate-500 uppercase tracking-widest text-center whitespace-nowrap">Temp</span>
                        <span className="text-lg font-black text-slate-900 dark:text-white whitespace-nowrap">
                            {signosVitales?.temperatura || '--'} <span className="text-xs font-normal text-slate-400">°C</span>
                        </span>
                    </div>
                    <div className="flex flex-col items-center px-4">
                        <span className="text-xs font-bold text-slate-500 uppercase tracking-widest text-center whitespace-nowrap">Peso / Talla</span>
                        <span className="text-lg font-black text-slate-900 dark:text-white whitespace-nowrap">
                            {signosVitales?.peso || '--'}<span className="text-xs font-normal text-slate-400">kg</span> / {signosVitales?.altura || '--'}<span className="text-xs font-normal text-slate-400">cm</span>
                        </span>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

                {/* Columna Izquierda: Notas Clínicas + Diagnóstico */}
                <div className="lg:col-span-2 flex flex-col gap-6">

                    {/* Anamnesis / Notas */}
                    <div className="bg-white dark:bg-slate-900 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 flex flex-col overflow-visible">
                        <div className="p-4 border-b border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/20 flex items-center gap-2">
                            <span className="material-symbols-outlined text-primary">clinical_notes</span>
                            <h3 className="font-bold text-slate-900 dark:text-white">Anamnesis y Evolución</h3>
                        </div>
                        <div className="p-6 flex flex-col gap-4">
                            <div>
                                <label className="block text-xs font-black text-slate-500 uppercase tracking-widest mb-2">Motivo de Consulta (Triage)</label>
                                <div className="p-3 bg-amber-50 dark:bg-amber-500/10 text-amber-900 dark:text-amber-200 rounded-xl text-sm font-medium border border-amber-100 dark:border-amber-500/20">
                                    {appointment.observaciones || 'Sin motivo registrado en triage.'}
                                </div>
                            </div>
                            <div>
                                <label className="block text-xs font-black text-slate-500 uppercase tracking-widest mb-2">Notas Médicas *</label>
                                <textarea
                                    value={notasMedicas}
                                    onChange={(e) => setNotasMedicas(e.target.value)}
                                    className="w-full rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-primary p-4 h-40 resize-none text-sm outline-none"
                                    placeholder="Redacte la evolución clínica, exploración física y observaciones..."
                                ></textarea>
                            </div>
                        </div>
                    </div>

                    {/* Diagnóstico Principal */}
                    <div className="bg-white dark:bg-slate-900 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 flex flex-col overflow-hidden">
                        <div className="p-4 border-b border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/20 flex items-center justify-between">
                            <div className="flex items-center gap-2">
                                <span className="material-symbols-outlined text-rose-500">search_insights</span>
                                <h3 className="font-bold text-slate-900 dark:text-white">Diagnóstico Principal <span className="text-rose-400">*</span></h3>
                            </div>
                            {hasAnyRole(['ROLE_ADMIN', 'ROLE_MEDICO']) && (
                                <button onClick={() => { setShowCreateModal(true); setNewDiagCodigo(diagQuery); setNewDiagDescripcion(''); }} className="text-primary font-bold text-sm hover:underline">
                                    Crear diagnóstico
                                </button>
                            )}
                        </div>
                        <div className="p-6">
                            <p className="text-sm text-slate-500 mb-4">Busque por descripción o código CIE-10.</p>
                            {selectedDiagnosis ? (
                                <div className="flex items-center justify-between p-4 bg-teal-50 dark:bg-teal-500/10 border border-teal-200 dark:border-teal-500/30 rounded-xl">
                                    <div className="flex items-center gap-3">
                                        <div className="px-2 py-1 bg-teal-100 dark:bg-teal-500/20 rounded-lg text-teal-700 dark:text-teal-300 font-black text-xs uppercase tracking-widest border border-teal-200 dark:border-teal-500/30">
                                            {selectedDiagnosis.codigoCIE}
                                        </div>
                                        <div className="flex flex-col">
                                            <span className="text-sm font-bold text-slate-900 dark:text-white">{selectedDiagnosis.descripcion}</span>
                                            <span className="text-xs text-teal-600 dark:text-teal-400 font-medium">Diagnóstico Principal Confirmado ✓</span>
                                        </div>
                                    </div>
                                    <button
                                        onClick={() => setSelectedDiagnosis(null)}
                                        className="text-slate-400 hover:text-rose-500 transition-colors"
                                        title="Cambiar diagnóstico"
                                    >
                                        <span className="material-symbols-outlined">close</span>
                                    </button>
                                </div>
                            ) : (
                                <div className="relative" ref={diagDropdownRef}>
                                    <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-[20px]">
                                        {isDiagSearching ? 'hourglass_top' : 'search'}
                                    </span>
                                    <input
                                        type="text"
                                        value={diagQuery}
                                        onChange={(e) => setDiagQuery(e.target.value)}
                                        className="w-full pl-12 pr-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 focus:ring-2 focus:ring-primary focus:border-primary text-sm outline-none transition-all"
                                        placeholder="Ej: faringitis, J06, hipertensión..."
                                    />
                                    {diagResults.length > 0 && (
                                        <div className="absolute z-10 w-full mt-2 bg-white dark:bg-slate-800 rounded-xl shadow-xl border border-slate-200 dark:border-slate-700 max-h-60 overflow-y-auto">
                                                    {diagResults.map((diag) => (
                                                <button
                                                    key={diag.id}
                                                    onClick={() => {
                                                        setSelectedDiagnosis(diag);
                                                        setDiagQuery('');
                                                        setDiagResults([]);
                                                    }}
                                                    className="w-full text-left px-4 py-3 hover:bg-slate-50 dark:hover:bg-slate-700/50 border-b border-slate-100 dark:border-slate-700/50 last:border-0 transition-colors flex items-center gap-3"
                                                >
                                                    <span className="text-xs font-black text-slate-400 bg-slate-100 dark:bg-slate-700 px-2 py-1 rounded shrink-0">{diag.codigoCIE}</span>
                                                    <span className="text-sm text-slate-700 dark:text-slate-200 truncate">{diag.descripcion}</span>
                                                </button>
                                            ))}
                                        </div>
                                    )}
                                    {diagResults.length === 0 && diagQuery.length >= 2 && (
                                        <div className="mt-3 px-3 flex items-center justify-between text-sm text-slate-500">
                                            <span>No se encontraron diagnósticos.</span>
                                            {hasAnyRole(['ROLE_ADMIN', 'ROLE_MEDICO']) ? (
                                                <button onClick={() => { setShowCreateModal(true); setNewDiagCodigo(diagQuery); setNewDiagDescripcion(''); }} className="text-primary font-bold hover:underline">
                                                    Crear diagnóstico
                                                </button>
                                            ) : (
                                                <span className="text-xs text-slate-400">Pide al administrador crear este diagnóstico</span>
                                            )}
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Columna Derecha: Receta Médica */}
                <div className="flex flex-col gap-6">
                    <div className="bg-white dark:bg-slate-900 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 flex flex-col overflow-visible h-full">
                        <div className="p-4 border-b border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/20 flex items-center gap-2">
                            <span className="material-symbols-outlined text-teal-500">prescriptions</span>
                            <h3 className="font-bold text-slate-900 dark:text-white">Receta Médica</h3>
                        </div>

                        <div className="p-6 flex flex-col flex-1 gap-5">
                            <p className="text-sm text-slate-500">Agregue medicamentos al plan de tratamiento.</p>

                            {/* Formulario de selección de medicamento */}
                            <div className="flex flex-col gap-3 p-4 bg-slate-50 dark:bg-slate-800/50 rounded-xl border border-slate-200 dark:border-slate-700">
                                {selectedMed ? (
                                    <div className="flex flex-col gap-3">
                                        <div className="flex items-center justify-between">
                                            <div className="flex items-center gap-2">
                                                <span className="material-symbols-outlined text-teal-500 text-[18px]">pill</span>
                                                <span className="font-bold text-sm text-slate-900 dark:text-white">{selectedMed.nombre}</span>
                                            </div>
                                            <button onClick={() => setSelectedMed(null)} className="text-slate-400 hover:text-rose-500">
                                                <span className="material-symbols-outlined text-[16px]">close</span>
                                            </button>
                                        </div>
                                        <div className="grid grid-cols-2 gap-2">
                                            <input type="text" value={dosisValue} onChange={e => setDosisValue(e.target.value)}
                                                placeholder="Dosis (ej: 500mg)"
                                                className="w-full text-sm p-2.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 outline-none focus:ring-2 focus:ring-teal-500" />
                                            <input type="text" value={frecuenciaValue} onChange={e => setFrecuenciaValue(e.target.value)}
                                                placeholder="Frecuencia"
                                                className="w-full text-sm p-2.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 outline-none focus:ring-2 focus:ring-teal-500" />
                                            <input type="text" value={duracionValue} onChange={e => setDuracionValue(e.target.value)}
                                                placeholder="Duración (ej: 7 días)"
                                                className="w-full col-span-2 text-sm p-2.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 outline-none focus:ring-2 focus:ring-teal-500" />
                                        </div>
                                        <AppButton
                                            fullWidth
                                            variant="primary"
                                            size="sm"
                                            icon="add"
                                            onClick={handleAddPrescription}
                                            disabled={!dosisValue || !frecuenciaValue || !duracionValue}
                                            className="mt-2"
                                        >
                                            Añadir a receta
                                        </AppButton>
                                    </div>
                                ) : (
                                    <div className="relative" ref={medDropdownRef}>
                                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-[18px]">
                                            {isMedSearching ? 'hourglass_top' : 'medication'}
                                        </span>
                                        <input
                                            type="text"
                                            value={medQuery}
                                            onChange={(e) => setMedQuery(e.target.value)}
                                            className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 outline-none focus:ring-2 focus:ring-teal-500 text-sm"
                                            placeholder="Buscar medicamento..."
                                        />
                                        {medResults.length > 0 && (
                                            <div className="absolute z-10 w-full mt-2 bg-white dark:bg-slate-800 rounded-xl shadow-xl border border-slate-200 dark:border-slate-700 max-h-48 overflow-y-auto">
                                                {medResults.map((med) => (
                                                    <button
                                                        key={med.id}
                                                        onClick={() => {
                                                            setSelectedMed(med);
                                                            setMedQuery('');
                                                            setMedResults([]);
                                                        }}
                                                        className="w-full text-left px-4 py-3 hover:bg-slate-50 dark:hover:bg-slate-700/50 border-b border-slate-100 dark:border-slate-700/50 last:border-0 transition-colors flex flex-col"
                                                    >
                                                        <span className="text-sm font-bold text-slate-700 dark:text-slate-200">{med.nombre}</span>
                                                        <span className="text-xs text-slate-400 truncate">{med.descripcion || 'Sin descripción'}</span>
                                                    </button>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                )}
                            </div>

                            {/* Alertas de interacción medicamentosa */}
                            <DrugInteractionAlert interacciones={interacciones} onDismiss={() => setInteracciones([])} />

                            {/* Lista de recetas */}
                            <div className="flex-1 flex flex-col">
                                {prescriptions.length === 0 ? (
                                    <div className="flex-1 border border-slate-100 dark:border-slate-800 rounded-xl bg-slate-50/50 dark:bg-slate-900/50 flex flex-col items-center justify-center p-6 text-center min-h-[120px]">
                                        <span className="material-symbols-outlined text-4xl text-slate-300 mb-2">receipt_long</span>
                                        <p className="text-sm font-medium text-slate-500">Sin medicamentos prescritos.</p>
                                    </div>
                                ) : (
                                    <div className="flex flex-col gap-2">
                                        <h4 className="text-xs font-black text-slate-500 uppercase tracking-widest mb-1">Medicamentos Agregados</h4>
                                        {prescriptions.map((rx, idx) => (
                                            <div key={idx} className="flex flex-col p-3 border border-slate-200 dark:border-slate-700 rounded-xl bg-white dark:bg-slate-800 relative group">
                                                <div className="flex items-center gap-2">
                                                    <span className="w-1.5 h-1.5 rounded-full bg-teal-500 shrink-0"></span>
                                                    <span className="font-bold text-sm text-slate-900 dark:text-white">{rx.medicamento.nombre}</span>
                                                    <button
                                                        onClick={() => handleRemovePrescription(idx)}
                                                        className="absolute top-3 right-3 opacity-0 group-hover:opacity-100 transition-opacity text-slate-400 hover:text-rose-500"
                                                    >
                                                        <span className="material-symbols-outlined text-[16px]">delete</span>
                                                    </button>
                                                </div>
                                                <div className="text-xs text-slate-500 dark:text-slate-400 mt-1 pl-3 border-l-2 border-slate-100 dark:border-slate-700 ml-[3px]">
                                                    <p><b>Dosis:</b> {rx.dosis} · <b>Frec:</b> {rx.frecuencia}</p>
                                                    <p><b>Duración:</b> {rx.duracion}</p>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Toast de error */}
            {/* Modal: Crear Diagnóstico (desde la vista del médico) */}
            {showCreateModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
                    <div className="bg-white dark:bg-slate-900 rounded-xl w-full max-w-xl p-6 border border-slate-200 dark:border-slate-800">
                        <h3 className="text-lg font-bold mb-1">Crear diagnóstico</h3>
                        <p className="text-sm text-slate-500 mb-4">Crear un nuevo código ICD‑10 y seleccionarlo para esta consulta.</p>
                        <div className="grid gap-3">
                            <label className="text-[11px] font-black text-slate-500 uppercase">Código CIE-10</label>
                            <input value={newDiagCodigo} onChange={(e) => setNewDiagCodigo(e.target.value)} placeholder="Ej: J06" className="w-full px-3 py-2 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 outline-none" />
                            <label className="text-[11px] font-black text-slate-500 uppercase">Descripción</label>
                            <input value={newDiagDescripcion} onChange={(e) => setNewDiagDescripcion(e.target.value)} placeholder="Faringitis aguda" className="w-full px-3 py-2 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 outline-none" />
                            {createDiagError && <p className="text-xs text-red-500">{createDiagError}</p>}
                            <div className="flex justify-end gap-3 mt-3">
                                <button onClick={() => { setShowCreateModal(false); setCreateDiagError(null); }} className="px-4 py-2 rounded-lg border">Cancelar</button>
                                <button onClick={handleCreateDiagnostico} disabled={creatingDiag} className="px-4 py-2 rounded-lg bg-primary text-white">
                                    {creatingDiag ? 'Creando...' : 'Crear y seleccionar'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
            {saveError && (
                <div className="fixed bottom-6 right-6 z-50 bg-rose-50 border border-rose-200 text-rose-700 px-5 py-4 rounded-2xl shadow-xl flex items-center gap-3 text-sm font-medium max-w-sm">
                    <span className="material-symbols-outlined text-rose-500 shrink-0">error</span>
                    <span className="flex-1">{saveError}</span>
                    <button onClick={() => setSaveError(null)} className="text-rose-400 hover:text-rose-600 shrink-0">
                        <span className="material-symbols-outlined text-[18px]">close</span>
                    </button>
                </div>
            )}

            {/* Componente de receta imprimible — oculto en pantalla, visible solo al imprimir */}
            {appointment && selectedDiagnosis && (
                <PrintableReceta
                    appointment={appointment}
                    diagnosis={selectedDiagnosis}
                    prescriptions={prescriptions}
                    notasMedicas={notasMedicas}
                    doctorName={doctorName}
                />
            )}
        </div>
    );
};

export default DoctorConsultationView;
