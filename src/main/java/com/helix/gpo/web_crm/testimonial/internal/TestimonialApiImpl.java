package com.helix.gpo.web_crm.testimonial.internal;

import com.helix.gpo.web_crm.testimonial.TestimonialApi;
import com.helix.gpo.web_crm.testimonial.TestimonialSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class TestimonialApiImpl implements TestimonialApi {

    private final TestimonialRepository testimonialRepository;

    @Override
    public List<TestimonialSummary> findAllVisibleOnWebsite() {
        return testimonialRepository.findAllByVisibleOnWebsiteTrueOrderByCreatedAtDesc(true).stream()
                .map(TestimonialMapper::toSummary)
                .toList();
    }

}
