import { Link } from 'react-router-dom';

const Unauthorized = () => {
    return (
        <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col items-center justify-center p-8">
            <span className="material-symbols-outlined text-[100px] text-red-500 mb-6 drop-shadow-lg">gpp_bad</span>
            <h1 className="text-4xl md:text-5xl font-black text-slate-900 dark:text-white mb-4 text-center">Acceso Denegado</h1>
            <p className="text-slate-500 text-lg text-center max-w-xl mb-10">
                No tiene los permisos necesarios (roles) para acceder a esta sección del sistema. Póngase en contacto con un administrador si cree que esto es un error.
            </p>
            <Link to="/" className="px-8 py-4 bg-primary text-white font-bold rounded-full shadow-xl shadow-primary/30 hover:scale-105 transition-transform flex items-center gap-3">
                <span className="material-symbols-outlined">home</span>
                Volver al Inicio
            </Link>
        </div>
    );
};

export default Unauthorized;
