package com.helix.gpo.web_crm.testimonial.internal;

import com.helix.gpo.web_crm.notification.EmailMessage;
import com.helix.gpo.web_crm.notification.NotificationApi;
import com.helix.gpo.web_crm.testimonial.internal.config.WebsiteProperties;
import com.helix.gpo.web_crm.testimonial.internal.dto.TestimonialDtos.*;
import com.helix.gpo.web_crm.tenant.PartnerSummary;
import com.helix.gpo.web_crm.tenant.TenantApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
                .orElseThrow(() -> new EntityNotFoundException("Dieser Ansprechpartner wurde nicht gefunden."));

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

        // rawToken existiert ab jetzt NUR noch in diesem Response - nirgends persistiert
        return new InvitationResponse(invitation.getId(), rawToken, expiresAt, sendEmail, sentToEmail);
    }

    @Transactional(readOnly = true)
    List<InvitationSummaryResponse> findInvitationsByTenant(UUID tenantId) {
        return invitationRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(TestimonialMapper::toSummaryResponse)
                .toList();
    }

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
        Testimonial testimonial = getOrThrow(id);
        testimonial.approve();
        return toResponse(testimonial);
    }

    TestimonialResponse reject(UUID id) {
        Testimonial testimonial = getOrThrow(id);
        testimonial.reject();
        return toResponse(testimonial);
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
        return toResponse(testimonial);
    }

    TestimonialResponse unpublish(UUID id) {
        Testimonial testimonial = getOrThrow(id);
        testimonial.unpublish();
        return toResponse(testimonial);
    }

    @Transactional(readOnly = true)
    List<TestimonialResponse> findAll() {
        return testimonialRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    List<TestimonialResponse> findAllByTenant(UUID tenantId) {
        return testimonialRepository.findAllByTenantId(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

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

    // Partner kann zwischenzeitlich gelöscht worden sein - dann einfach kein
    // Foto anzeigen (Snapshot-Felder wie Name/Rolle bleiben trotzdem stabil),
    // statt die ganze Testimonial-Anzeige fehlschlagen zu lassen
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
        TestimonialInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Diese Einladung wurde nicht gefunden."));
        invitation.revoke();
    }

}
