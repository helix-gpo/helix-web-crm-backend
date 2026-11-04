package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.EntityType;
import com.helix.gpo.web_crm.access.PermissionAction;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void unrestrictedRoleAllowsEverything() {
        Role admin = Role.builder().name("Administrator").unrestricted(true).build();

        for (EntityType entity : EntityType.values()) {
            for (PermissionAction action : PermissionAction.values()) {
                assertThat(admin.allows(entity, action)).as("%s:%s", entity, action).isTrue();
            }
        }
    }

    @Test
    void restrictedRoleAllowsOnlyGrantedPermissions() {
        Role role = Role.builder()
                .name("Buchhaltung")
                .permissions(Set.of(
                        new RolePermission(EntityType.INVOICE, PermissionAction.READ),
                        new RolePermission(EntityType.INVOICE, PermissionAction.WRITE)))
                .build();

        assertThat(role.allows(EntityType.INVOICE, PermissionAction.READ)).isTrue();
        assertThat(role.allows(EntityType.INVOICE, PermissionAction.WRITE)).isTrue();
        assertThat(role.allows(EntityType.INVOICE, PermissionAction.DELETE)).isFalse();
        assertThat(role.allows(EntityType.TENANT, PermissionAction.READ)).isFalse();
    }

    @Test
    void roleWithoutPermissionsAllowsNothing() {
        Role role = Role.builder().name("Leer").build();

        assertThat(role.allows(EntityType.PROJECT, PermissionAction.READ)).isFalse();
    }

}
