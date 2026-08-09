package com.helix.gpo.web_crm.testimonial.internal;

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
@Table(name = "testimonials")
class Testimonial extends BaseEntity {

    @Column(name = "invitation_id", nullable = false)
    private UUID invitationId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @Column(name = "project_id")
    private UUID projectId;

    // Schnappschuss der Partner-/Mandanten-Daten zum Einreichzeitpunkt -
    // gleiches Prinzip wie seller/buyer bei Invoice: bleibt stabil, auch
    // wenn sich der Partner-Datensatz später ändert oder gelöscht wird
    @Column(name = "partner_name_snapshot", nullable = false, length = 200)
    private String partnerNameSnapshot;

    @Column(name = "partner_role_snapshot", length = 100)
    private String partnerRoleSnapshot;

    @Column(name = "company_name_snapshot", nullable = false, length = 160)
    private String companyNameSnapshot;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private int rating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TestimonialStatus status = TestimonialStatus.PENDING_REVIEW;

    @Column(nullable = false)
    @Builder.Default
    private boolean visibleOnWebsite = false;

    public void approve() {
        this.status = TestimonialStatus.APPROVED;
        this.visibleOnWebsite = true;
    }

    public void reject() {
        this.status = TestimonialStatus.REJECTED;
        this.visibleOnWebsite = false;
    }

}
