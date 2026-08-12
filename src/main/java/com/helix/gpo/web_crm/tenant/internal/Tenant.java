package com.helix.gpo.web_crm.tenant.internal;

import com.helix.gpo.web_crm.shared.Address;
import com.helix.gpo.web_crm.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tenants")
class Tenant extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String companyName;

    @Column(length = 160)
    private String legalName;

    @Column(name = "reference_code", length = 12)
    private String referenceCode;

    @Column(name = "vat_id", length = 20)
    private String vatId;

    @Embedded
    private Address address;

    @Column(length = 254)
    private String contactEmail;

    @Column(length = 30)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TenantStatus status = TenantStatus.PROSPECT;

    @Column(nullable = false)
    @Builder.Default
    private boolean visibleOnWebsite = false;

    public void activate() {
        this.status = TenantStatus.ACTIVE;
    }

    public void archive() {
        this.status = TenantStatus.ARCHIVED;
        this.visibleOnWebsite = false;
    }

    public void updateContactDetails(String contactEmail, String contactPhone, Address address) {
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.address = address;
    }

    public void updateCoreDetails(String companyName, String legalName, String vatId, String referenceCode) {
        this.companyName = companyName;
        this.legalName = legalName;
        this.vatId = vatId;
        this.referenceCode = referenceCode;
    }

}
