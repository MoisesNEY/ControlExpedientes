import React, { createContext, useContext, useEffect, useState } from 'react';
import keycloak from '../keycloak';
import { UserService, type UserAccount } from '../services/userService';

interface AuthContextType {
    isAuthenticated: boolean;
    login: () => void;
    logout: () => void;
    token: string | undefined;
    user: any;
    account: UserAccount | null;
    loading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState<any>(null);
    const [account, setAccount] = useState<UserAccount | null>(null);

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
                    // Esperamos a que getAccount termine ANTES de quitar el loading screen
                    // para evitar el flash hacia "Unauthorized" mientras Account aún es null
                    UserService.getAccount()
                        .then(setAccount)
                        .catch(error => {
                            console.error('Error fetching account data:', error);
                        })
                        .finally(() => {
                            setLoading(false);
                        });
                } else {
                    setLoading(false);
                }
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
            account,
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
