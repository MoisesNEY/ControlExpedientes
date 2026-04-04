import { Outlet } from 'react-router-dom';

/**
 * Entry point del Módulo Médico.
 * Removido el Sidebar local porque ahora está centralizado en MainLayout.
 */
const DoctorDashboard = () => {
    return <Outlet />;
};

export default DoctorDashboard;
