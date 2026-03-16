package com.example.correction_tps.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Centralisation des habilitations côté serveur.
 * Aucune habilitation côté client.
 */
public final class RolePermissionMapping {

    private static final Map<Role, Set<Permission>> PERMISSIONS = new EnumMap<>(Role.class);

    static {
        PERMISSIONS.put(Role.USER, Set.of(
                Permission.READ_DATA,
                Permission.PERFORM_TRANSACTION
        ));

        PERMISSIONS.put(Role.ADMIN, Set.of(
                Permission.READ_DATA,
                Permission.WRITE_DATA,
                Permission.DELETE_DATA,
                Permission.MANAGE_USERS,
                Permission.VIEW_LOGS,
                Permission.PERFORM_TRANSACTION,
                Permission.PERFORM_CRITICAL_TRANSACTION
        ));

        PERMISSIONS.put(Role.SUPER_ADMIN, Set.of(Permission.values()));
    }

    private RolePermissionMapping() {
    }

    public static Set<Permission> getPermissions(Role role) {
        return PERMISSIONS.getOrDefault(role, Set.of());
    }

    public static boolean hasPermission(Role role, Permission permission) {
        return getPermissions(role).contains(permission);
    }
}
