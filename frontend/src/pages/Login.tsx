import { useMemo, useState, type FormEvent } from 'react';
import { useAuth } from '../context/AuthContext';
import { Navigate, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { AppointmentService } from '../services/appointment.service';
import { resolveAuthorizedHomePath } from '../utils/authNavigation';

/* ─── SVG: Visualización abstracta de nodos médicos ─── */
const NodePattern = () => (
    <svg
        viewBox="0 0 400 500"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="absolute inset-0 w-full h-full opacity-30"
        aria-hidden="true"
    >
        {/* Líneas de conexión */}
        <line x1="80" y1="120" x2="200" y2="80" stroke="#0ea5e9" strokeWidth="0.7" strokeDasharray="4 6" />
        <line x1="200" y1="80" x2="320" y2="160" stroke="#0ea5e9" strokeWidth="0.7" strokeDasharray="4 6" />
        <line x1="200" y1="80" x2="180" y2="230" stroke="#0ea5e9" strokeWidth="0.7" strokeDasharray="4 6" />
        <line x1="80" y1="120" x2="140" y2="280" stroke="#0ea5e9" strokeWidth="0.5" strokeDasharray="3 8" />
        <line x1="320" y1="160" x2="300" y2="300" stroke="#0ea5e9" strokeWidth="0.5" strokeDasharray="3 8" />
        <line x1="180" y1="230" x2="300" y2="300" stroke="#0ea5e9" strokeWidth="0.7" strokeDasharray="4 6" />
        <line x1="140" y1="280" x2="180" y2="230" stroke="#0ea5e9" strokeWidth="0.5" strokeDasharray="3 8" />
        <line x1="140" y1="280" x2="220" y2="380" stroke="#0ea5e9" strokeWidth="0.5" strokeDasharray="4 8" />
        <line x1="300" y1="300" x2="220" y2="380" stroke="#0ea5e9" strokeWidth="0.5" strokeDasharray="3 8" />
        <line x1="80" y1="120" x2="60" y2="260" stroke="#0ea5e9" strokeWidth="0.4" strokeDasharray="2 10" />
        <line x1="60" y1="260" x2="140" y2="280" stroke="#0ea5e9" strokeWidth="0.4" strokeDasharray="2 10" />
        <line x1="320" y1="160" x2="370" y2="280" stroke="#0ea5e9" strokeWidth="0.4" strokeDasharray="2 10" />
        <line x1="370" y1="280" x2="300" y2="300" stroke="#0ea5e9" strokeWidth="0.4" strokeDasharray="2 10" />

        {/* Nodos — primario grande */}
        <circle cx="200" cy="80" r="7" fill="#0ea5e9" fillOpacity="0.9" />
        <circle cx="200" cy="80" r="13" fill="#0ea5e9" fillOpacity="0.12" />

        {/* Nodos secundarios */}
        <circle cx="80" cy="120" r="5" fill="#38bdf8" fillOpacity="0.8" />
        <circle cx="80" cy="120" r="10" fill="#38bdf8" fillOpacity="0.10" />

        <circle cx="320" cy="160" r="5" fill="#38bdf8" fillOpacity="0.8" />
        <circle cx="320" cy="160" r="10" fill="#38bdf8" fillOpacity="0.10" />

        <circle cx="180" cy="230" r="4" fill="#67e8f9" fillOpacity="0.7" />
        <circle cx="140" cy="280" r="4" fill="#67e8f9" fillOpacity="0.6" />
        <circle cx="300" cy="300" r="4" fill="#67e8f9" fillOpacity="0.6" />

        {/* Nodos terciarios pequeños */}
        <circle cx="60" cy="260" r="3" fill="#e0f2fe" fillOpacity="0.4" />
        <circle cx="370" cy="280" r="3" fill="#e0f2fe" fillOpacity="0.4" />
        <circle cx="220" cy="380" r="3" fill="#e0f2fe" fillOpacity="0.35" />

        {/* Pulso sutil en el nodo central */}
        <circle cx="200" cy="80" r="22" fill="none" stroke="#0ea5e9" strokeWidth="0.5" strokeOpacity="0.25" />
        <circle cx="200" cy="80" r="32" fill="none" stroke="#0ea5e9" strokeWidth="0.3" strokeOpacity="0.12" />

        {/* Cruz médica integrada en el nodo central — muy sutil */}
        <line x1="200" y1="74" x2="200" y2="86" stroke="#fff" strokeWidth="1.5" strokeOpacity="0.6" strokeLinecap="round" />
        <line x1="194" y1="80" x2="206" y2="80" stroke="#fff" strokeWidth="1.5" strokeOpacity="0.6" strokeLinecap="round" />
    </svg>
);

/* ─── Componente principal ─── */
const Login = () => {
    const { isAuthenticated, login, completeRequiredActions, hasAnyRole } = useAuth();
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [language, setLanguage] = useState('es');
    const [requiredActions, setRequiredActions] = useState<string[]>([]);
    const [pendingProfile, setPendingProfile] = useState({ login: '', firstName: '', lastName: '', email: '' });
    const [newPassword, setNewPassword] = useState('');
    const [confirmNewPassword, setConfirmNewPassword] = useState('');
    const requiredActionSet = useMemo(() => new Set(requiredActions), [requiredActions]);

    if (isAuthenticated) return <Navigate to="/" replace />;

    const navigateAfterAuthentication = async () => {
        try {
            const accountResponse = await api.get('/api/account');
            const account = accountResponse.data;
            const authorities = Array.isArray(account?.authorities) ? account.authorities : [];
            const permissions = Array.isArray(account?.permissions) ? account.permissions : [];

            if (authorities.includes('ROLE_MEDICO')) {
                const activeConsultation = await AppointmentService.getActiveConsultation(String(account.id ?? account.login ?? ''));
                if (activeConsultation?.id) {
                    try {
                        localStorage.setItem('activeConsultation', String(activeConsultation.id));
                    } catch {
                        // ignore
                    }
                    navigate(`/medico/consulta/${activeConsultation.id}`);
                    return;
                }
            }

            const active = typeof window !== 'undefined' ? localStorage.getItem('activeConsultation') : null;
            if (active && hasAnyRole(['ROLE_MEDICO'])) {
                navigate(`/medico/consulta/${active}`);
                return;
            }

            navigate(resolveAuthorizedHomePath(authorities, permissions));
        } catch {
            navigate('/');
        }
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError('');
        setRequiredActions([]);
        setPendingProfile({ login: '', firstName: '', lastName: '', email: '' });
        setNewPassword('');
        setConfirmNewPassword('');
        setIsLoading(true);
        const result = await login(username, password);
        setIsLoading(false);
        if (!result.success) {
            setError(result.error || 'Credenciales incorrectas.');
            if (result.requiresActionCompletion) {
                setRequiredActions(result.requiredActions ?? []);
                setPendingProfile({
                    login: result.profile?.login ?? username,
                    firstName: result.profile?.firstName ?? '',
                    lastName: result.profile?.lastName ?? '',
                    email: result.profile?.email ?? '',
                });
            }
            return;
        }
        await navigateAfterAuthentication();
    };

    const handleRequiredActionsSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError('');

        if (requiredActionSet.has('UPDATE_PASSWORD')) {
            if (!newPassword.trim()) {
                setError('Debes definir una nueva contraseña para completar el acceso.');
                return;
            }
            if (newPassword !== confirmNewPassword) {
                setError('La confirmación de la nueva contraseña no coincide.');
                return;
            }
        }

        setIsLoading(true);
        const result = await completeRequiredActions({
            firstName: pendingProfile.firstName,
            lastName: pendingProfile.lastName,
            email: pendingProfile.email,
            currentPassword: password,
            newPassword: requiredActionSet.has('UPDATE_PASSWORD') ? newPassword : undefined,
        });
        setIsLoading(false);

        if (!result.success) {
            setError(result.error || 'No se pudieron completar las acciones obligatorias.');
            setRequiredActions(result.requiredActions ?? requiredActions);
            if (result.profile) {
                setPendingProfile({
                    login: result.profile.login,
                    firstName: result.profile.firstName ?? '',
                    lastName: result.profile.lastName ?? '',
                    email: result.profile.email ?? '',
                });
            }
            return;
        }

        await navigateAfterAuthentication();
    };

    return (
        <div className="min-h-screen flex flex-col lg:flex-row font-display overflow-hidden">

            {/* ══════════════════════════════════════════
                PANEL IZQUIERDO — 35% — Solo en desktop
            ══════════════════════════════════════════ */}
            <aside className="hidden lg:flex lg:w-[35%] relative flex-col justify-between bg-[#071e2b] overflow-hidden">

                {/* Fondo con gradiente muy sutil */}
                <div className="absolute inset-0 bg-gradient-to-b from-[#0a2d42] via-[#071e2b] to-[#050f19]" />

                {/* Patrón de cuadrícula fina */}
                <div
                    className="absolute inset-0 opacity-[0.04]"
                    style={{
                        backgroundImage: `
                            linear-gradient(to right, #ffffff 1px, transparent 1px),
                            linear-gradient(to bottom, #ffffff 1px, transparent 1px)
                        `,
                        backgroundSize: '40px 40px',
                    }}
                />

                {/* Visualización de nodos */}
                <div className="absolute inset-0">
                    <NodePattern />
                </div>

                {/* Gradiente de fade en la parte inferior para fundir el gráfico */}
                <div className="absolute bottom-0 left-0 right-0 h-48 bg-gradient-to-t from-[#050f19] to-transparent" />

                {/* Contenido real sobre el fondo */}
                <div className="relative z-10 flex flex-col h-full p-10">

                    {/* Logo */}
                    <div className="flex items-center gap-2.5">
                        <div className="w-8 h-8 rounded-lg bg-sky-500/20 border border-sky-500/30 flex items-center justify-center">
                            <span className="material-symbols-outlined text-sky-400 text-[17px]">local_hospital</span>
                        </div>
                        <div>
                            <p className="text-white text-sm font-black leading-none tracking-tight">ClinData</p>
                            <p className="text-sky-500/60 text-[9px] font-bold uppercase tracking-[3px] mt-0.5">Health Platform</p>
                        </div>
                    </div>

                    {/* Espaciador */}
                    <div className="flex-1" />

                    {/* Headline */}
                    <div className="mb-10">
                        <h1 className="text-white/90 text-2xl font-black leading-snug tracking-tight">
                            Expedientes<br />
                            Clínicos<br />
                            <span className="text-sky-400">Inteligentes.</span>
                        </h1>
                        <p className="text-white/30 text-xs mt-3 leading-relaxed font-medium max-w-[200px]">
                            Plataforma integrada de gestión médica para clínicas modernas.
                        </p>
                    </div>

                    {/* Copyright */}
                    <p className="text-white/15 text-[10px] font-medium tracking-wide">
                        © {new Date().getFullYear()} ClinData
                    </p>
                </div>

                {/* Separador vertical derecho */}
                <div className="absolute right-0 top-0 h-full w-px bg-white/[0.06]" />
            </aside>

            {/* ══════════════════════════════════════════
                PANEL DERECHO — 65% — Formulario
            ══════════════════════════════════════════ */}
            <main className="flex-1 flex flex-col bg-white dark:bg-[#0b1a24]">

                {/* Barra superior */}
                <header className="flex items-center justify-between px-10 py-7">
                    {/* Logo visible solo en mobile */}
                    <div className="flex lg:hidden items-center gap-2">
                        <div className="w-7 h-7 rounded-lg bg-sky-500/15 border border-sky-500/25 flex items-center justify-center">
                            <span className="material-symbols-outlined text-sky-500 text-[15px]">local_hospital</span>
                        </div>
                        <p className="text-slate-900 dark:text-white text-sm font-black tracking-tight">ClinData</p>
                    </div>
                    <div className="hidden lg:block" />

                    {/* Selector de Idioma */}
                    <div className="flex items-center gap-1.5 text-slate-400 dark:text-slate-500">
                        <span className="material-symbols-outlined text-[15px]">language</span>
                        <select
                            value={language}
                            onChange={(e) => setLanguage(e.target.value)}
                            className="text-[11px] font-bold bg-transparent border-none outline-none cursor-pointer hover:text-slate-600 dark:hover:text-slate-300 transition-colors appearance-none"
                        >
                            <option value="es">Español</option>
                            <option value="en">English</option>
                        </select>
                    </div>
                </header>

                {/* Formulario — centrado vertical */}
                <div className="flex-1 flex items-center justify-center px-8 md:px-16 lg:px-20 py-10">
                    <div className="w-full max-w-sm">

                        {/* Encabezado del formulario */}
                        <div className="mb-9">
                            <p className="text-[10px] font-black text-sky-500 uppercase tracking-[3px] mb-3">
                                Portal Institucional
                            </p>
                             <h2 className="text-2xl font-black text-slate-900 dark:text-white tracking-tight leading-tight">
                                 Iniciar sesión
                             </h2>
                             <p className="text-slate-400 dark:text-slate-500 text-sm mt-1.5">
                                 Accede con tus credenciales institucionales y completa cualquier requisito de seguridad sin salir del portal.
                             </p>
                          </div>
 
                         {/* Error */}
                        {error && (
                            <div className="mb-6 flex items-start gap-2.5 p-3.5 rounded-lg bg-rose-50 dark:bg-rose-500/8 border border-rose-200 dark:border-rose-500/20">
                                <span className="material-symbols-outlined text-rose-500 text-[17px] mt-0.5 shrink-0">error</span>
                                <p className="text-rose-600 dark:text-rose-400 text-sm font-medium">{error}</p>
                            </div>
                        )}
 
                        {requiredActions.length > 0 && (
                            <div className="mb-6 flex items-start gap-2.5 p-3.5 rounded-lg bg-amber-50 dark:bg-amber-500/8 border border-amber-200 dark:border-amber-500/20">
                                  <span className="material-symbols-outlined text-amber-500 text-[17px] mt-0.5 shrink-0">security</span>
                                  <p className="text-amber-700 dark:text-amber-300 text-sm font-medium">
                                     Esta cuenta debe completar acciones obligatorias antes de entrar. Todo el proceso se resuelve desde este portal.
                                  </p>
                              </div>
                          )}

                        <form onSubmit={handleSubmit} className="space-y-5">

                            {/* Usuario */}
                            <div>
                                <label
                                    htmlFor="username"
                                    className="block text-[10px] font-black text-slate-400 dark:text-slate-500 uppercase tracking-widest mb-2"
                                >
                                    Usuario
                                </label>
                                <div className="relative group">
                                    <span className="absolute left-3.5 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-300 dark:text-slate-600 group-focus-within:text-sky-500 transition-colors duration-150 text-[18px]">
                                        badge
                                    </span>
                                    <input
                                        id="username"
                                        type="text"
                                        value={username}
                                        onChange={(e) => setUsername(e.target.value)}
                                        className="w-full pl-10 pr-4 py-3 rounded-lg border border-slate-200 dark:border-white/10 bg-slate-50 dark:bg-white/[0.04] text-slate-900 dark:text-white text-sm font-semibold placeholder:font-normal placeholder:text-slate-400 dark:placeholder:text-white/20 focus:outline-none focus:border-sky-500 dark:focus:border-sky-500 focus:bg-white dark:focus:bg-white/[0.06] transition-all duration-150"
                                        placeholder="Nombre de usuario"
                                        required
                                        autoComplete="username"
                                        autoFocus
                                    />
                                </div>
                            </div>

                            {/* Contraseña */}
                            <div>
                                <div className="flex items-center justify-between mb-2">
                                    <label
                                        htmlFor="password"
                                        className="text-[10px] font-black text-slate-400 dark:text-slate-500 uppercase tracking-widest"
                                    >
                                        Contraseña
                                    </label>
                                    <button
                                        type="button"
                                        tabIndex={-1}
                                        className="text-[11px] font-semibold text-sky-500 hover:text-sky-600 transition-colors"
                                    >
                                        ¿Olvidó su contraseña?
                                    </button>
                                </div>
                                <div className="relative group">
                                    <span className="absolute left-3.5 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-300 dark:text-slate-600 group-focus-within:text-sky-500 transition-colors duration-150 text-[18px]">
                                        lock
                                    </span>
                                    <input
                                        id="password"
                                        type={showPassword ? 'text' : 'password'}
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        className="w-full pl-10 pr-11 py-3 rounded-lg border border-slate-200 dark:border-white/10 bg-slate-50 dark:bg-white/[0.04] text-slate-900 dark:text-white text-sm font-semibold placeholder:font-normal placeholder:text-slate-400 dark:placeholder:text-white/20 focus:outline-none focus:border-sky-500 dark:focus:border-sky-500 focus:bg-white dark:focus:bg-white/[0.06] transition-all duration-150"
                                        placeholder="Contraseña"
                                        required
                                        autoComplete="current-password"
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowPassword(!showPassword)}
                                        tabIndex={-1}
                                        className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-300 dark:text-slate-600 hover:text-slate-500 dark:hover:text-slate-400 transition-colors"
                                        aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                                    >
                                        <span className="material-symbols-outlined text-[18px]">
                                            {showPassword ? 'visibility_off' : 'visibility'}
                                        </span>
                                    </button>
                                </div>
                             </div>
                             
                             {/* Botón */}
                            <div className="pt-1">
                                <button
                                    type="submit"
                                    disabled={isLoading || !username || !password}
                                    className="w-full py-3 px-5 rounded-lg bg-sky-600 hover:bg-sky-700 active:bg-sky-800
                                               text-white text-sm font-bold tracking-wide
                                               shadow-md shadow-sky-600/20 hover:shadow-lg hover:shadow-sky-700/25
                                               hover:-translate-y-px active:translate-y-0
                                               disabled:opacity-40 disabled:cursor-not-allowed disabled:translate-y-0 disabled:shadow-none
                                               transition-all duration-150 ease-out
                                               flex items-center justify-center gap-2"
                                >
                                    {isLoading ? (
                                        <>
                                            <div className="w-4 h-4 rounded-full border-2 border-white/30 border-t-white animate-spin" />
                                            <span>Autenticando...</span>
                                        </>
                                    ) : (
                                        <>
                                            <span className="material-symbols-outlined text-[18px]">login</span>
                                            <span>Entrar al Sistema</span>
                                        </>
                                     )}
                                 </button>
                             </div>
                         </form>

                        {requiredActions.length > 0 && (
                            <form onSubmit={handleRequiredActionsSubmit} className="mt-6 space-y-5 rounded-2xl border border-slate-200 dark:border-white/10 bg-slate-50 dark:bg-white/[0.03] p-5">
                                <div>
                                    <p className="text-[10px] font-black text-amber-500 uppercase tracking-[3px] mb-2">
                                        Acciones obligatorias
                                    </p>
                                    <h3 className="text-base font-black text-slate-900 dark:text-white">
                                        Completa la seguridad de {pendingProfile.login}
                                    </h3>
                                    <div className="mt-3 flex flex-wrap gap-2">
                                        {requiredActions.map(action => (
                                            <span key={action} className="rounded-full border border-amber-200 bg-amber-50 px-2.5 py-1 text-[11px] font-bold text-amber-700 dark:border-amber-500/30 dark:bg-amber-500/10 dark:text-amber-200">
                                                {action}
                                            </span>
                                        ))}
                                    </div>
                                </div>

                                {(requiredActionSet.has('UPDATE_PROFILE') || requiredActionSet.has('VERIFY_EMAIL')) && (
                                    <div className="grid grid-cols-1 gap-4">
                                        <div>
                                            <label className="block text-[10px] font-black text-slate-400 dark:text-slate-500 uppercase tracking-widest mb-2">
                                                Nombres
                                            </label>
                                            <input
                                                type="text"
                                                value={pendingProfile.firstName}
                                                onChange={e => setPendingProfile(current => ({ ...current, firstName: e.target.value }))}
                                                className="w-full px-4 py-3 rounded-lg border border-slate-200 dark:border-white/10 bg-white dark:bg-white/[0.04] text-slate-900 dark:text-white text-sm font-semibold focus:outline-none focus:border-sky-500"
                                            />
                                        </div>
                                        <div>
                                            <label className="block text-[10px] font-black text-slate-400 dark:text-slate-500 uppercase tracking-widest mb-2">
                                                Apellidos
                                            </label>
                                            <input
                                                type="text"
                                                value={pendingProfile.lastName}
                                                onChange={e => setPendingProfile(current => ({ ...current, lastName: e.target.value }))}
                                                className="w-full px-4 py-3 rounded-lg border border-slate-200 dark:border-white/10 bg-white dark:bg-white/[0.04] text-slate-900 dark:text-white text-sm font-semibold focus:outline-none focus:border-sky-500"
                                            />
                                        </div>
                                        <div>
                                            <label className="block text-[10px] font-black text-slate-400 dark:text-slate-500 uppercase tracking-widest mb-2">
                                                Correo institucional
                                            </label>
                                            <input
                                                type="email"
                                                value={pendingProfile.email}
                                                onChange={e => setPendingProfile(current => ({ ...current, email: e.target.value }))}
                                                className="w-full px-4 py-3 rounded-lg border border-slate-200 dark:border-white/10 bg-white dark:bg-white/[0.04] text-slate-900 dark:text-white text-sm font-semibold focus:outline-none focus:border-sky-500"
                                            />
                                        </div>
                                    </div>
                                )}

                                {requiredActionSet.has('UPDATE_PASSWORD') && (
                                    <div className="grid grid-cols-1 gap-4">
                                        <div>
                                            <label className="block text-[10px] font-black text-slate-400 dark:text-slate-500 uppercase tracking-widest mb-2">
                                                Nueva contraseña
                                            </label>
                                            <input
                                                type="password"
                                                value={newPassword}
                                                onChange={e => setNewPassword(e.target.value)}
                                                className="w-full px-4 py-3 rounded-lg border border-slate-200 dark:border-white/10 bg-white dark:bg-white/[0.04] text-slate-900 dark:text-white text-sm font-semibold focus:outline-none focus:border-sky-500"
                                                autoComplete="new-password"
                                            />
                                        </div>
                                        <div>
                                            <label className="block text-[10px] font-black text-slate-400 dark:text-slate-500 uppercase tracking-widest mb-2">
                                                Confirmar nueva contraseña
                                            </label>
                                            <input
                                                type="password"
                                                value={confirmNewPassword}
                                                onChange={e => setConfirmNewPassword(e.target.value)}
                                                className="w-full px-4 py-3 rounded-lg border border-slate-200 dark:border-white/10 bg-white dark:bg-white/[0.04] text-slate-900 dark:text-white text-sm font-semibold focus:outline-none focus:border-sky-500"
                                                autoComplete="new-password"
                                            />
                                        </div>
                                    </div>
                                )}

                                {requiredActionSet.has('CONFIGURE_TOTP') && (
                                    <div className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700 dark:border-rose-500/20 dark:bg-rose-500/8 dark:text-rose-300">
                                        La configuración de segundo factor todavía no está disponible desde este portal para esta cuenta, por lo que no podrás completar el acceso aquí. Contacta a un administrador para continuar.
                                    </div>
                                )}

                                <button
                                    type="submit"
                                    disabled={isLoading || requiredActionSet.has('CONFIGURE_TOTP')}
                                    className="w-full py-3 px-5 rounded-lg border border-sky-200 bg-white text-sky-700 text-sm font-bold tracking-wide hover:border-sky-300 hover:text-sky-800 disabled:opacity-40 disabled:cursor-not-allowed transition-all duration-150 ease-out flex items-center justify-center gap-2 dark:border-sky-500/20 dark:bg-white/[0.03] dark:text-sky-300"
                                >
                                    {isLoading ? (
                                        <>
                                            <div className="w-4 h-4 rounded-full border-2 border-sky-300/30 border-t-sky-500 animate-spin" />
                                            <span>Aplicando cambios...</span>
                                        </>
                                    ) : (
                                        <>
                                            <span className="material-symbols-outlined text-[18px]">verified_user</span>
                                            <span>Completar acciones y entrar</span>
                                        </>
                                    )}
                                </button>
                            </form>
                        )}

                        {/* Nota de seguridad */}
                        <p className="mt-8 text-center text-[11px] text-slate-300 dark:text-white/15 font-medium leading-relaxed">
                            Acceso restringido al personal autorizado.<br />
                            Todas las sesiones son registradas y auditadas.
                        </p>
                    </div>
                </div>

                {/* Footer */}
                <footer className="flex items-center justify-between px-10 py-6 border-t border-slate-100 dark:border-white/[0.05]">
                    <div className="flex items-center gap-2">
                        <div className="w-5 h-5 rounded-md bg-sky-500/15 border border-sky-500/20 flex items-center justify-center">
                            <span className="material-symbols-outlined text-sky-500 text-[11px]">local_hospital</span>
                        </div>
                        <span className="text-[11px] font-semibold text-slate-400 dark:text-white/20">
                            ClinData
                        </span>
                    </div>
                    <span className="text-[10px] font-bold text-slate-300 dark:text-white/15 border border-slate-200 dark:border-white/10 px-2 py-0.5 rounded-md">
                        v2.1.0
                    </span>
                </footer>
            </main>
        </div>
    );
};

export default Login;
