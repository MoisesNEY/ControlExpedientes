import React, { useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Navigate } from 'react-router-dom';

const Login = () => {
    const { isAuthenticated, login } = useAuth();
    const isLoginStarted = React.useRef(false);

    useEffect(() => {
        if (!isAuthenticated && !isLoginStarted.current) {
            isLoginStarted.current = true;
            console.log('Iniciando redirección a Keycloak...');
            login();
        }
    }, [isAuthenticated, login]);

    if (isAuthenticated) {
        return <Navigate to="/doctor" replace />;
    }

    return (
        <div className="flex items-center justify-center min-h-screen bg-slate-50">
            <div className="text-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                <p className="text-slate-600 font-medium tracking-tight">Redirigiendo al inicio de sesión seguro...</p>
            </div>
        </div>
    );
};

export default Login;
