import api from './api';

export interface SignosVitalesDTO {
    id?: number;
    peso?: number;
    altura?: number;
    presionArterial?: string;
    temperatura?: number;
    frecuenciaCardiaca?: number;
    consulta?: { id: number };
}

export const SignosVitalesService = {
    create: async (data: SignosVitalesDTO): Promise<SignosVitalesDTO> => {
        const response = await api.post('/api/signos-vitales', data);
        return response.data;
    },

    getByConsulta: async (consultaId: number): Promise<SignosVitalesDTO[]> => {
        const response = await api.get('/api/signos-vitales', {
            params: { 'consultaId.equals': consultaId },
        });
        return response.data;
    },

    getTodayByPacienteId: async (pacienteId: number): Promise<SignosVitalesDTO[]> => {
        const response = await api.get(`/api/signos-vitales/paciente/${pacienteId}/hoy`);
        return response.data;
    }
};

// ─── Clinical validation ranges (WHO / AHA guidelines) ────────────────────────
export const VITALS_RANGES = {
    temperatura: { min: 33.0, max: 43.0, unit: '°C', label: 'Temperatura' },
    peso: { min: 0.5, max: 350, unit: 'kg', label: 'Peso' },
    altura: { min: 30, max: 250, unit: 'cm', label: 'Talla' },
    fc: { min: 20, max: 300, unit: 'lpm', label: 'Frec. Cardíaca' },
    fr: { min: 5, max: 70, unit: 'rpm', label: 'Frec. Respiratoria' },
    sao2: { min: 50, max: 100, unit: '%', label: 'SpO₂' },
} as const;

// Presión arterial: systolic 50-300, diastolic 20-200, systolic > diastolic
export const validateBP = (value: string): string | null => {
    if (!value) return null;
    const match = value.match(/^(\d+)\/(\d+)$/);
    if (!match) return 'Formato debe ser Sistólica/Diastólica (ej: 120/80)';
    const sys = parseInt(match[1]);
    const dia = parseInt(match[2]);
    if (sys < 50 || sys > 300) return 'Presión sistólica fuera de rango (50-300 mmHg)';
    if (dia < 20 || dia > 200) return 'Presión diastólica fuera de rango (20-200 mmHg)';
    if (sys <= dia) return 'La sistólica debe ser mayor que la diastólica';
    if (sys - dia > 100) return 'Diferencial de presión inusualmente alto (>100 mmHg)';
    return null;
};

export const validateVital = (
    key: keyof typeof VITALS_RANGES,
    value: number | string
): string | null => {
    const range = VITALS_RANGES[key];
    const num = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(num)) return null;
    if (num < range.min) return `${range.label} muy baja (mín: ${range.min} ${range.unit})`;
    if (num > range.max) return `${range.label} muy alta (máx: ${range.max} ${range.unit})`;
    return null;
};

export const calcIMC = (pesoKg: number, alturaCm: number): number | null => {
    if (!pesoKg || !alturaCm || alturaCm < 30) return null;
    const alturaM = alturaCm / 100;
    return Math.round((pesoKg / (alturaM * alturaM)) * 10) / 10;
};

export const imcClassification = (imc: number): { label: string; color: string } => {
    if (imc < 18.5) return { label: 'Bajo peso', color: 'text-blue-500' };
    if (imc < 25) return { label: 'Normal', color: 'text-emerald-500' };
    if (imc < 30) return { label: 'Sobrepeso', color: 'text-amber-500' };
    if (imc < 35) return { label: 'Obesidad I', color: 'text-orange-500' };
    if (imc < 40) return { label: 'Obesidad II', color: 'text-red-500' };
    return { label: 'Obesidad III', color: 'text-red-700' };
};
