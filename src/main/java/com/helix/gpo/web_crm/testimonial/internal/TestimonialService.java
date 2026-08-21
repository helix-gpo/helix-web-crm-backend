package com.helix.gpo.web_crm.testimonial.internal;

import com.helix.gpo.web_crm.notification.NotificationApi;
import com.helix.gpo.web_crm.testimonial.internal.config.WebsiteProperties;
import com.helix.gpo.web_crm.testimonial.internal.dto.TestimonialDtos.*;
import com.helix.gpo.web_crm.tenant.PartnerSummary;
import com.helix.gpo.web_crm.tenant.TenantApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
class TestimonialService {

    private static final int DEFAULT_EXPIRY_DAYS = 30;

    // Gleiche redaktionelle Obergrenze wie bei Projekten - Website-Sektion
    // "Referenzen" zeigt maximal 6 Karten
    private static final int MAX_VISIBLE_ON_WEBSITE = 6;

    private final TestimonialInvitationRepository invitationRepository;
    private final TestimonialRepository testimonialRepository;
    private final TokenGenerator tokenGenerator;
    private final TenantApi tenantApi;
    private final NotificationApi notificationApi;
    private final WebsiteProperties websiteProperties;

    InvitationResponse createInvitation(CreateInvitationRequest request) {
        PartnerSummary partner = tenantApi.findPartnerSummaryById(request.partnerId())
                .orElseThrow(() -> new EntityNotFoundException("Partner not found: " + request.partnerId()));

        String rawToken = tokenGenerator.generateRawToken();
        int expiryDays = request.expiresInDays() != null ? request.expiresInDays() : DEFAULT_EXPIRY_DAYS;
        Instant expiresAt = Instant.now().plus(java.time.Duration.ofDays(expiryDays));

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

        // rawToken existiert ab jetzt NUR noch in diesem Response - nirgends persistiert
        return new InvitationResponse(invitation.getId(), rawToken, expiresAt, sendEmail, sentToEmail);
    }

    @Transactional(readOnly = true)
    List<InvitationSummaryResponse> findInvitationsByTenant(UUID tenantId) {
        return invitationRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(TestimonialMapper::toSummaryResponse)
                .toList();
    }

    private void sendInvitationEmail(PartnerSummary partner, String toEmail, String rawToken) {
        String link = websiteProperties.baseUrl() + "/feedback?token=" + rawToken;
        String subject = "Wir würden uns über Ihre Referenz freuen – " + partner.companyName();
        String html = """
            <p>Hallo %s,</p>
            <p>vielen Dank für die Zusammenarbeit! Wir würden uns sehr über eine kurze Referenz von Ihnen freuen.</p>
            <p><a href="%s">Hier klicken, um eine Referenz abzugeben</a></p>
            <p>Der Link ist einmalig gültig und läuft automatisch ab.</p>
            <p>Viele Grüße<br/>Helix GPO</p>
            """.formatted(partner.firstName(), link);

        notificationApi.send(new com.helix.gpo.web_crm.notification.EmailMessage(toEmail, subject, html));
    }

    void revokeInvitation(UUID invitationId) {
        TestimonialInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found: " + invitationId));
        invitation.revoke();
    }

    // Öffentlicher Einreiche-Vorgang - einzige Legitimation ist der gültige Token
    TestimonialResponse submit(SubmitTestimonialRequest request) {
        String tokenHash = tokenGenerator.hash(request.token());

        TestimonialInvitation invitation = invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or unknown token"));

        if (!invitation.isUsable()) {
            throw new IllegalStateException("Token is expired, already used, or revoked");
        }

        PartnerSummary partner = tenantApi.findPartnerSummaryById(invitation.getPartnerId())
                .orElseThrow(() -> new EntityNotFoundException("Partner not found: " + invitation.getPartnerId()));

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

        return TestimonialMapper.toResponse(testimonialRepository.save(testimonial));
    }

    TestimonialResponse approve(UUID id) {
        Testimonial testimonial = getOrThrow(id);
        testimonial.approve();
        return TestimonialMapper.toResponse(testimonial);
    }

    TestimonialResponse reject(UUID id) {
        Testimonial testimonial = getOrThrow(id);
        testimonial.reject();
        return TestimonialMapper.toResponse(testimonial);
    }

    TestimonialResponse publish(UUID id) {
        Testimonial testimonial = getOrThrow(id);

        if (testimonial.getStatus() != TestimonialStatus.APPROVED) {
            throw new IllegalStateException(
                    "Nur freigegebene Referenzen können auf der Website veröffentlicht werden: " + id);
        }

        if (!testimonial.isVisibleOnWebsite() && testimonialRepository.countByVisibleOnWebsiteTrue() >= MAX_VISIBLE_ON_WEBSITE) {
            throw new IllegalStateException(
                    "Es können maximal " + MAX_VISIBLE_ON_WEBSITE + " Referenzen gleichzeitig auf der Website sichtbar sein");
        }

        testimonial.publish();
        return TestimonialMapper.toResponse(testimonial);
    }

    TestimonialResponse unpublish(UUID id) {
        Testimonial testimonial = getOrThrow(id);
        testimonial.unpublish();
        return TestimonialMapper.toResponse(testimonial);
    }

    @Transactional(readOnly = true)
    List<TestimonialResponse> findAll() {
        return testimonialRepository.findAll().stream()
                .map(TestimonialMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    List<TestimonialResponse> findAllByTenant(UUID tenantId) {
        return testimonialRepository.findAllByTenantId(tenantId).stream()
                .map(TestimonialMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    List<TestimonialResponse> findAllVisibleOnWebsite() {
        return testimonialRepository.findAllByVisibleOnWebsiteTrueOrderByCreatedAtDesc().stream()
                .map(TestimonialMapper::toResponse)
                .toList();
    }

    private Testimonial getOrThrow(UUID id) {
        return testimonialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Testimonial not found: " + id));
    }

}
