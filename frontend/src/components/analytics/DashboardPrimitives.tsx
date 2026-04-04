import type { ReactNode } from 'react';

export const DashboardMetricCard = ({
    label,
    value,
    helperText,
    icon,
    accent,
}: {
    label: string;
    value: number | string;
    helperText?: string;
    icon: string;
    accent: string;
}) => (
    <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 flex items-center gap-4 transition-all hover:shadow-md">
        <div className={`size-14 rounded-2xl flex items-center justify-center text-white shadow-lg ${accent}`}>
            <span className="material-symbols-outlined text-[24px]">{icon}</span>
        </div>
        <div>
            <p className="text-slate-500 dark:text-slate-400 text-xs font-bold uppercase tracking-wider">{label}</p>
            <p className="text-3xl font-black text-slate-900 dark:text-white leading-none mt-1">{value}</p>
            {helperText && <p className="text-[11px] text-slate-400 mt-1">{helperText}</p>}
        </div>
    </div>
);

export const DashboardPanel = ({ title, icon, children, actions }: { title: string; icon: string; children: ReactNode; actions?: ReactNode }) => (
    <div className="bg-white dark:bg-slate-900 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 p-6">
        <div className="flex items-center justify-between gap-4 mb-4">
            <h3 className="font-bold text-slate-900 dark:text-white flex items-center gap-2">
                <span className="material-symbols-outlined text-sky-500">{icon}</span>
                {title}
            </h3>
            {actions}
        </div>
        {children}
    </div>
);

export const DashboardEmptyState = ({ message }: { message: string }) => (
    <div className="flex flex-col items-center justify-center h-[260px] text-slate-400">
        <span className="material-symbols-outlined text-5xl mb-3">monitoring</span>
        <p className="text-sm font-medium">{message}</p>
    </div>
);

export const DashboardLoading = () => (
    <div className="p-8 flex items-center justify-center h-full min-h-[320px]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-sky-500"></div>
    </div>
);