const NurseDashboard = () => {
    return (
        <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col items-center justify-center p-8">
            <span className="material-symbols-outlined text-[80px] text-primary mb-6 opacity-80">vital_signs</span>
            <h1 className="text-4xl font-black text-slate-900 dark:text-white mb-4 text-center">Panel de Enfermería</h1>
            <p className="text-slate-500 text-lg text-center max-w-lg mb-8">
                Bienvenido al área de enfermería. Aquí podrá registrar signos vitales y gestionar el triaje de pacientes.
            </p>
            <div className="bg-primary/10 text-primary px-6 py-3 rounded-xl border border-primary/20 font-bold uppercase tracking-widest text-sm">
                Módulo en Construcción
            </div>
        </div>
    );
};

export default NurseDashboard;
