import { useState } from 'react';
import AdminSidebar from '../components/admin/AdminSidebar';
import AdminHomeView from '../components/admin/views/AdminHomeView';
import AdminPacientesView from '../components/admin/views/AdminPacientesView';
import AdminMedicamentosView from '../components/admin/views/AdminMedicamentosView';
import AdminCitasView from '../components/admin/views/AdminCitasView';
import AdminExpedientesView from '../components/admin/views/AdminExpedientesView';
import AdminAuditoriaView from '../components/admin/views/AdminAuditoriaView';

const AdminDashboard = () => {
    const [activeTab, setActiveTab] = useState('Dashboard');
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    const renderContent = () => {
        switch (activeTab) {
            case 'Dashboard':
                return <AdminHomeView />;
            case 'Pacientes':
                return <AdminPacientesView />;
            case 'Medicamentos':
                return <AdminMedicamentosView />;
            case 'Citas':
                return <AdminCitasView />;
            case 'Expedientes':
                return <AdminExpedientesView />;
            case 'Auditoría':
                return <AdminAuditoriaView />;
            default:
                return <AdminHomeView />;
        }
    };

    return (
        <div className="flex flex-col lg:flex-row h-screen bg-slate-50 dark:bg-slate-950 transition-colors duration-300 overflow-hidden font-sans">
            {/* Cabecera Móvil */}
            <header className="lg:hidden flex items-center justify-between px-6 py-4 bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800 z-50">
                <div className="flex items-center gap-3">
                    <div className="w-8 h-8 bg-amber-600 rounded-lg flex items-center justify-center shadow-lg shadow-amber-600/30">
                        <span className="material-symbols-outlined text-white font-bold text-xl">admin_panel_settings</span>
                    </div>
                    <h1 className="text-slate-900 dark:text-white font-black text-sm uppercase tracking-tight">Admin Panel</h1>
                </div>
                <button
                    onClick={() => setIsSidebarOpen(!isSidebarOpen)}
                    className="p-2 text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition-colors"
                >
                    <span className="material-symbols-outlined">{isSidebarOpen ? 'close' : 'menu'}</span>
                </button>
            </header>

            {/* Sidebar con Mobile Toggle */}
            <div className={`
                fixed inset-0 z-40 lg:static lg:z-auto lg:block
                ${isSidebarOpen ? 'block' : 'hidden'}
            `}>
                <div
                    className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm lg:hidden"
                    onClick={() => setIsSidebarOpen(false)}
                />
                <AdminSidebar
                    onNavigate={(tab) => {
                        setActiveTab(tab);
                        setIsSidebarOpen(false);
                    }}
                    currentTab={activeTab}
                />
            </div>

            {/* Contenido Principal */}
            <main className="flex-1 flex flex-col min-w-0 h-full overflow-y-auto relative custom-scrollbar">
                {renderContent()}
            </main>
        </div>
    );
};

export default AdminDashboard;
