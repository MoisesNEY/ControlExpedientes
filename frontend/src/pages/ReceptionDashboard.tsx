import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ReceptionDashboard = () => {
    const { logout, account } = useAuth();
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();

    const isActive = (path: string) => location.pathname.endsWith(path);

    return (
        <div className="flex w-full h-screen bg-slate-50 dark:bg-slate-950 font-sans text-slate-900 dark:text-slate-100 overflow-hidden">
            {/* Sidebar */}
            <aside className={`
                fixed inset-y-0 left-0 z-50 w-64 bg-slate-900 text-slate-300 flex flex-col 
                transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static
                ${isSidebarOpen ? 'translate-x-0' : '-translate-x-full'}
            `}>
                <div className="p-6 flex items-center gap-3 border-b border-white/10">
                    <div className="w-10 h-10 bg-teal-500 rounded-xl flex items-center justify-center shadow-lg shadow-teal-500/30">
                        <span className="material-symbols-outlined font-bold text-white">support_agent</span>
                    </div>
                    <div>
                        <h1 className="text-white font-black leading-tight uppercase tracking-tight text-sm">Vitalis</h1>
                        <p className="text-[10px] font-bold uppercase tracking-widest text-teal-400">Recepción</p>
                    </div>
                    <button onClick={() => setIsSidebarOpen(false)} className="ml-auto text-slate-400 hover:text-white lg:hidden">
                        <span className="material-symbols-outlined">close</span>
                    </button>
                </div>

                <div className="p-4">
                    <div className="mb-6 flex items-center gap-3 rounded-xl border border-white/5 bg-white/5 p-3">
                        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-teal-500/20 font-bold text-teal-400">
                            {account?.firstName?.[0] || 'R'}
                        </div>
                        <div className="overflow-hidden">
                            <p className="truncate text-sm font-bold text-white">{account?.firstName} {account?.lastName}</p>
                            <p className="text-[10px] uppercase tracking-widest text-slate-400">{account?.login}</p>
                        </div>
                    </div>

                    <nav className="space-y-1">
                        <button onClick={() => { navigate('pacientes'); setIsSidebarOpen(false); }} className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold transition-all ${isActive('pacientes') || isActive('recepcion') ? 'bg-teal-500 text-white shadow-md shadow-teal-500/20' : 'hover:bg-white/5 hover:text-white'}`}>
                            <span className="material-symbols-outlined">groups</span> Admisión
                        </button>
                        <button onClick={() => { navigate('citas'); setIsSidebarOpen(false); }} className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold transition-all ${isActive('citas') ? 'bg-teal-500 text-white shadow-md shadow-teal-500/20' : 'hover:bg-white/5 hover:text-white'}`}>
                            <span className="material-symbols-outlined">calendar_month</span> Agenda
                        </button>
                    </nav>
                </div>

                <div className="mt-auto border-t border-white/10 p-4">
                    <button onClick={logout} className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-bold text-red-400 transition-colors hover:bg-red-500/10 hover:text-red-300">
                        <span className="material-symbols-outlined">logout</span> Cerrar Sesión
                    </button>
                </div>
            </aside>

            {/* Mobile overlay */}
            {isSidebarOpen && (
                <div className="fixed inset-0 z-40 backdrop-blur-sm lg:hidden bg-slate-900/50" onClick={() => setIsSidebarOpen(false)} />
            )}

            {/* Main Content */}
            <main className="flex-1 flex min-w-0 flex-col bg-slate-50 dark:bg-slate-950">
                <header className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-4 dark:border-slate-800 dark:bg-slate-900 lg:hidden">
                    <div className="flex items-center gap-2">
                        <button onClick={() => setIsSidebarOpen(true)} className="-ml-2 rounded-lg p-2 text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800">
                            <span className="material-symbols-outlined">menu</span>
                        </button>
                        <h2 className="text-sm font-black uppercase tracking-tight text-slate-900 dark:text-white">Recepción</h2>
                    </div>
                </header>

                <div className="custom-scrollbar flex-1 overflow-y-auto">
                    <Outlet />
                </div>
            </main>
        </div>
    );
};

export default ReceptionDashboard;
