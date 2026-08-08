package com.helix.gpo.web_crm.tenant.internal;

import com.helix.gpo.web_crm.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tenant_partners")
class Partner extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_partner_tenant"))
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(length = 100)
    private String role;

    @Column(length = 254)
    private String email;

    @Column(length = 30)
    private String phone;

    public UUID tenantId() {
        return tenant.getId();
    }

}
