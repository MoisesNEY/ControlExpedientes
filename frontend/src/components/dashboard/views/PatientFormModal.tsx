import { useState, useEffect, useRef, type HTMLAttributes, type RefObject } from 'react';
import { PacienteService, type PacienteDTO } from '../../../services/paciente.service';
import { formatNicaraguanCedula, formatPhoneNumber, isValidNicaraguanCedula, isValidPhoneNumber } from '../../../utils/inputMasks';

interface PatientFormModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSaveSuccess: () => void;
    editingPaciente?: PacienteDTO | null;
}

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

const sanitizePacienteForm = (paciente: PacienteDTO): PacienteDTO => ({
    ...paciente,
    cedula: formatNicaraguanCedula(paciente.cedula || ''),
    telefono: formatPhoneNumber(paciente.telefono || ''),
});

const todayIsoDate = () => new Date().toISOString().split('T')[0];

const PatientFormModal = ({ isOpen, onClose, onSaveSuccess, editingPaciente }: PatientFormModalProps) => {
    const [form, setForm] = useState<PacienteDTO>(emptyPaciente);
    const [saving, setSaving] = useState(false);
    const birthDateInputRef = useRef<HTMLInputElement>(null);

    const cedulaError = form.cedula && !isValidNicaraguanCedula(form.cedula)
        ? 'Formato requerido: 000-000000-0000X'
        : '';
    const telefonoError = form.telefono && !isValidPhoneNumber(form.telefono)
        ? 'Formato requerido: 0000-0000'
        : '';

    useEffect(() => {
        if (isOpen) {
            setForm(editingPaciente ? sanitizePacienteForm({ ...editingPaciente }) : emptyPaciente);
        }
    }, [isOpen, editingPaciente]);

    if (!isOpen) return null;

    const handleSave = async () => {
        if (!form.nombres || !form.apellidos || !form.fechaNacimiento) {
            alert('Por favor complete los campos obligatorios: nombres, apellidos y fecha de nacimiento.');
            return;
        }

        if (cedulaError) {
            alert('La cédula debe tener el formato 000-000000-0000X.');
            return;
        }

        if (telefonoError) {
            alert('El teléfono debe tener el formato 0000-0000.');
            return;
        }

        const today = new Date();
        const selectedDate = new Date(`${form.fechaNacimiento}T00:00:00`);
        if (selectedDate.getTime() > today.getTime()) {
            alert('La fecha de nacimiento no puede ser futura.');
            return;
        }

        setSaving(true);
        try {
            const payload: PacienteDTO = {
                ...form,
                nombres: form.nombres.trim(),
                apellidos: form.apellidos.trim(),
                cedula: form.cedula || undefined,
                telefono: form.telefono || undefined,
                direccion: form.direccion?.trim() || undefined,
                email: form.email?.trim() || undefined,
            };

            if (editingPaciente?.id) {
                await PacienteService.update(editingPaciente.id, payload);
            } else {
                await PacienteService.create(payload);
            }
            onSaveSuccess();
            onClose();
        } catch (error: any) {
            console.error('Error saving paciente:', error);
            alert(error?.response?.data?.message || 'Error al guardar el paciente.');
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={onClose} />
            <div className="relative bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                <div className="sticky top-0 bg-white dark:bg-slate-900 p-6 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center z-10">
                    <h3 className="text-lg font-black text-slate-900 dark:text-white">
                        {editingPaciente ? 'Editar Paciente' : 'Nuevo Paciente'}
                    </h3>
                    <button onClick={onClose} className="text-slate-400 hover:text-slate-600 transition-colors">
                        <span className="material-symbols-outlined">close</span>
                    </button>
                </div>
                <div className="p-6 space-y-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <FormField label="Nombres *" value={form.nombres} onChange={v => setForm({ ...form, nombres: v })} placeholder="Juan Carlos" />
                        <FormField label="Apellidos *" value={form.apellidos} onChange={v => setForm({ ...form, apellidos: v })} placeholder="Pérez López" />
                        <div className="flex flex-col gap-1.5">
                            <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Sexo *</label>
                            <select
                                value={form.sexo}
                                onChange={e => setForm({ ...form, sexo: e.target.value as any })}
                                className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-primary text-slate-900 dark:text-white"
                            >
                                <option value="MASCULINO">Masculino</option>
                                <option value="FEMENINO">Femenino</option>
                                <option value="OTRO">Otro</option>
                            </select>
                        </div>
                        <DateField
                            label="Fecha de Nacimiento *"
                            value={form.fechaNacimiento}
                            max={todayIsoDate()}
                            inputRef={birthDateInputRef}
                            onChange={v => setForm({ ...form, fechaNacimiento: v })}
                        />
                        <FormField label="Cédula" value={form.cedula || ''} onChange={v => setForm({ ...form, cedula: formatNicaraguanCedula(v) })} placeholder="000-000000-0000X" error={cedulaError} autoCapitalize="characters" />
                        <FormField label="Teléfono" value={form.telefono || ''} onChange={v => setForm({ ...form, telefono: formatPhoneNumber(v) })} placeholder="0000-0000" inputMode="numeric" error={telefonoError} />
                        <FormField label="Email" type="email" value={form.email || ''} onChange={v => setForm({ ...form, email: v })} placeholder="correo@ejemplo.com" />
                        <div className="flex flex-col gap-1.5">
                            <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Estado Civil</label>
                            <select
                                value={form.estadoCivil || ''}
                                onChange={e => setForm({ ...form, estadoCivil: e.target.value as any || undefined })}
                                className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-primary text-slate-900 dark:text-white"
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
                                className="w-4 h-4 accent-primary"
                            />
                            <label className="text-sm font-bold text-slate-700 dark:text-slate-300">Paciente Activo</label>
                        </div>
                    </div>
                    <FormField label="Dirección" value={form.direccion || ''} onChange={v => setForm({ ...form, direccion: v })} placeholder="Barrio, calle, casa #..." />
                </div>
                <div className="sticky bottom-0 bg-white dark:bg-slate-900 p-6 border-t border-slate-100 dark:border-slate-800 flex justify-end gap-3">
                    <button onClick={onClose} className="px-6 py-2.5 text-sm font-bold text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors">
                        Cancelar
                    </button>
                    <button
                        onClick={handleSave}
                        disabled={saving}
                        className={`px-6 py-2.5 text-sm font-bold text-white bg-primary rounded-xl shadow-lg shadow-primary/30 hover:scale-105 transition-all ${saving ? 'opacity-70 cursor-not-allowed' : ''}`}
                    >
                        {saving ? 'Guardando...' : editingPaciente ? 'Actualizar' : 'Crear Paciente'}
                    </button>
                </div>
            </div>
        </div>
    );
};

const DateField = ({ label, value, onChange, max, inputRef }: {
    label: string;
    value: string;
    onChange: (value: string) => void;
    max: string;
    inputRef: RefObject<HTMLInputElement | null>;
}) => (
    <div className="flex flex-col gap-1.5">
        <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">{label}</label>
        <div className="relative">
            <input
                ref={inputRef}
                type="date"
                value={value}
                max={max}
                onChange={e => onChange(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2.5 pr-11 text-sm outline-none focus:ring-2 focus:ring-primary transition-all text-slate-900 dark:text-white"
            />
            <button
                type="button"
                onClick={() => inputRef.current?.showPicker?.()}
                className="absolute inset-y-0 right-0 px-3 text-slate-400 hover:text-primary transition-colors"
                aria-label="Abrir calendario"
            >
                <span className="material-symbols-outlined text-[18px]">calendar_month</span>
            </button>
        </div>
        <p className="text-[11px] text-slate-400">Selecciona la fecha desde el calendario. No se permiten fechas futuras.</p>
    </div>
);

const FormField = ({ label, value, onChange, placeholder, type = 'text', maxLength, inputMode, error, autoCapitalize }: {
    label: string;
    value: string;
    onChange: (v: string) => void;
    placeholder?: string;
    type?: string;
    maxLength?: number;
    inputMode?: HTMLAttributes<HTMLInputElement>['inputMode'];
    error?: string;
    autoCapitalize?: string;
}) => (
    <div className="flex flex-col gap-1.5">
        <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest">{label}</label>
        <input
            type={type}
            value={value}
            onChange={e => onChange(e.target.value)}
            placeholder={placeholder}
            maxLength={maxLength}
            inputMode={inputMode}
            autoCapitalize={autoCapitalize}
            className={`bg-slate-50 dark:bg-slate-800 border rounded-xl px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-primary transition-all text-slate-900 dark:text-white ${error ? 'border-rose-300 dark:border-rose-500/40' : 'border-slate-200 dark:border-slate-700'}`}
        />
        {error && <p className="text-[11px] font-medium text-rose-500">{error}</p>}
    </div>
);

export default PatientFormModal;
