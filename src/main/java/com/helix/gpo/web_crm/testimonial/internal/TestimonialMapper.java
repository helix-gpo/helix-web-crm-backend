package com.helix.gpo.web_crm.testimonial.internal;

import com.helix.gpo.web_crm.testimonial.TestimonialSummary;
import com.helix.gpo.web_crm.testimonial.internal.dto.TestimonialDtos;
import com.helix.gpo.web_crm.testimonial.internal.dto.TestimonialDtos.TestimonialResponse;

final class TestimonialMapper {

    private TestimonialMapper() {
    }

    static TestimonialResponse toResponse(Testimonial testimonial, String partnerPhotoUrl) {
        return new TestimonialResponse(
                testimonial.getId(),
                testimonial.getTenantId(),
                testimonial.getPartnerId(),
                testimonial.getProjectId(),
                testimonial.getPartnerNameSnapshot(),
                testimonial.getPartnerRoleSnapshot(),
                testimonial.getCompanyNameSnapshot(),
                testimonial.getDescription(),
                testimonial.getRating(),
                testimonial.getStatus(),
                testimonial.isVisibleOnWebsite(),
                partnerPhotoUrl,
                testimonial.getCreatedAt()
        );
    }

    static TestimonialSummary toSummary(Testimonial testimonial) {
        return new TestimonialSummary(
                testimonial.getId(),
                testimonial.getPartnerNameSnapshot(),
                testimonial.getPartnerRoleSnapshot(),
                testimonial.getCompanyNameSnapshot(),
                testimonial.getDescription(),
                testimonial.getRating(),
                testimonial.getCreatedAt()
        );
    }

    static TestimonialDtos.InvitationSummaryResponse toSummaryResponse(TestimonialInvitation invitation) {
        return new TestimonialDtos.InvitationSummaryResponse(
                invitation.getId(),
                invitation.getPartnerId(),
                invitation.getProjectId(),
                invitation.getStatus(),
                invitation.getSentToEmail(),
                invitation.getSentAt(),
                invitation.getExpiresAt(),
                invitation.getUsedAt(),
                invitation.getCreatedAt()
        );
    }

}
