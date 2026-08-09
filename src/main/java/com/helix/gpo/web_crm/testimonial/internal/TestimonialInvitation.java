package com.helix.gpo.web_crm.testimonial.internal;

import com.helix.gpo.web_crm.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "testimonial_invitations")
class TestimonialInvitation extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @Column(name = "project_id")
    private UUID projectId;

    // Nur der Hash wird gespeichert - der Rohtoken existiert nie in der DB
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    public boolean isUsable() {
        return status == InvitationStatus.PENDING && Instant.now().isBefore(expiresAt);
    }

    public void markUsed() {
        this.status = InvitationStatus.USED;
        this.usedAt = Instant.now();
    }

    public void revoke() {
        if (this.status == InvitationStatus.PENDING) {
            this.status = InvitationStatus.REVOKED;
        }
    }

}
