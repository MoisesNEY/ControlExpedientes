import axios from 'axios';
import keycloak from '../keycloak';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
    baseURL: API_URL,
});

api.interceptors.request.use(
    (config) => {
        if (keycloak.token) {
            config.headers.Authorization = `Bearer ${keycloak.token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Add response interceptor to handle 401 Unauthorized (token expired)
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401) {
            try {
                await keycloak.updateToken(30);
                // Retry the original request with the new token
                const originRequest = error.config;
                originRequest.headers.Authorization = `Bearer ${keycloak.token}`;
                return api(originRequest);
            } catch (refreshError) {
                console.error('Failed to refresh token:', refreshError);
                keycloak.login();
            }
        }
        return Promise.reject(error);
    }
);

export default api;
