import { Outlet } from 'react-router-dom';

/**
 * Entry point del Módulo Administrativo.
 * Removido el Sidebar local porque ahora está centralizado en MainLayout.
 */
const AdminDashboard = () => {
    return <Outlet />;
};

export default AdminDashboard;
