import api from './api';

export interface UserAccount {
    id: string;
    login: string;
    firstName: string;
    lastName: string;
    email: string;
    imageUrl: string;
    activated: boolean;
    langKey: string;
    createdBy: string;
    createdDate: string;
    lastModifiedBy: string;
    lastModifiedDate: string;
    authorities: string[];
}

export interface PublicUser {
    id: string;
    login: string;
}

export const UserService = {
    getAccount: async (): Promise<UserAccount> => {
        const response = await api.get<UserAccount>('/api/account');
        return response.data;
    },

    getPublicUsers: async (params?: Record<string, any>): Promise<PublicUser[]> => {
        const response = await api.get<PublicUser[]>('/api/users', { params });
        return response.data;
    },

    getMedicos: async (params?: Record<string, any>): Promise<PublicUser[]> => {
        const response = await api.get<PublicUser[]>('/api/users/medicos', { params });
        return response.data;
    }
};
