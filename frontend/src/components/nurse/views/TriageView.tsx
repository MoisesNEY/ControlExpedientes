import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { CitaService, type CitaMedicaDTO } from '../../../services/cita.service';
import { PacienteService, type PacienteDTO } from '../../../services/paciente.service';
import { ExpedienteService } from '../../../services/expediente.service';
import api from '../../../services/api';
import {
    validateBP,
    validateVital,
    calcIMC,
    imcClassification,
} from '../../../services/signosVitales.service';

// ─── Field component ───────────────────────────────────────────────────────────
interface VitalFieldProps {
    label: string;
    unit: string;
    icon: string;
    value: string;
    onChange: (v: string) => void;
    error?: string | null;
    type?: string;
    placeholder?: string;
    hint?: string;
}
const VitalField = ({ label, unit, icon, value, onChange, error, type = 'number', placeholder, hint }: VitalFieldProps) => (
    <div className="flex flex-col gap-1">
        <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest flex items-center gap-1.5">
            <span className="material-symbols-outlined text-sm text-slate-400">{icon}</span>
            {label}
        </label>
        <div className={`flex items-center bg-slate-50 dark:bg-slate-800 border rounded-xl overflow-hidden transition-all ${error ? 'border-red-400 ring-1 ring-red-400' : 'border-slate-200 dark:border-slate-700 focus-within:ring-2 focus-within:ring-primary/50 focus-within:border-primary'}`}>
            <input
                type={type}
                value={value}
                onChange={e => onChange(e.target.value)}
                placeholder={placeholder}
                step="any"
                className="flex-1 bg-transparent px-4 py-2.5 text-sm text-slate-900 dark:text-white outline-none min-w-0"
            />
            {unit && (
                <span className="px-3 text-xs font-black text-slate-400 uppercase border-l border-slate-200 dark:border-slate-700 bg-slate-100 dark:bg-slate-700/50 py-2.5 whitespace-nowrap">
                    {unit}
                </span>
            )}
        </div>
        {error ? (
            <p className="text-xs text-red-500 font-semibold flex items-center gap-1">
                <span className="material-symbols-outlined text-xs">warning</span>
                {error}
            </p>
        ) : hint ? (
            <p className="text-[10px] text-slate-400">{hint}</p>
        ) : null}
    </div>
);

