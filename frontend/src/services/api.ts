import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
    baseURL: API_URL,
    withCredentials: true, // Always send session cookie (JSESSIONID)
});

// No automatic redirect on 401 — AuthContext handles authentication state.
// Individual components should check isAuthenticated and redirect as needed.

export default api;
