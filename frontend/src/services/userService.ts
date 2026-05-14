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
    permissions?: string[];
}

export interface AccountProfileUpdatePayload {
    firstName: string;
    lastName: string;
    email: string;
}

export interface AccountPasswordChangePayload {
    currentPassword: string;
    newPassword: string;
    confirmationPassword: string;
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

    updateAccount: async (payload: AccountProfileUpdatePayload): Promise<UserAccount> => {
        const response = await api.put<UserAccount>('/api/account', payload);
        return response.data;
    },

    changePassword: async (payload: AccountPasswordChangePayload): Promise<void> => {
        await api.post('/api/account/change-password', payload);
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