// ─── Main Component ────────────────────────────────────────────────────────────
const TriageView = () => {
    const { citaId } = useParams<{ citaId: string }>();
    const navigate = useNavigate();

    const [cita, setCita] = useState<CitaMedicaDTO | null>(null);
    const [paciente, setPaciente] = useState<PacienteDTO | null>(null);
    const [expedienteId, setExpedienteId] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [savedOk, setSavedOk] = useState(false);

    // ── Vitals form state ──────────────────────────────────────────────────
    const [presion, setPresion] = useState('');
    const [fc, setFc] = useState('');
    const [fr, setFr] = useState('');
    const [temp, setTemp] = useState('');
    const [peso, setPeso] = useState('');
    const [talla, setTalla] = useState('');
    const [sao2, setSao2] = useState('');
    const [alergias, setAlergias] = useState('');
    const [motivo, setMotivo] = useState('');

    // ── Validation errors ──────────────────────────────────────────────────
    const [erPresion, setErPresion] = useState<string | null>(null);
    const [erFc, setErFc] = useState<string | null>(null);
    const [erFr, setErFr] = useState<string | null>(null);
    const [erTemp, setErTemp] = useState<string | null>(null);
    const [erPeso, setErPeso] = useState<string | null>(null);
    const [erTalla, setErTalla] = useState<string | null>(null);
    const [erSao2, setErSao2] = useState<string | null>(null);

    // ── Derived values ─────────────────────────────────────────────────────
    const imc = calcIMC(parseFloat(peso), parseFloat(talla));
    const imcInfo = imc ? imcClassification(imc) : null;

    // ── Load appointment + patient ─────────────────────────────────────────
    useEffect(() => {
        if (!citaId) return;
        const load = async () => {
            setLoading(true);
            try {
                const c = await CitaService.getById(parseInt(citaId));
                setCita(c);
                if (c.paciente?.id) {
                    const p = await PacienteService.getById(c.paciente.id);
                    setPaciente(p);
                    // Get expediente to link consultation
                    try {
                        const exp = await ExpedienteService.getByPacienteId(p.id!);
                        if (exp?.id) setExpedienteId(exp.id);
                    } catch (_) { /* no expediente yet — consultation saved without it */ }
                }
            } catch (e) {
                console.error('Error cargando cita para triage', e);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [citaId]);

    // ── Live validation handlers ───────────────────────────────────────────
    const handlePresion = (v: string) => {
        setPresion(v);
        // Only validate when pattern looks complete e.g. "120/80"
        setErPresion(v.length > 3 ? validateBP(v) : null);
    };
    const handleFc = (v: string) => { setFc(v); setErFc(v ? validateVital('fc', parseFloat(v)) : null); };
    const handleFr = (v: string) => { setFr(v); setErFr(v ? validateVital('fr', parseFloat(v)) : null); };
    const handleTemp = (v: string) => { setTemp(v); setErTemp(v ? validateVital('temperatura', parseFloat(v)) : null); };
    const handlePeso = (v: string) => { setPeso(v); setErPeso(v ? validateVital('peso', parseFloat(v)) : null); };
    const handleTalla = (v: string) => { setTalla(v); setErTalla(v ? validateVital('altura', parseFloat(v)) : null); };
    const handleSao2 = (v: string) => { setSao2(v); setErSao2(v ? validateVital('sao2', parseFloat(v)) : null); };

    const hasErrors = !![erPresion, erFc, erFr, erTemp, erPeso, erTalla, erSao2].find(Boolean);

    // ── Save triage ────────────────────────────────────────────────────────
    const handleSave = async () => {
        if (hasErrors) { alert('Corrige los errores antes de guardar.'); return; }
        if (!motivo.trim()) { alert('El motivo de consulta es obligatorio.'); return; }
        if (!cita?.id) return;

        setSaving(true);
        try {
            // 1. Create a consultation (pre-clínica) linked to expediente + motivo + vitals
            const today = new Date().toISOString().split('T')[0];
            const consultaPayload: any = {
                motivoConsulta: motivo.trim(),
                notasMedicas: alergias.trim() ? `ALERGIAS: ${alergias.trim()}` : '',
                fechaConsulta: today,
                ...(expedienteId ? { expediente: { id: expedienteId } } : {}),
            };
            const consultaResp = await api.post('/api/consulta-medicas', consultaPayload);
            const consultaId: number = consultaResp.data.id;

            // 2. Save vitals linked to newly created consultation
            const vitalsPayload: any = {
                consulta: { id: consultaId },
            };
            if (peso) vitalsPayload.peso = parseFloat(peso);
            if (talla) vitalsPayload.altura = parseFloat(talla);
            if (presion) vitalsPayload.presionArterial = presion;
            if (temp) vitalsPayload.temperatura = parseFloat(temp);
            if (fc) vitalsPayload.frecuenciaCardiaca = parseInt(fc);
            await api.post('/api/signos-vitales', vitalsPayload);

            // 3. Patch cita to ESPERANDO_MEDICO so the doctor can see it in their dashboard
            await CitaService.patch(cita.id, { estado: 'ESPERANDO_MEDICO' as any });

            setSavedOk(true);
            setTimeout(() => navigate('/enfermeria'), 2000);
        } catch (error: any) {
            alert(error?.response?.data?.message || 'Error al guardar el triage. Verifica que el paciente tenga expediente clínico abierto.');
        } finally {
            setSaving(false);
        }
    };

    // ── Early states ───────────────────────────────────────────────────────
    if (loading) return (
        <div className="flex items-center justify-center h-full min-h-64">
            <div className="text-center space-y-3">
                <div className="animate-spin rounded-full h-10 w-10 border-4 border-slate-200 border-t-primary mx-auto" />
                <p className="text-sm text-slate-500 font-medium">Cargando paciente...</p>
            </div>
        </div>
    );

    if (!cita) return (
        <div className="flex flex-col items-center justify-center h-full p-8 text-center gap-3">
            <span className="material-symbols-outlined text-5xl text-slate-300">error</span>
            <p className="font-black text-slate-700 dark:text-slate-300">Cita no encontrada</p>
            <button onClick={() => navigate('/enfermeria')} className="text-primary font-bold text-sm hover:underline">← Volver al panel</button>
        </div>
    );

    if (savedOk) return (
        <div className="flex flex-col items-center justify-center h-full p-8 text-center gap-4">
            <div className="w-20 h-20 rounded-full bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center">
                <span className="material-symbols-outlined text-5xl text-emerald-500">check_circle</span>
            </div>
            <div>
                <h3 className="font-black text-slate-900 dark:text-white text-xl">Triage completado</h3>
                <p className="text-slate-500 text-sm mt-1">Signos vitales guardados. Paciente listo para consulta médica.</p>
            </div>
            <p className="text-xs text-slate-400">Redirigiendo al panel...</p>
        </div>
    );

    const nombrePaciente = paciente
        ? `${paciente.nombres} ${paciente.apellidos || ''}`.trim()
        : cita.paciente?.nombres ?? 'Paciente';

    // ── Full Triage Form ───────────────────────────────────────────────────
    return (
        <div className="p-4 md:p-8 space-y-6 max-w-4xl mx-auto">
            {/* Back + Header */}
            <div>
                <button onClick={() => navigate('/enfermeria')} className="flex items-center gap-1.5 text-sm text-slate-500 hover:text-primary font-bold mb-4 transition-colors">
                    <span className="material-symbols-outlined text-sm">arrow_back</span>
                    Volver al Panel
                </button>
                <div className="flex flex-col sm:flex-row sm:items-center gap-4">
                    <div className="w-14 h-14 rounded-2xl bg-primary/10 dark:bg-primary/20 flex items-center justify-center shrink-0">
                        <span className="material-symbols-outlined text-primary text-3xl">vital_signs</span>
                    </div>
                    <div>
                        <h2 className="text-xl md:text-2xl font-black text-slate-900 dark:text-white">
                            Triage Pre-clínico
                        </h2>
                        <p className="text-slate-500 text-sm font-medium mt-0.5">
                            Paciente: <span className="font-black text-slate-700 dark:text-slate-300">{nombrePaciente}</span>
                            {paciente?.cedula && <span className="text-slate-400"> · C.I. {paciente.cedula}</span>}
                        </p>
                    </div>
                </div>
            </div>

            {/* ─── VITALS SECTION ─────────────────────────────────────────────── */}
            <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex items-center gap-2">
                    <span className="material-symbols-outlined text-primary">monitor_heart</span>
                    <h3 className="font-black text-slate-900 dark:text-white text-sm uppercase tracking-tight">Signos Vitales</h3>
                    <span className="text-[10px] text-slate-400 font-bold ml-auto">Rangos clínicos validados — OMS / AHA</span>
                </div>
                <div className="p-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
                    <VitalField
                        label="Presión Arterial"
                        unit="mmHg"
                        icon="blood_pressure"
                        value={presion}
                        onChange={handlePresion}
                        error={erPresion}
                        type="text"
                        placeholder="120/80"
                        hint="Formato: Sistólica/Diastólica"
                    />
                    <VitalField
                        label="Frecuencia Cardíaca"
                        unit="lpm"
                        icon="cardiology"
                        value={fc}
                        onChange={handleFc}
                        error={erFc}
                        placeholder="75"
                        hint="Rango normal: 60-100 lpm"
                    />
                    <VitalField
                        label="Frecuencia Respiratoria"
                        unit="rpm"
                        icon="respiratory_rate"
                        value={fr}
                        onChange={handleFr}
                        error={erFr}
                        placeholder="16"
                        hint="Rango normal: 12-20 rpm"
                    />
                    <VitalField
                        label="Temperatura"
                        unit="°C"
                        icon="thermometer"
                        value={temp}
                        onChange={handleTemp}
                        error={erTemp}
                        placeholder="36.5"
                        hint="Rango normal: 36.0-37.5 °C"
                    />
                    <VitalField
                        label="Peso"
                        unit="kg"
                        icon="scale"
                        value={peso}
                        onChange={handlePeso}
                        error={erPeso}
                        placeholder="70"
                    />
                    <VitalField
                        label="Talla"
                        unit="cm"
                        icon="height"
                        value={talla}
                        onChange={handleTalla}
                        error={erTalla}
                        placeholder="170"
                    />
                    <VitalField
                        label="Saturación de Oxígeno (SpO₂)"
                        unit="%"
                        icon="humidity_percentage"
                        value={sao2}
                        onChange={handleSao2}
                        error={erSao2}
                        placeholder="98"
                        hint="Normal: ≥ 95%"
                    />

                    {/* IMC auto-calculated */}
                    <div className="flex flex-col gap-1">
                        <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest flex items-center gap-1.5">
                            <span className="material-symbols-outlined text-sm text-slate-400">calculate</span>
                            IMC (calculado)
                        </label>
                        <div className={`flex items-center bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 min-h-[42px]`}>
                            {imc ? (
                                <div className="flex items-center justify-between w-full gap-2">
                                    <span className="text-sm font-black text-slate-900 dark:text-white">{imc}</span>
                                    {imcInfo && (
                                        <span className={`text-[10px] font-black uppercase ${imcInfo.color}`}>
                                            {imcInfo.label}
                                        </span>
                                    )}
                                </div>
                            ) : (
                                <span className="text-xs text-slate-400 italic">Ingresa peso y talla</span>
                            )}
                        </div>
                        <p className="text-[10px] text-slate-400">Se calcula automáticamente: peso / talla²</p>
                    </div>
                </div>
            </div>

            {/* ─── CLINICAL INFO SECTION ──────────────────────────────────────── */}
            <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex items-center gap-2">
                    <span className="material-symbols-outlined text-primary">clinical_notes</span>
                    <h3 className="font-black text-slate-900 dark:text-white text-sm uppercase tracking-tight">Información Clínica</h3>
                </div>
                <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-5">
                    <div className="flex flex-col gap-1.5">
                        <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest flex items-center gap-1.5">
                            <span className="material-symbols-outlined text-sm text-slate-400">emergency</span>
                            Alergias conocidas
                        </label>
                        <textarea
                            value={alergias}
                            onChange={e => setAlergias(e.target.value)}
                            placeholder="Ej: Penicilina, sulfonamidas, látex... (dejar en blanco si no hay)"
                            rows={3}
                            className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-3 text-sm text-slate-900 dark:text-white outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all resize-none"
                        />
                    </div>
                    <div className="flex flex-col gap-1.5">
                        <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest flex items-center gap-1.5">
                            <span className="material-symbols-outlined text-sm text-slate-400">help</span>
                            Motivo de Consulta <span className="text-red-500">*</span>
                        </label>
                        <textarea
                            value={motivo}
                            onChange={e => setMotivo(e.target.value)}
                            placeholder="Describe el motivo principal de la visita del paciente..."
                            rows={3}
                            className={`bg-slate-50 dark:bg-slate-800 border rounded-xl px-4 py-3 text-sm text-slate-900 dark:text-white outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all resize-none ${!motivo.trim() ? 'border-slate-200 dark:border-slate-700' : 'border-emerald-400'}`}
                        />
                        {!motivo.trim() && (
                            <p className="text-[10px] text-slate-400">Campo requerido para continuar.</p>
                        )}
                    </div>
                </div>
            </div>

            {/* ─── Actions ────────────────────────────────────────────────────── */}
            <div className="flex flex-col sm:flex-row justify-end gap-3 pb-8">
                <button
                    onClick={() => navigate('/enfermeria')}
                    className="px-6 py-3 text-sm font-bold text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors"
                >
                    Cancelar
                </button>
                <button
                    onClick={handleSave}
                    disabled={saving || hasErrors}
                    className={`flex items-center justify-center gap-2 px-8 py-3 text-sm font-bold text-white rounded-xl shadow-lg transition-all
                        ${saving || hasErrors
                            ? 'bg-slate-400 cursor-not-allowed shadow-none'
                            : 'bg-primary shadow-primary/30 hover:scale-105 active:scale-95'
                        }`}
                >
                    {saving ? (
                        <>
                            <span className="animate-spin material-symbols-outlined text-sm">progress_activity</span>
                            Guardando...
                        </>
                    ) : (
                        <>
                            <span className="material-symbols-outlined text-sm">save</span>
                            Completar Triage
                        </>
                    )}
                </button>
            </div>
        </div>
    );
};

export default TriageView;
