import { Outlet } from 'react-router-dom';

/**
 * Entry point del Módulo de Recepción.
 * Removido el Sidebar local porque ahora está centralizado en MainLayout.
 */
const ReceptionDashboard = () => {
    return <Outlet />;
};

export default ReceptionDashboard;
