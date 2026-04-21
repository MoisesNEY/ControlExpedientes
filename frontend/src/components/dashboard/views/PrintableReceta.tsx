import type { Appointment } from '../../../services/appointment.service';
import { buildFullName } from '../../../utils/personName';

interface Prescription {
    medicamento: { nombre: string; descripcion?: string };
    dosis: string;
    frecuencia: string;
    duracion: string;
}

interface Diagnostico {
    id: number;
    codigoCIE: string;
    descripcion: string;
}

interface PrintableRecetaProps {
    appointment: Appointment;
    diagnosis: Diagnostico;
    prescriptions: Prescription[];
    notasMedicas: string;
    doctorName: string;
}

/**
 * Componente de Receta Médica imprimible.
 * Se renderiza oculto en el DOM (clase 'print-only') y solo aparece al imprimir.
 * La lógica de mostrar/ocultar se maneja via CSS @media print en index.css o global styles.
 */
const PrintableReceta = ({
    appointment,
    diagnosis,
    prescriptions,
    notasMedicas,
    doctorName,
}: PrintableRecetaProps) => {
    const today = new Date().toLocaleDateString('es-NI', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
    });

    const patientName = buildFullName([appointment.paciente?.nombres, appointment.paciente?.apellidos], 'Paciente');
    const patientId = appointment.paciente?.id ? `PAC-${String(appointment.paciente.id).padStart(4, '0')}` : 'N/D';

    return (
        <div id="printable-receta" className="hidden print:block print:fixed print:inset-0 print:bg-white print:z-[9999] print:p-0 print:m-0">
            {/* Hoja A4 simulada */}
            <div style={{
                fontFamily: "'Segoe UI', Arial, sans-serif",
                maxWidth: '210mm',
                minHeight: '297mm',
                margin: '0 auto',
                padding: '20mm 18mm',
                color: '#1a1a2e',
                display: 'flex',
                flexDirection: 'column',
                gap: '0',
            }}>
                {/* ===== ENCABEZADO ===== */}
                <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'flex-start',
                    borderBottom: '3px solid #0a6e75',
                    paddingBottom: '14px',
                    marginBottom: '18px',
                }}>
                    <div>
                        {/* Logo textual de la clínica */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <div style={{
                                width: '46px', height: '46px',
                                background: 'linear-gradient(135deg, #0a6e75, #0ea5b8)',
                                borderRadius: '10px',
                                display: 'flex', alignItems: 'center', justifyContent: 'center',
                                color: '#fff', fontWeight: 900, fontSize: '18px', flexShrink: 0,
                            }}>
                                ✚
                            </div>
                            <div>
                                <div style={{ fontSize: '20px', fontWeight: 900, color: '#0a6e75', lineHeight: 1 }}>
                                    STITCH Medical Center
                                </div>
                                <div style={{ fontSize: '11px', color: '#666', marginTop: '2px' }}>
                                    Atención Médica Integral y de Calidad
                                </div>
                            </div>
                        </div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                        <div style={{
                            fontSize: '13px', fontWeight: 700,
                            color: '#0a6e75', textTransform: 'uppercase', letterSpacing: '1px',
                        }}>
                            Receta Médica
                        </div>
                        <div style={{ fontSize: '11px', color: '#888', marginTop: '4px' }}>Fecha: {today}</div>
                        <div style={{ fontSize: '11px', color: '#888' }}>Cita #: {appointment.id}</div>
                    </div>
                </div>

                {/* ===== DATOS DEL PACIENTE ===== */}
                <div style={{
                    background: '#f3fbfc',
                    border: '1px solid #b2e4e9',
                    borderRadius: '8px',
                    padding: '12px 16px',
                    marginBottom: '18px',
                    display: 'grid',
                    gridTemplateColumns: '1fr 1fr',
                    gap: '6px 24px',
                }}>
                    <div>
                        <span style={{ fontSize: '9px', fontWeight: 700, color: '#0a6e75', textTransform: 'uppercase', letterSpacing: '0.8px' }}>Paciente</span>
                        <div style={{ fontSize: '14px', fontWeight: 700, color: '#1a1a2e' }}>{patientName}</div>
                    </div>
                    <div>
                        <span style={{ fontSize: '9px', fontWeight: 700, color: '#0a6e75', textTransform: 'uppercase', letterSpacing: '0.8px' }}>ID</span>
                        <div style={{ fontSize: '13px', fontWeight: 600, color: '#1a1a2e' }}>{patientId}</div>
                    </div>
                    <div>
                        <span style={{ fontSize: '9px', fontWeight: 700, color: '#0a6e75', textTransform: 'uppercase', letterSpacing: '0.8px' }}>Médico Tratante</span>
                        <div style={{ fontSize: '13px', fontWeight: 600, color: '#1a1a2e' }}>{doctorName}</div>
                    </div>
                    <div>
                        <span style={{ fontSize: '9px', fontWeight: 700, color: '#0a6e75', textTransform: 'uppercase', letterSpacing: '0.8px' }}>Motivo de Consulta</span>
                        <div style={{ fontSize: '12px', color: '#444' }}>{appointment.observaciones || 'Consulta médica general'}</div>
                    </div>
                </div>

                {/* ===== DIAGNÓSTICO ===== */}
                <div style={{ marginBottom: '20px' }}>
                    <div style={{
                        fontSize: '10px', fontWeight: 700, color: '#0a6e75',
                        textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '8px',
                        display: 'flex', alignItems: 'center', gap: '6px',
                    }}>
                        <span style={{ width: '3px', height: '14px', background: '#0a6e75', borderRadius: '2px', display: 'inline-block' }}></span>
                        Diagnóstico Principal
                    </div>
                    <div style={{
                        display: 'flex', alignItems: 'center', gap: '10px',
                        padding: '10px 14px',
                        background: '#fff',
                        border: '1.5px solid #0a6e75',
                        borderRadius: '8px',
                    }}>
                        <div style={{
                            background: '#0a6e75', color: '#fff',
                            padding: '4px 10px', borderRadius: '5px',
                            fontSize: '11px', fontWeight: 800, flexShrink: 0,
                        }}>
                            {diagnosis.codigoCIE}
                        </div>
                        <div style={{ fontSize: '13px', fontWeight: 600, color: '#1a1a2e' }}>
                            {diagnosis.descripcion}
                        </div>
                    </div>
                </div>

                {/* ===== NOTAS MÉDICAS ===== */}
                {notasMedicas && (
                    <div style={{ marginBottom: '20px' }}>
                        <div style={{
                            fontSize: '10px', fontWeight: 700, color: '#0a6e75',
                            textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '8px',
                            display: 'flex', alignItems: 'center', gap: '6px',
                        }}>
                            <span style={{ width: '3px', height: '14px', background: '#0a6e75', borderRadius: '2px', display: 'inline-block' }}></span>
                            Notas Clínicas
                        </div>
                        <div style={{
                            padding: '10px 14px',
                            background: '#fafafa',
                            border: '1px solid #e0e0e0',
                            borderRadius: '8px',
                            fontSize: '12px', color: '#333', lineHeight: 1.6,
                            fontStyle: 'italic',
                        }}>
                            {notasMedicas}
                        </div>
                    </div>
                )}

                {/* ===== MEDICAMENTOS ===== */}
                <div style={{ marginBottom: '24px', flex: 1 }}>
                    <div style={{
                        fontSize: '10px', fontWeight: 700, color: '#0a6e75',
                        textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '8px',
                        display: 'flex', alignItems: 'center', gap: '6px',
                    }}>
                        <span style={{ width: '3px', height: '14px', background: '#0ea5b8', borderRadius: '2px', display: 'inline-block' }}></span>
                        Tratamiento Farmacológico
                    </div>

                    {prescriptions.length === 0 ? (
                        <p style={{ fontSize: '12px', color: '#888', fontStyle: 'italic' }}>
                            Sin medicamentos prescritos en esta consulta.
                        </p>
                    ) : (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                            {prescriptions.map((rx, idx) => (
                                <div key={idx} style={{
                                    display: 'flex',
                                    padding: '10px 14px',
                                    background: '#fff',
                                    border: '1px solid #ddd',
                                    borderLeft: '4px solid #0ea5b8',
                                    borderRadius: '6px',
                                    alignItems: 'flex-start',
                                    gap: '12px',
                                }}>
                                    <div style={{
                                        minWidth: '24px', height: '24px',
                                        background: '#0ea5b8', color: '#fff',
                                        borderRadius: '50%', display: 'flex',
                                        alignItems: 'center', justifyContent: 'center',
                                        fontSize: '11px', fontWeight: 800, flexShrink: 0,
                                    }}>
                                        {idx + 1}
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <div style={{ fontSize: '13px', fontWeight: 700, color: '#1a1a2e' }}>
                                            {rx.medicamento.nombre}
                                        </div>
                                        <div style={{ fontSize: '11px', color: '#555', marginTop: '4px' }}>
                                            <span style={{ marginRight: '14px' }}>💊 <b>Dosis:</b> {rx.dosis}</span>
                                            <span style={{ marginRight: '14px' }}>🕐 <b>Frecuencia:</b> {rx.frecuencia}</span>
                                            <span>📅 <b>Duración:</b> {rx.duracion}</span>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* ===== PIE DE FIRMA ===== */}
                <div style={{
                    borderTop: '1.5px solid #e0e0e0',
                    paddingTop: '20px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'flex-end',
                    marginTop: 'auto',
                }}>
                    <div style={{ fontSize: '10px', color: '#aaa', lineHeight: 1.7 }}>
                        <div>Este documento es válido únicamente con firma y sello del médico.</div>
                        <div>Generado por Sistema Control de Expedientes — STITCH Medical Center</div>
                    </div>
                    <div style={{ textAlign: 'center', minWidth: '160px' }}>
                        <div style={{
                            borderTop: '1.5px solid #1a1a2e',
                            paddingTop: '8px',
                            fontSize: '11px', color: '#333', fontWeight: 600,
                        }}>
                            {doctorName}
                        </div>
                        <div style={{ fontSize: '10px', color: '#888' }}>Firma y Sello del Médico</div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PrintableReceta;
