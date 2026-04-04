import React from 'react';
import Avatar from './Avatar';

export interface PatientCardProps {
    id: string | number;
    fullName: string;
    avatarUrl?: string;
    subtitle?: string;
    cedula?: string;
    compact?: boolean;
}

export const PatientCard: React.FC<PatientCardProps> = ({
    id, fullName, subtitle, cedula, compact = false
}) => {
    
    if (compact) {
        return (
            <div className="flex items-center gap-3">
                <Avatar 
                    name={fullName} 
                    size="md" 
                    className="shadow-sm border border-slate-200 dark:border-slate-700"
                />
                <div className="flex flex-col">
                    <span className="text-sm font-bold text-slate-900 dark:text-white transition-colors">{fullName}</span>
                    <span className="text-xs text-slate-500 font-mono mt-0.5 tracking-tight">ID: {id.toString().padStart(6, '0')}</span>
                </div>
            </div>
        );
    }
    
    // Vista Full (Para Dashboards o Paneles del Paciente Lateral)
    return (
        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 flex items-center gap-4">
            <Avatar 
                name={fullName} 
                size="lg" 
                className="shadow-md ring-4 ring-slate-50 dark:ring-slate-800"
            />
            <div className="flex flex-col flex-1">
                <h3 className="text-lg font-black text-slate-900 dark:text-white leading-tight">{fullName}</h3>
                <div className="flex items-center gap-3 mt-1.5 opacity-80">
                    <p className="text-sm text-slate-500 dark:text-slate-400 font-medium flex items-center gap-1.5">
                        <span className="material-symbols-outlined text-[16px]">tag</span>
                        {id.toString().padStart(6, '0')}
                    </p>
                    {cedula && (
                        <>
                            <div className="w-1 h-1 bg-slate-300 dark:bg-slate-700 rounded-full"></div>
                            <p className="text-sm text-slate-500 dark:text-slate-400 font-medium flex items-center gap-1.5">
                                <span className="material-symbols-outlined text-[16px]">badge</span>
                                {cedula}
                            </p>
                        </>
                    )}
                </div>
                {subtitle && <p className="text-sm font-bold text-sky-600 dark:text-sky-400 mt-2">{subtitle}</p>}
            </div>
        </div>
    );
};
