package com.helix.gpo.web_crm.testimonial.internal.dto;

import com.helix.gpo.web_crm.testimonial.internal.InvitationStatus;
import com.helix.gpo.web_crm.testimonial.internal.TestimonialStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public final class TestimonialDtos {

    private TestimonialDtos() {
    }

    public record CreateInvitationRequest(
            @NotNull UUID partnerId,
            UUID projectId,
            Integer expiresInDays,
            Boolean sendEmail,
            String email
    ) {
    }

    // rawToken wird NUR hier, einmalig, im Response zurückgegeben - danach
    // existiert er nirgends mehr im System (nur der Hash in der DB)
    public record InvitationResponse(
            UUID invitationId,
            String rawToken,
            Instant expiresAt,
            boolean sent,
            String sentToEmail
    ) {
    }

    public record InvitationSummaryResponse(
            UUID id,
            UUID partnerId,
            UUID projectId,
            InvitationStatus status,
            String sentToEmail,
            Instant sentAt,
            Instant expiresAt,
            Instant usedAt,
            Instant createdAt
    ) {
    }

    public record SubmitTestimonialRequest(
            @NotBlank String token,
            @NotBlank String description,
            @Min(1) @Max(5) int rating
    ) {
    }

    public record TestimonialResponse(
            UUID id,
            UUID tenantId,
            UUID partnerId,
            UUID projectId,
            String partnerName,
            String partnerRole,
            String companyName,
            String description,
            int rating,
            TestimonialStatus status,
            boolean visibleOnWebsite,
            String partnerPhotoUrl,
            Instant createdAt
    ) {
    }

}
