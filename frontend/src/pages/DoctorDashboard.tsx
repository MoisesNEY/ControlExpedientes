import { useState } from 'react';
import Sidebar from '../components/layout/Sidebar';
import DoctorHomeView from '../components/dashboard/views/DoctorHomeView';
import ConsultationView from '../components/dashboard/views/ConsultationView';
import PatientListView from '../components/dashboard/views/PatientListView';
import { usePatient } from '../context/PatientContext';
import VitalsPanel from '../components/dashboard/VitalsPanel';
import AppointmentList from '../components/dashboard/AppointmentList';

const DoctorDashboard = () => {
    const { selectedPatient } = usePatient();
    const [activeTab, setActiveTab] = useState('Panel Principal');

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
            default:
                return (
                    <div className="flex-1 flex flex-col items-center justify-center text-slate-400 bg-slate-50 dark:bg-slate-950 transition-colors">
                        <span className="material-symbols-outlined text-6xl mb-4 opacity-20">construction</span>
                        <p className="italic font-medium">La vista de "{activeTab}" está bajo construcción clínica...</p>
                    </div>
                );
        }
    };

    return (
        <div className="flex h-screen bg-slate-50 dark:bg-slate-950 transition-colors duration-300 overflow-hidden font-sans">
            <Sidebar onNavigate={setActiveTab} currentTab={activeTab} />

            <main className="flex-1 flex flex-col h-full overflow-y-auto relative custom-scrollbar">
                {renderContent()}
            </main>

            <aside className="w-80 bg-white dark:bg-slate-900 border-l border-slate-200 dark:border-slate-800 flex flex-col transition-colors duration-300 overflow-y-auto custom-scrollbar">
                {selectedPatient ? (
                    <>
                        <VitalsPanel />
                        <div className="mt-auto">
                            <AppointmentList />
                        </div>
                    </>
                ) : (
                    <AppointmentList />
                )}
            </aside>
        </div>
    );
};

export default DoctorDashboard;
