import React from 'react';

interface PatientHeaderProps {
    name: string;
    id: string;
    age: string;
    gender: string;
    status: string;
    image: string;
}

const PatientHeader: React.FC<PatientHeaderProps> = ({ name, id, age, gender, status, image }) => {
    return (
        <header className="sticky top-0 z-10 bg-white/80 dark:bg-slate-900/80 backdrop-blur-md border-b border-slate-200 dark:border-slate-800 px-8 py-4 transition-colors duration-300">
            <div className="flex justify-between items-center max-w-5xl mx-auto w-full">
                <div className="flex gap-4 items-center">
                    <div className="h-14 w-14 rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden ring-2 ring-primary/20 shadow-inner">
                        <img src={image} alt={`Perfil de ${name}`} className="w-full h-full object-cover" />
                    </div>
                    <div className="flex flex-col">
                        <div className="flex items-center gap-3">
                            <h2 className="text-slate-900 dark:text-white text-xl font-bold tracking-tight">{name}</h2>
                            <span className="bg-primary/10 text-primary text-[10px] font-bold px-2 py-0.5 rounded-md uppercase tracking-wider border border-primary/20">
                                {id}
                            </span>
                        </div>
                        <p className="text-slate-500 text-sm font-medium">
                            {age === '45 yrs' ? '45 años' : age} • {gender === 'Male' ? 'Masculino' : gender} • Estado: <span className="text-success font-bold">{status === 'Active' ? 'Activo' : status}</span>
                        </p>
                    </div>
                </div>
                <div className="flex gap-2">
                    <button className="flex items-center gap-2 px-4 py-2 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 rounded-lg text-sm font-bold transition-all shadow-sm">
                        <span className="material-symbols-outlined text-[18px]">edit</span>
                        Editar Perfil
                    </button>
                </div>
            </div>
        </header>
    );
};

export default PatientHeader;
