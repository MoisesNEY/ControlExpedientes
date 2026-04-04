import { type ReactNode } from 'react';

export interface Column<T> {
    header: string;
    accessorKey?: keyof T;
    id?: string;
    cell?: (item: T) => ReactNode;
    className?: string;
}

interface DataGridProps<T> {
    data: T[];
    columns: Column<T>[];
    keyExtractor: (item: T) => string | number;
    isLoading?: boolean;
    emptyMessage?: {
        title: string;
        description: string;
        icon?: string;
    };
    onRowClick?: (item: T) => void;
}

export function DataGrid<T>({
    data,
    columns,
    keyExtractor,
    isLoading = false,
    emptyMessage = { title: 'No hay datos', description: 'No se encontraron resultados.', icon: 'info' },
    onRowClick,
}: DataGridProps<T>) {
    
    if (isLoading && data.length === 0) {
        return (
            <div className="p-8 flex items-center justify-center w-full min-h-[300px]">
                <div className="flex flex-col items-center gap-4">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-sky-500"></div>
                    <p className="text-slate-500 font-medium animate-pulse">Cargando información...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="w-full overflow-x-auto rounded-xl">
            <table className="w-full text-left min-w-[800px] border-collapse">
                <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 uppercase text-[10px] font-black tracking-widest border-b border-slate-200 dark:border-slate-800/80">
                    <tr>
                        {columns.map((col, idx) => (
                            <th key={col.id || col.accessorKey?.toString() || idx} className={`px-6 py-4 ${col.className || ''}`}>
                                {col.header}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800/60 transition-all">
                    {data.length > 0 ? (
                        data.map((row) => (
                            <tr 
                                key={keyExtractor(row)} 
                                onClick={() => onRowClick && onRowClick(row)}
                                className={`group transition-colors ${onRowClick ? 'cursor-pointer hover:bg-slate-50/80 dark:hover:bg-slate-800/40' : 'hover:bg-slate-50/40 dark:hover:bg-slate-800/20'}`}
                            >
                                {columns.map((col, idx) => (
                                    <td key={col.id || col.accessorKey?.toString() || idx} className="px-6 py-5">
                                        {col.cell 
                                            ? col.cell(row) 
                                            : col.accessorKey 
                                                ? (row[col.accessorKey] as ReactNode) 
                                                : null}
                                    </td>
                                ))}
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan={columns.length} className="px-6 py-20 text-center">
                                <div className="flex flex-col items-center justify-center gap-4 fade-in slide-in-from-bottom-2 duration-500">
                                    <div className="h-20 w-20 bg-slate-100 dark:bg-slate-800 rounded-full flex items-center justify-center shadow-inner">
                                        <span className="material-symbols-outlined text-4xl text-slate-400">
                                            {emptyMessage.icon}
                                        </span>
                                    </div>
                                    <div className="flex flex-col gap-1 text-center">
                                        <p className="text-slate-900 dark:text-white font-bold text-lg">{emptyMessage.title}</p>
                                        <p className="text-slate-500 max-w-sm">{emptyMessage.description}</p>
                                    </div>
                                </div>
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
}
