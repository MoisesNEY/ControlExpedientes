import React, { type ButtonHTMLAttributes } from 'react';

export interface AppButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: 'primary' | 'secondary' | 'danger' | 'ghost' | 'outline';
    size?: 'sm' | 'md' | 'lg';
    icon?: string;
    isLoading?: boolean;
    fullWidth?: boolean;
}

const variantStyles = {
    primary: 'bg-sky-500 hover:bg-sky-600 text-white shadow-lg shadow-sky-500/30 hover:shadow-sky-500/50 border-transparent',
    secondary: 'bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700 border-transparent',
    danger: 'bg-rose-500 hover:bg-rose-600 text-white shadow-lg shadow-rose-500/30 hover:shadow-rose-500/50 border-transparent',
    ghost: 'bg-transparent text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-white/5 border-transparent',
    outline: 'bg-transparent border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800'
};

const sizeStyles = {
    sm: 'px-3 py-1.5 text-xs font-semibold rounded-lg gap-1.5',
    md: 'px-5 py-2.5 text-sm font-bold rounded-xl gap-2',
    lg: 'px-8 py-3.5 text-base font-black rounded-2xl gap-2.5'
};

const iconSizes = {
    sm: 'text-[16px]',
    md: 'text-[18px]',
    lg: 'text-[22px]'
};

export const AppButton: React.FC<AppButtonProps> = ({
    children,
    variant = 'primary',
    size = 'md',
    icon,
    isLoading = false,
    fullWidth = false,
    className = '',
    disabled,
    ...props
}) => {
    const baseStyle = 'inline-flex items-center justify-center transition-all duration-200 focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:-translate-y-0 disabled:hover:shadow-none hover:-translate-y-0.5 active:translate-y-0 active:scale-95 whitespace-nowrap';
    const widthStyle = fullWidth ? 'w-full' : '';

    return (
        <button
            className={`${baseStyle} ${variantStyles[variant]} ${sizeStyles[size]} ${widthStyle} ${className}`}
            disabled={disabled || isLoading}
            {...props}
        >
            {isLoading ? (
                <span className={`material-symbols-outlined animate-spin ${iconSizes[size]}`}>progress_activity</span>
            ) : icon ? (
                <span className={`material-symbols-outlined ${iconSizes[size]}`}>{icon}</span>
            ) : null}
            {children}
        </button>
    );
};
