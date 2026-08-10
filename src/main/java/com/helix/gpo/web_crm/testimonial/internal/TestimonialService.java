package com.helix.gpo.web_crm.testimonial.internal;

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

    private final TestimonialInvitationRepository invitationRepository;
    private final TestimonialRepository testimonialRepository;
    private final TokenGenerator tokenGenerator;
    private final TenantApi tenantApi;

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

        // rawToken existiert ab jetzt NUR noch in diesem Response - nirgends persistiert
        return new InvitationResponse(invitation.getId(), rawToken, expiresAt);
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
        return testimonialRepository.findAllByVisibleOnWebsiteTrue().stream()
                .map(TestimonialMapper::toResponse)
                .toList();
    }

    private Testimonial getOrThrow(UUID id) {
        return testimonialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Testimonial not found: " + id));
    }

}
