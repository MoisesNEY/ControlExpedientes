import React from 'react';
import Avatar from '../ui/Avatar';

interface PatientHeaderProps {
    name: string;
    id: string;
    age: string;
    gender: string;
    status: string;
    image?: string;
}

const PatientHeader: React.FC<PatientHeaderProps> = ({ name, id, age, gender, status, image }) => {
    return (
        <header className="sticky top-0 z-10 bg-white/80 dark:bg-slate-900/80 backdrop-blur-md border-b border-slate-200 dark:border-slate-800 px-4 md:px-8 py-3 md:py-4 transition-colors duration-300">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 max-w-5xl mx-auto w-full">
                <div className="flex gap-4 items-center w-full md:w-auto">
                    {image ? (
                        <div className="h-12 w-12 md:h-14 md:w-14 rounded-full bg-slate-200 dark:bg-slate-800 flex-shrink-0 overflow-hidden ring-2 ring-primary/20 shadow-inner">
                            <img src={image} alt={`Perfil de ${name}`} className="w-full h-full object-cover" />
                        </div>
                    ) : (
                        <Avatar name={name} size="lg" className="ring-2 ring-primary/20 shadow-inner" />
                    )}
                    <div className="flex flex-col min-w-0">
                        <div className="flex items-center gap-2 md:gap-3 flex-wrap">
                            <h2 className="text-slate-900 dark:text-white text-lg md:text-xl font-bold tracking-tight truncate">{name}</h2>
                            <span className="bg-primary/10 text-primary text-[9px] md:text-[10px] font-bold px-2 py-0.5 rounded-md uppercase tracking-wider border border-primary/20">
                                {id}
                            </span>
                        </div>
                        <p className="text-slate-500 text-xs md:text-sm font-medium truncate">
                            {(age && age !== 'N/A' ? age : '—')} • {(gender && gender !== 'N/A' ? gender : '—')} •{' '}
                            <span className="text-success font-bold">{status && status !== 'N/A' ? status : '—'}</span>
                        </p>
                    </div>
                </div>
                <div className="flex gap-2 w-full md:w-auto">
                    <button className="flex-1 md:flex-none flex items-center justify-center gap-2 px-4 py-2 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 rounded-lg text-xs md:text-sm font-bold transition-all shadow-sm">
                        <span className="material-symbols-outlined text-[16px] md:text-[18px]">edit</span>
                        Editar Perfil
                    </button>
                </div>
            </div>
        </header>
    );
};

export default PatientHeader;
