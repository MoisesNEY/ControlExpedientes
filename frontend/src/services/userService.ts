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

export const UserService = {
    getAccount: async (): Promise<UserAccount> => {
        const response = await api.get<UserAccount>('/api/account');
        return response.data;
    }
};
