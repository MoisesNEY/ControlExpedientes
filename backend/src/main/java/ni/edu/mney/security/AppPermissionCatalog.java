package ni.edu.mney.security;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AppPermissionCatalog {

    public static final String ADMIN_DASHBOARD_VIEW = "admin.dashboard.view";
    public static final String ADMIN_PATIENTS_VIEW = "admin.patients.view";
    public static final String ADMIN_MEDICATIONS_VIEW = "admin.medications.view";
    public static final String ADMIN_INTERACTIONS_VIEW = "admin.interactions.view";
    public static final String ADMIN_DIAGNOSES_VIEW = "admin.diagnoses.view";
    public static final String ADMIN_APPOINTMENTS_VIEW = "admin.appointments.view";
    public static final String ADMIN_RECORDS_VIEW = "admin.records.view";
    public static final String ADMIN_AUDIT_VIEW = "admin.audit.view";
    public static final String ADMIN_USERS_VIEW = "admin.users.view";
    public static final String ADMIN_USERS_MANAGE = "admin.users.manage";
    public static final String ADMIN_USERS_EXPORT = "admin.users.export";
    public static final String ADMIN_ROLES_VIEW = "admin.roles.view";
    public static final String ADMIN_ROLES_MANAGE = "admin.roles.manage";
    public static final String ADMIN_ROLES_EXPORT = "admin.roles.export";
    public static final String ADMIN_DATABASE_VIEW = "admin.database.view";
    public static final String ADMIN_DATABASE_EXPORT = "admin.database.export";
    public static final String ADMIN_DATABASE_RESTORE = "admin.database.restore";

    private static final Map<String, AppPermission> PERMISSIONS = new LinkedHashMap<>();

    static {
        register(ADMIN_DASHBOARD_VIEW, "Ver dashboard administrativo", "Permite acceder al panel principal de administración.");
        register(ADMIN_PATIENTS_VIEW, "Ver gestión de pacientes", "Permite abrir la vista administrativa de pacientes.");
        register(ADMIN_MEDICATIONS_VIEW, "Ver medicamentos", "Permite abrir la vista administrativa del catálogo de medicamentos.");
        register(ADMIN_INTERACTIONS_VIEW, "Ver interacciones", "Permite abrir la vista administrativa de interacciones medicamentosas.");
        register(ADMIN_DIAGNOSES_VIEW, "Ver diagnósticos", "Permite abrir la vista administrativa del catálogo de diagnósticos.");
        register(ADMIN_APPOINTMENTS_VIEW, "Ver gestión de citas", "Permite abrir la vista administrativa de citas.");
        register(ADMIN_RECORDS_VIEW, "Ver expedientes", "Permite abrir la vista administrativa de expedientes.");
        register(ADMIN_AUDIT_VIEW, "Ver auditoría", "Permite consultar la vista de auditoría del sistema.");
        register(ADMIN_USERS_VIEW, "Ver usuarios", "Permite consultar el listado de usuarios administrados.");
        register(ADMIN_USERS_MANAGE, "Gestionar usuarios", "Permite crear, editar y asignar roles a usuarios desde el panel.");
        register(ADMIN_USERS_EXPORT, "Exportar usuarios", "Permite exportar el listado de usuarios administrados.");
        register(ADMIN_ROLES_VIEW, "Ver roles", "Permite consultar el catálogo de roles registrados.");
        register(ADMIN_ROLES_MANAGE, "Gestionar roles", "Permite crear roles dinámicos y administrar sus permisos.");
        register(ADMIN_ROLES_EXPORT, "Exportar roles", "Permite exportar el catálogo de roles dinámicos.");
        register(ADMIN_DATABASE_VIEW, "Ver módulo de base de datos", "Permite ver el historial y la configuración de respaldos.");
        register(ADMIN_DATABASE_EXPORT, "Exportar respaldos", "Permite generar y descargar respaldos manuales.");
        register(ADMIN_DATABASE_RESTORE, "Restaurar respaldos", "Permite restaurar respaldos almacenados o cargados manualmente.");
    }

    private AppPermissionCatalog() {}

    private static void register(String code, String label, String description) {
        PERMISSIONS.put(code, new AppPermission(code, label, description));
    }

    public static List<AppPermission> all() {
        return List.copyOf(PERMISSIONS.values());
    }

    public static boolean exists(String code) {
        return PERMISSIONS.containsKey(code);
    }

    public static Set<String> allCodes() {
        return Set.copyOf(PERMISSIONS.keySet());
    }
}
