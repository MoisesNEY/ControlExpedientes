// import React from 'react';

const VitalCard = ({ label, value, unit, extra, color = 'primary', showChart = false }: any) => {
    return (
        <div className="p-3 bg-slate-50 dark:bg-slate-800 rounded-lg border border-slate-100 dark:border-slate-700 transition-colors">
            <p className="text-[10px] font-bold text-slate-400 uppercase">{label}</p>
            <p className="text-lg font-bold text-slate-900 dark:text-white">
                {value} <span className="text-xs font-normal">{unit}</span>
            </p>
            {showChart ? (
                <div className={`h-6 mt-2 w-full bg-${color}/10 rounded flex items-center justify-center`}>
                    <div className="w-full px-2">
                        <svg className={`w-full h-full text-${color}`} preserveAspectRatio="none" viewBox="0 0 100 20">
                            <path
                                d="M0,15 L10,12 L20,14 L30,10 L40,11 L50,8 L60,10 L70,5 L80,7 L90,2 L100,4"
                                fill="none"
                                stroke="currentColor"
                                strokeLinecap="round"
                                strokeWidth="2"
                            />
                        </svg>
                    </div>
                </div>
            ) : (
                <div className={`mt-2 text-[10px] ${color === 'success' ? 'text-success font-medium' : 'text-slate-400'}`}>
                    {extra}
                </div>
            )}
        </div>
    );
};

const VitalsPanel = () => {
    return (
        <div className="p-6 transition-colors duration-300">
            <h3 className="text-slate-900 dark:text-white text-sm font-bold mb-4 flex items-center gap-2">
                <span className="material-symbols-outlined text-primary">monitoring</span>
                Signos Vitales
            </h3>
            <div className="grid grid-cols-2 gap-3">
                <VitalCard label="Peso" value="75.2" unit="kg" showChart color="primary" />
                <VitalCard label="Altura" value="1.75" unit="m" extra="IMC: 24.6 (Normal)" />
                <VitalCard label="Presión Art." value="120/80" unit="" showChart color="success" />
                <VitalCard label="Temperatura" value="36.6" unit="°C" extra="Estable" color="success" />
                <VitalCard label="Frec. Card." value="72" unit="bpm" showChart color="primary" />
                <VitalCard label="SPO2" value="98" unit="%" extra="Óptimo" color="success" />
            </div>
        </div>
    );
};

export default VitalsPanel;
