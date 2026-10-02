package com.helix.gpo.web_crm.testimonial.internal;

import com.helix.gpo.web_crm.access.AccessApi;
import com.helix.gpo.web_crm.access.EntityType;
import com.helix.gpo.web_crm.notification.EmailMessage;
import com.helix.gpo.web_crm.notification.NotificationApi;
import com.helix.gpo.web_crm.tenant.PartnerSummary;
import com.helix.gpo.web_crm.tenant.TenantApi;
import com.helix.gpo.web_crm.testimonial.internal.config.WebsiteProperties;
import com.helix.gpo.web_crm.testimonial.internal.dto.TestimonialDtos.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class TestimonialService {

    private static final int DEFAULT_EXPIRY_DAYS = 30;
    private static final int MAX_VISIBLE_ON_WEBSITE = 6;

    private final TestimonialInvitationRepository invitationRepository;
    private final TestimonialRepository testimonialRepository;

    private final TenantApi tenantApi;
    private final NotificationApi notificationApi;
    private final AccessApi accessApi;

    private final TokenGenerator tokenGenerator;
    private final WebsiteProperties websiteProperties;

    InvitationResponse createInvitation(CreateInvitationRequest request) {
        accessApi.requireWrite(EntityType.TESTIMONIAL);

        PartnerSummary partner = tenantApi.findPartnerSummaryById(request.partnerId())
                .orElseThrow(() -> new EntityNotFoundException("Dieser Ansprechpartner wurde nicht gefunden."));

        requireScopeAccess(partner.tenantId(), request.projectId());

        String rawToken = tokenGenerator.generateRawToken();
        int expiryDays = request.expiresInDays() != null ? request.expiresInDays() : DEFAULT_EXPIRY_DAYS;
        Instant expiresAt = Instant.now().plus(Duration.ofDays(expiryDays));

        TestimonialInvitation invitation = TestimonialInvitation.builder()
                .tenantId(partner.tenantId())
                .partnerId(partner.id())
                .projectId(request.projectId())
                .tokenHash(tokenGenerator.hash(rawToken))
                .expiresAt(expiresAt)
                .build();

        invitation = invitationRepository.save(invitation);

        boolean sendEmail = Boolean.TRUE.equals(request.sendEmail());
        String sentToEmail = null;
        if (sendEmail) {
            sentToEmail = request.email() != null && !request.email().isBlank()
                    ? request.email()
                    : partner.email();
            if (sentToEmail == null || sentToEmail.isBlank()) {
                throw new IllegalArgumentException(
                        "Für diesen Ansprechpartner ist keine E-Mail-Adresse hinterlegt - bitte manuell angeben.");
            }
            sendInvitationEmail(partner, sentToEmail, rawToken);
            invitation.markSent(sentToEmail);
            invitationRepository.save(invitation);
        }

        return new InvitationResponse(invitation.getId(), rawToken, expiresAt, sendEmail, sentToEmail);
    }

    @Transactional(readOnly = true)
    List<InvitationSummaryResponse> findInvitationsByTenant(UUID tenantId) {
        accessApi.requireRead(EntityType.TESTIMONIAL);
        requireTenantAccess(tenantId);

        return invitationRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(TestimonialMapper::toSummaryResponse)
                .toList();
    }

    // public api for website - no access check, token-based instead
    TestimonialResponse submit(SubmitTestimonialRequest request) {
        String tokenHash = tokenGenerator.hash(request.token());

        TestimonialInvitation invitation = invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or unknown token"));

        if (!invitation.isUsable()) {
            throw new IllegalStateException("Dieser Einladungslink ist abgelaufen, bereits verwendet oder wurde widerrufen.");
        }

        PartnerSummary partner = tenantApi.findPartnerSummaryById(invitation.getPartnerId())
                .orElseThrow(() -> new EntityNotFoundException("Dieser Ansprechpartner wurde nicht gefunden."));

        Testimonial testimonial = Testimonial.builder()
                .invitationId(invitation.getId())
                .tenantId(invitation.getTenantId())
                .partnerId(invitation.getPartnerId())
                .projectId(invitation.getProjectId())
                .partnerNameSnapshot(partner.firstName() + " " + partner.lastName())
                .partnerRoleSnapshot(partner.role())
                .companyNameSnapshot(partner.companyName())
                .description(request.description())
                .rating(request.rating())
                .build();

        invitation.markUsed();

        return toResponse(testimonialRepository.save(testimonial));
    }

    TestimonialResponse approve(UUID id) {
        accessApi.requireWrite(EntityType.TESTIMONIAL);
        Testimonial testimonial = getOrThrow(id);
        requireTestimonialAccess(testimonial);

        testimonial.approve();
        return toResponse(testimonial);
    }

    TestimonialResponse reject(UUID id) {
        accessApi.requireWrite(EntityType.TESTIMONIAL);
        Testimonial testimonial = getOrThrow(id);
        requireTestimonialAccess(testimonial);

        testimonial.reject();
        return toResponse(testimonial);
    }

    TestimonialResponse publish(UUID id) {
        accessApi.requireWrite(EntityType.TESTIMONIAL);
        Testimonial testimonial = getOrThrow(id);
        requireTestimonialAccess(testimonial);

        if (testimonial.getStatus() != TestimonialStatus.APPROVED) {
            throw new IllegalStateException(
                    "Nur freigegebene Referenzen können auf der Website veröffentlicht werden: " + id);
        }

        if (!testimonial.isVisibleOnWebsite() && testimonialRepository.countByVisibleOnWebsiteTrue() >= MAX_VISIBLE_ON_WEBSITE) {
            throw new IllegalStateException(
                    "Es können maximal " + MAX_VISIBLE_ON_WEBSITE + " Referenzen gleichzeitig auf der Website sichtbar sein");
        }

        testimonial.publish();
        return toResponse(testimonial);
    }

    TestimonialResponse unpublish(UUID id) {
        accessApi.requireWrite(EntityType.TESTIMONIAL);
        Testimonial testimonial = getOrThrow(id);
        requireTestimonialAccess(testimonial);

        testimonial.unpublish();
        return toResponse(testimonial);
    }

    @Transactional(readOnly = true)
    List<TestimonialResponse> findAll() {
        accessApi.requireRead(EntityType.TESTIMONIAL);
        return filterAccessible(testimonialRepository.findAll()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    List<TestimonialResponse> findAllByTenant(UUID tenantId) {
        accessApi.requireRead(EntityType.TESTIMONIAL);
        return filterAccessible(testimonialRepository.findAllByTenantId(tenantId)).stream()
                .map(this::toResponse)
                .toList();
    }

    // public api for website - no access check
    @Transactional(readOnly = true)
    List<TestimonialResponse> findAllVisibleOnWebsite() {
        return testimonialRepository.findAllByVisibleOnWebsiteTrueOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private Testimonial getOrThrow(UUID id) {
        return testimonialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Diese Referenz wurde nicht gefunden."));
    }

    private TestimonialResponse toResponse(Testimonial testimonial) {
        String partnerPhotoUrl = tenantApi.findPartnerSummaryById(testimonial.getPartnerId())
                .map(PartnerSummary::photoUrl)
                .orElse(null);
        return TestimonialMapper.toResponse(testimonial, partnerPhotoUrl);
    }

    private void sendInvitationEmail(PartnerSummary partner, String toEmail, String rawToken) {
        String link = websiteProperties.baseUrl() + "/feedback?token=" + rawToken;
        String subject = "Wir würden uns über Ihre Referenz freuen – " + partner.companyName();
        String preheader = "Teilen Sie in wenigen Minuten Ihre Erfahrung mit uns.";
        String body = """
            <p style="margin:0 0 16px;">Hallo %s,</p>
            <p style="margin:0 0 16px;">vielen Dank für die Zusammenarbeit! Wir würden uns sehr über eine kurze Referenz von Ihnen freuen – es dauert nur wenige Minuten.</p>
            %s
            <p style="margin:24px 0 0; font-size:13px; color:#8a93a6;">Der Link ist einmalig gültig und läuft automatisch ab.</p>
            <p style="margin:16px 0 0;">Viele Grüße<br/>Helix GPO</p>
            """.formatted(partner.firstName(), com.helix.gpo.web_crm.notification.EmailLayout.button("Referenz abgeben", link));

        notificationApi.send(new EmailMessage(toEmail, subject, preheader, body));
    }

    void revokeInvitation(UUID invitationId) {
        accessApi.requireWrite(EntityType.TESTIMONIAL);
        TestimonialInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Diese Einladung wurde nicht gefunden."));
        requireScopeAccess(invitation.getTenantId(), invitation.getProjectId());

        invitation.revoke();
    }

    private List<Testimonial> filterAccessible(List<Testimonial> testimonials) {
        if (accessApi.isUnrestricted()) {
            return testimonials;
        }
        List<UUID> accessibleProjects = accessApi.accessibleProjectIds();
        List<UUID> accessibleTenants = accessApi.accessibleTenantIds();
        return testimonials.stream()
                .filter(t -> t.getProjectId() != null
                        ? accessibleProjects.contains(t.getProjectId())
                        : accessibleTenants.contains(t.getTenantId()))
                .toList();
    }

    private void requireTestimonialAccess(Testimonial testimonial) {
        requireScopeAccess(testimonial.getTenantId(), testimonial.getProjectId());
    }

    private void requireTenantAccess(UUID tenantId) {
        if (!accessApi.canAccessTenant(tenantId)) {
            throw new AccessDeniedException("Kein Zugriff auf diesen Mandanten.");
        }
    }

    private void requireScopeAccess(UUID tenantId, UUID projectId) {
        boolean allowed = accessApi.isUnrestricted()
                || (projectId != null ? accessApi.canAccessProject(projectId) : accessApi.canAccessTenant(tenantId));
        if (!allowed) {
            throw new AccessDeniedException("Kein Zugriff auf diesen Mandanten oder dieses Projekt.");
        }
    }

}
