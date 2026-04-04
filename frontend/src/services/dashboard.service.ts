import api from './api';

export interface DashboardMetricCard {
    key: string;
    label: string;
    value: number;
    helperText?: string;
}

export interface DashboardSeriesPoint {
    label: string;
    value: number;
    secondaryValue?: number | null;
    tertiaryValue?: number | null;
}

export interface DashboardListItem {
    id?: number | null;
    title: string;
    subtitle?: string | null;
    status?: string | null;
    timestamp?: string | null;
    meta?: string | null;
}

export interface DashboardActivityItem {
    id?: number | null;
    title: string;
    subtitle?: string | null;
    timestamp?: string | null;
    action?: string | null;
}

export interface DashboardMetrics {
    cards: DashboardMetricCard[];
    primarySeries: DashboardSeriesPoint[];
    secondarySeries: DashboardSeriesPoint[];
    tertiarySeries: DashboardSeriesPoint[];
    queue: DashboardListItem[];
    activity: DashboardActivityItem[];
    spotlight?: DashboardListItem | null;
}

export const DashboardService = {
    getAdminDashboard: async (): Promise<DashboardMetrics> => (await api.get('/api/dashboard/admin')).data,
    getDoctorDashboard: async (): Promise<DashboardMetrics> => (await api.get('/api/dashboard/doctor')).data,
    getNurseDashboard: async (): Promise<DashboardMetrics> => (await api.get('/api/dashboard/nurse')).data,
    getReceptionDashboard: async (): Promise<DashboardMetrics> => (await api.get('/api/dashboard/reception')).data,
};