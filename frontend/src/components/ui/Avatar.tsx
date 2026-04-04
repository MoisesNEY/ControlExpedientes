// import React from 'react';

type AvatarSize = 'sm' | 'md' | 'lg';

const sizeClass: Record<AvatarSize, string> = {
    sm: 'h-8 w-8 text-xs',
    md: 'h-10 w-10 text-sm',
    lg: 'h-14 w-14 text-base',
};

const colorClass = (seed: string) => {
    const colors = [
        'bg-blue-600',
        'bg-emerald-600',
        'bg-indigo-600',
        'bg-rose-600',
        'bg-amber-600',
        'bg-teal-600',
        'bg-violet-600',
        'bg-cyan-600',
    ];
    let hash = 0;
    for (let i = 0; i < seed.length; i++) hash = (hash * 31 + seed.charCodeAt(i)) >>> 0;
    return colors[hash % colors.length];
};

const initialsFromName = (name: string) => {
    const parts = name.trim().split(/\s+/).filter(Boolean);
    const initials = parts.slice(0, 2).map(p => p[0]?.toUpperCase()).join('');
    return initials || 'P';
};

export default function Avatar({
    name,
    size = 'md',
    className = '',
}: {
    name: string;
    size?: AvatarSize;
    className?: string;
}) {
    const initials = initialsFromName(name);
    const bg = colorClass(name);

    return (
        <div
            className={`${sizeClass[size]} ${bg} rounded-full ${className} flex items-center justify-center font-black text-white shadow-sm select-none`}
            aria-label={`Avatar de ${name}`}
            title={name}
        >
            {initials}
        </div>
    );
}

