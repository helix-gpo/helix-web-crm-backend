package com.helix.gpo.web_crm.testimonial.internal;

import com.helix.gpo.web_crm.testimonial.internal.dto.TestimonialDtos.TestimonialResponse;

final class TestimonialMapper {

    private TestimonialMapper() {
    }

    static TestimonialResponse toResponse(Testimonial testimonial) {
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
                testimonial.getCreatedAt()
        );
    }

}
