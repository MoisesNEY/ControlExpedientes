import { Outlet } from 'react-router-dom';

/**
 * Entry point del Módulo de Enfermería.
 * Removido el Sidebar local porque ahora está centralizado en MainLayout.
 */
const NurseDashboard = () => {
    return <Outlet />;
};

export default NurseDashboard;
