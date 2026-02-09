import React, { createContext, useContext, useEffect, useState } from 'react';
import keycloak from '../keycloak';

interface AuthContextType {
    isAuthenticated: boolean;
    login: () => void;
    logout: () => void;
    token: string | undefined;
    user: any;
    loading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState<any>(null);

    const isRun = React.useRef(false);

    useEffect(() => {
        if (isRun.current) return;
        isRun.current = true;

        keycloak
            .init({
                onLoad: 'check-sso',
                pkceMethod: 'S256',
                checkLoginIframe: false // Desactivar iframe para evitar loops en localhost
            })
            .then((authenticated) => {
                setIsAuthenticated(authenticated);
                if (authenticated) {
                    setUser(keycloak.tokenParsed);
                }
                setLoading(false);
            })
            .catch((err) => {
                console.error('Keycloak init error:', err);
                setLoading(false);
            });
    }, []);

    const login = () => keycloak.login();
    const logout = () => keycloak.logout({ redirectUri: window.location.origin });

    return (
        <AuthContext.Provider value={{
            isAuthenticated,
            login,
            logout,
            token: keycloak.token,
            user,
            loading
        }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within an AuthProvider');
    return context;
};
