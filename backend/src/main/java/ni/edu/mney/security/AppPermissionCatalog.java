package ni.edu.mney.security;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AppPermissionCatalog {

    public static final String ADMIN_USERS_MANAGE = "admin.users.manage";
    public static final String ADMIN_ROLES_MANAGE = "admin.roles.manage";
    public static final String ADMIN_DATABASE_VIEW = "admin.database.view";
    public static final String ADMIN_DATABASE_EXPORT = "admin.database.export";
    public static final String ADMIN_DATABASE_RESTORE = "admin.database.restore";

    private static final Map<String, AppPermission> PERMISSIONS = new LinkedHashMap<>();

    static {
        register(ADMIN_USERS_MANAGE, "Gestionar usuarios", "Permite crear, editar y asignar roles a usuarios desde el panel.");
        register(ADMIN_ROLES_MANAGE, "Gestionar roles", "Permite crear roles dinámicos y administrar sus permisos.");
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
