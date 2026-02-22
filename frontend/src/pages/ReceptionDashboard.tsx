const ReceptionDashboard = () => {
    return (
        <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col items-center justify-center p-8">
            <span className="material-symbols-outlined text-[80px] text-primary mb-6 opacity-80">support_agent</span>
            <h1 className="text-4xl font-black text-slate-900 dark:text-white mb-4 text-center">Centro de Recepción</h1>
            <p className="text-slate-500 text-lg text-center max-w-lg mb-8">
                Bienvenido a recepción. Aquí podrá registrar nuevos pacientes y agendar citas médicas.
            </p>
            <div className="bg-primary/10 text-primary px-6 py-3 rounded-xl border border-primary/20 font-bold uppercase tracking-widest text-sm">
                Módulo en Construcción
            </div>
        </div>
    );
};

export default ReceptionDashboard;
