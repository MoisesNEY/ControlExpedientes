import { useEffect, useMemo, useRef, useState } from 'react';

export interface SearchableSelectOption {
  value: string;
  label: string;
  description?: string;
  keywords?: string[];
}

interface SearchableSelectProps {
  value?: string;
  options: SearchableSelectOption[];
  placeholder: string;
  searchPlaceholder?: string;
  emptyMessage?: string;
  onChange: (value: string) => void;
}

const SearchableSelect = ({
  value,
  options,
  placeholder,
  searchPlaceholder = 'Buscar...',
  emptyMessage = 'No hay resultados para mostrar.',
  onChange,
}: SearchableSelectProps) => {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [isOpen, setIsOpen] = useState(false);
  const [query, setQuery] = useState('');

  const selectedOption = useMemo(() => options.find(option => option.value === value), [options, value]);
  const filteredOptions = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();
    if (!normalizedQuery) {
      return options;
    }

    return options.filter(option =>
      [option.label, option.description, ...(option.keywords ?? [])]
        .filter((part): part is string => Boolean(part))
        .some(part => part.toLowerCase().includes(normalizedQuery))
    );
  }, [options, query]);

  useEffect(() => {
    if (!isOpen) {
      setQuery('');
    }
  }, [isOpen]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div ref={containerRef} className="relative">
      <button
        type="button"
        onClick={() => setIsOpen(current => !current)}
        className="flex w-full items-center justify-between rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-left text-sm text-slate-900 outline-none transition focus:ring-2 focus:ring-primary dark:border-slate-700 dark:bg-slate-800 dark:text-white"
      >
        <div className="min-w-0">
          <p className={`truncate ${selectedOption ? 'font-medium' : 'text-slate-400 dark:text-slate-500'}`}>
            {selectedOption?.label ?? placeholder}
          </p>
          {selectedOption?.description && (
            <p className="mt-1 truncate text-xs text-slate-500 dark:text-slate-400">{selectedOption.description}</p>
          )}
        </div>
        <span className="material-symbols-outlined ml-3 text-base text-slate-400">
          {isOpen ? 'expand_less' : 'expand_more'}
        </span>
      </button>

      {isOpen && (
        <div className="absolute z-30 mt-2 w-full rounded-2xl border border-slate-200 bg-white p-3 shadow-2xl dark:border-slate-700 dark:bg-slate-900">
          <div className="relative">
            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-base text-slate-400">search</span>
            <input
              type="text"
              value={query}
              onChange={event => setQuery(event.target.value)}
              placeholder={searchPlaceholder}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 py-2.5 pl-10 pr-4 text-sm outline-none focus:ring-2 focus:ring-primary dark:border-slate-700 dark:bg-slate-800 dark:text-white"
              autoFocus
            />
          </div>

          <div className="mt-3 max-h-64 overflow-y-auto rounded-xl border border-slate-100 dark:border-slate-800">
            {filteredOptions.length > 0 ? (
              filteredOptions.map(option => (
                <button
                  key={option.value}
                  type="button"
                  onClick={() => {
                    onChange(option.value);
                    setIsOpen(false);
                  }}
                  className={`block w-full border-b border-slate-100 px-4 py-3 text-left transition last:border-b-0 dark:border-slate-800 ${
                    option.value === value
                      ? 'bg-primary/10 text-primary'
                      : 'hover:bg-slate-50 dark:hover:bg-slate-800'
                  }`}
                >
                  <p className="text-sm font-semibold">{option.label}</p>
                  {option.description && (
                    <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">{option.description}</p>
                  )}
                </button>
              ))
            ) : (
              <div className="px-4 py-6 text-center text-sm text-slate-500 dark:text-slate-400">{emptyMessage}</div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default SearchableSelect;
