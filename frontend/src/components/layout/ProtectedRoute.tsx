import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export const ProtectedRoute = ({ allowedRoles }: { allowedRoles?: string[] }) => {
    const { isAuthenticated, account, loading } = useAuth();

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen bg-slate-50 dark:bg-slate-900">
                <div className="flex flex-col items-center gap-4">
                    <div className="animate-spin rounded-full h-12 w-12 border-4 border-slate-200 border-t-primary"></div>
                    <span className="text-slate-500 font-medium">Autenticando...</span>
                </div>
            </div>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles && allowedRoles.length > 0) {
        const hasRole = allowedRoles.some(role => account?.authorities?.includes(role));
        if (!hasRole) {
            return <Navigate to="/unauthorized" replace />;
        }
    }

    return <Outlet />;
};
