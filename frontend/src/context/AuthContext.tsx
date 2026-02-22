import React, { createContext, useContext, useEffect, useState } from 'react';
import api from '../services/api';

export interface UserAccount {
    login: string;
    firstName: string;
    lastName: string;
    email: string;
    authorities: string[];
}

interface AuthContextType {
    isAuthenticated: boolean;
    login: (username: string, password: string) => Promise<{ success: boolean; error?: string }>;
    logout: () => Promise<void>;
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

    // On mount, check if there's an active session by calling GET /api/account
    useEffect(() => {
        checkSession();
    }, []);

    const checkSession = async () => {
        try {
            const response = await api.get('/api/account');
            setAccount(response.data);
            setUser(response.data);
            setIsAuthenticated(true);
        } catch {
            // No active session (401) — user needs to log in
            setIsAuthenticated(false);
            setAccount(null);
            setUser(null);
        } finally {
            setLoading(false);
        }
    };

    const login = async (username: string, password: string): Promise<{ success: boolean; error?: string }> => {
        try {
            const response = await api.post('/api/authenticate', { username, password });
            setAccount(response.data);
            setUser(response.data);
            setIsAuthenticated(true);
            return { success: true };
        } catch (error: any) {
            const message = error.response?.data?.detail || error.response?.data?.error || 'Error de autenticación';
            return { success: false, error: message };
        }
    };

    const logout = async () => {
        try {
            await api.post('/api/logout');
        } catch {
            // Ignore errors on logout
        }
        setIsAuthenticated(false);
        setAccount(null);
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{
            isAuthenticated,
            login,
            logout,
            token: undefined, // No token exposed to frontend in BFF pattern
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
