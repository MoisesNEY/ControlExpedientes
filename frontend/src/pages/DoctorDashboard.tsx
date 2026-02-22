import { useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import Sidebar from '../components/layout/Sidebar';
// Views are no longer directly imported here, they will be provided via Outlet
import { usePatient } from '../context/PatientContext';
import VitalsPanel from '../components/dashboard/VitalsPanel';
import AppointmentList from '../components/dashboard/AppointmentList';

const DoctorDashboard = () => {
    const { selectedPatient } = usePatient();
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);
    const navigate = useNavigate();

    // Mapping from Tab Label to relative path
    const handleNavigate = (tab: string) => {
        setIsSidebarOpen(false);
        switch (tab) {
            case 'Pacientes': navigate('pacientes'); break;
            case 'Citas': navigate('citas'); break;
            case 'Inventario': navigate('inventario'); break;
            case 'Registros': navigate('registros'); break;
            case 'Panel Principal':
            default: navigate(''); break;
        }
    };

    return (
        <div className="flex flex-col lg:flex-row h-screen bg-slate-50 dark:bg-slate-950 transition-colors duration-300 overflow-hidden font-sans">
            {/* Cabecera Móvil */}
            <header className="lg:hidden flex items-center justify-between px-6 py-4 bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800 z-50">
                <div className="flex items-center gap-3">
                    <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center shadow-lg shadow-primary/30">
                        <span className="material-symbols-outlined text-white font-bold text-xl">medical_services</span>
                    </div>
                    <h1 className="text-slate-900 dark:text-white font-black text-sm uppercase tracking-tight">Stitch</h1>
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
                {/* Backdrop para móvil */}
                <div
                    className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm lg:hidden"
                    onClick={() => setIsSidebarOpen(false)}
                />

                <Sidebar
                    onNavigate={handleNavigate}
                />
            </div>

            {/* Contenido Principal */}
            <main className="flex-1 flex flex-col min-w-0 h-full overflow-y-auto relative custom-scrollbar">
                <Outlet />
            </main>

            {/* Panel Derecho (Vitals / Next Appointments) */}
            <aside className="flex w-full lg:w-80 bg-white dark:bg-slate-900 border-t lg:border-t-0 lg:border-l border-slate-200 dark:border-slate-800 flex-col transition-colors duration-300 overflow-y-auto custom-scrollbar">
                {selectedPatient ? (
                    <>
                        <VitalsPanel />
                        <div className="lg:mt-auto">
                            <AppointmentList onNavigate={handleNavigate} />
                        </div>
                    </>
                ) : (
                    <AppointmentList onNavigate={handleNavigate} />
                )}
            </aside>
        </div >
    );
};

export default DoctorDashboard;
