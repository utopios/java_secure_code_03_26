package com.example.correction_tps.controller;

import com.example.correction_tps.model.Permission;
import com.example.correction_tps.model.Role;
import com.example.correction_tps.model.RolePermissionMapping;
import com.example.correction_tps.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/superadmin")
public class SuperAdminController {

    private final SessionService sessionService;

    public SuperAdminController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Consulter les permissions d'un rôle.
     */
    @GetMapping("/roles/{role}/permissions")
    public ResponseEntity<Set<Permission>> getRolePermissions(@PathVariable String role) {
        Role r = Role.valueOf(role.toUpperCase());
        return ResponseEntity.ok(RolePermissionMapping.getPermissions(r));
    }

    /**
     * Gérer les rôles - session hautement critique (5 min).
     */
    @PostMapping("/roles/manage")
    public ResponseEntity<String> manageRoles(HttpServletRequest request) {
        sessionService.configureSessionTimeout(request, "highly_critical");
        return ResponseEntity.ok("Gestion des rôles - session sécurisée (5 min)");
    }
}
