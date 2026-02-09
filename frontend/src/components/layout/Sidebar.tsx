import { usePatient } from '../../context/PatientContext';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';

interface SidebarProps {
    onNavigate?: (tab: string) => void;
    currentTab?: string;
}

const Sidebar = ({ onNavigate, currentTab }: SidebarProps) => {
    const { selectPatient, selectedPatient } = usePatient();
    const { logout, account, user } = useAuth();
    const { theme, toggleTheme } = useTheme();

    const handleNavClick = (label: string) => {
        if (label === 'Panel Principal') {
            selectPatient(null);
        }

        if (onNavigate) {
            onNavigate(label);
        }
    };

    const menuItems = [
        { icon: 'dashboard', label: 'Panel Principal' },
        { icon: 'group', label: 'Pacientes' },
        { icon: 'calendar_today', label: 'Citas' },
        { icon: 'inventory_2', label: 'Inventario' },
        { icon: 'history_edu', label: 'Registros' },
    ];

    const getSafeName = () => {
        // 1. Try account fields
        if (account?.firstName || account?.lastName) {
            return `${account.firstName || ''} ${account.lastName || ''}`.trim();
        }
        // 2. Try token name
        if (user?.name) return user.name;
        // 3. Try token components
        if (user?.given_name || user?.family_name) {
            return `${user.given_name || ''} ${user.family_name || ''}`.trim();
        }
        // 4. Try username
        if (user?.preferred_username) return user.preferred_username;
        // 5. Default
        return 'Doctor';
    };

    const fullName = getSafeName();

    const initials = fullName
        .split(' ')
        .filter(Boolean)
        .map((n: string) => n[0])
        .slice(0, 2)
        .join('')
        .toUpperCase() || 'DR';

    return (
        <div className="w-64 bg-white dark:bg-slate-900 border-r border-slate-200 dark:border-slate-800 flex flex-col h-full transition-colors duration-300">
            <div className="p-6">
                <div className="flex items-center gap-3 mb-8">
                    <div className="w-10 h-10 bg-primary rounded-xl flex items-center justify-center shadow-lg shadow-primary/30">
                        <span className="material-symbols-outlined text-white font-bold text-2xl">medical_services</span>
                    </div>
                    <div>
                        <h1 className="text-slate-900 dark:text-white font-black text-lg leading-tight tracking-tight uppercase">Stitch</h1>
                        <p className="text-[10px] text-primary font-black uppercase tracking-widest leading-none">Medical Center</p>
                    </div>
                </div>

                <nav className="space-y-1.5">
                    {menuItems.map((item) => {
                        const isActive = labelToActive(item.label, currentTab, selectedPatient);
                        return (
                            <button
                                key={item.label}
                                onClick={() => handleNavClick(item.label)}
                                className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 group ${isActive
                                    ? 'bg-primary text-white shadow-lg shadow-primary/20'
                                    : 'text-slate-500 hover:bg-primary/5 hover:text-primary'
                                    }`}
                            >
                                <span className={`material-symbols-outlined ${isActive ? 'fill-1' : 'opacity-80 group-hover:opacity-100'}`}>
                                    {item.icon}
                                </span>
                                <span className="text-sm font-bold tracking-tight">{item.label}</span>
                            </button>
                        );
                    })}
                </nav>
            </div>

            <div className="mt-auto p-6 space-y-4">
                {/* Theme Toggle */}
                <button
                    onClick={toggleTheme}
                    className="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 transition-all font-bold group"
                >
                    <span className="material-symbols-outlined transition-transform duration-300 group-hover:rotate-12">
                        {theme === 'light' ? 'dark_mode' : 'light_mode'}
                    </span>
                    <span className="text-sm">{theme === 'light' ? 'Modo Oscuro' : 'Modo Claro'}</span>
                </button>

                {/* User Info */}
                <div className="p-4 bg-slate-50 dark:bg-slate-800/50 rounded-2xl border border-slate-100 dark:border-slate-800 transition-colors">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-primary/10 text-primary border border-primary/20 rounded-full flex items-center justify-center font-black text-xs shadow-inner">
                            {initials}
                        </div>
                        <div className="flex-1 min-w-0">
                            <p className="text-xs font-black text-slate-900 dark:text-white truncate">
                                {fullName}
                            </p>
                            <p className="text-[10px] text-slate-400 font-bold truncate uppercase tracking-tight">
                                {account?.authorities?.find(r => r.startsWith('ROLE_'))?.replace('ROLE_', '').replace('_', ' ') ||
                                    user?.realm_access?.roles?.find((r: string) => r.startsWith('ROLE_'))?.replace('ROLE_', '').replace('_', ' ') ||
                                    'Médico'}
                            </p>
                        </div>
                    </div>
                    <button
                        onClick={logout}
                        className="w-full mt-3 flex items-center justify-center gap-2 py-2 text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 rounded-lg text-[10px] font-black uppercase tracking-widest transition-colors"
                    >
                        <span className="material-symbols-outlined text-sm">logout</span>
                        Cerrar Sesión
                    </button>
                </div>
            </div>
        </div>
    );
};

// Helper function to determine if a menu item should be highlighted as active
const labelToActive = (label: string, currentTab: string | undefined, selectedPatient: any) => {
    if (selectedPatient) return false; // When a patient is selected, no sidebar item is "active" in the traditional sense
    return currentTab === label;
};

export default Sidebar;
