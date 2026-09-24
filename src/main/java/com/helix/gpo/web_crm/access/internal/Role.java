package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.EntityType;
import com.helix.gpo.web_crm.access.PermissionAction;
import com.helix.gpo.web_crm.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "roles")
class Role extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean unrestricted;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    private Set<RolePermission> permissions = new HashSet<>();

    boolean allows(EntityType entity, PermissionAction action) {
        return unrestricted || permissions.contains(new RolePermission(entity, action));
    }

    void updateDetails(String name, String description, boolean unrestricted, Set<RolePermission> permissions) {
        this.name = name;
        this.description = description;
        this.unrestricted = unrestricted;
        this.permissions = new HashSet<>(permissions);
    }

}
