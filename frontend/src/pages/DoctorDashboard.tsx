import { useState } from 'react';
import Sidebar from '../components/layout/Sidebar';
import DoctorHomeView from '../components/dashboard/views/DoctorHomeView';
import ConsultationView from '../components/dashboard/views/ConsultationView';
import PatientListView from '../components/dashboard/views/PatientListView';
import AppointmentView from '../components/dashboard/views/AppointmentView';
import InventoryView from '../components/dashboard/views/InventoryView';
import RecordsView from '../components/dashboard/views/RecordsView';
import { usePatient } from '../context/PatientContext';
import VitalsPanel from '../components/dashboard/VitalsPanel';
import AppointmentList from '../components/dashboard/AppointmentList';

const DoctorDashboard = () => {
    const { selectedPatient } = usePatient();
    const [activeTab, setActiveTab] = useState('Panel Principal');
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    const renderContent = () => {
        // Si hay un paciente seleccionado, SIEMPRE mostramos la Consulta (Acto Clínico)
        if (selectedPatient) {
            return <ConsultationView />;
        }

        switch (activeTab) {
            case 'Panel Principal':
                return <DoctorHomeView />;
            case 'Pacientes':
                return <PatientListView />;
            case 'Citas':
                return <AppointmentView />;
            case 'Inventario':
                return <InventoryView />;
            case 'Registros':
                return <RecordsView />;
            default:
                return (
                    <div className="flex-1 flex flex-col items-center justify-center text-slate-400 bg-slate-50 dark:bg-slate-950 transition-colors p-8 text-center">
                        <span className="material-symbols-outlined text-6xl mb-4 opacity-20">construction</span>
                        <p className="italic font-medium">La vista de "{activeTab}" está bajo construcción clínica...</p>
                    </div>
                );
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

            {/* Panel Derecho (Vitals / Next Appointments) */}
            <aside className="flex w-full lg:w-80 bg-white dark:bg-slate-900 border-t lg:border-t-0 lg:border-l border-slate-200 dark:border-slate-800 flex-col transition-colors duration-300 overflow-y-auto custom-scrollbar">
                {selectedPatient ? (
                    <>
                        <VitalsPanel />
                        <div className="lg:mt-auto">
                            <AppointmentList onNavigate={setActiveTab} />
                        </div>
                    </>
                ) : (
                    <AppointmentList onNavigate={setActiveTab} />
                )}
            </aside>
        </div>
    );
};

export default DoctorDashboard;
