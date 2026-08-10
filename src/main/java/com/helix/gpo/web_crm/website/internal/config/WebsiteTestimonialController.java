package com.helix.gpo.web_crm.website.internal;

import com.helix.gpo.web_crm.testimonial.TestimonialApi;
import com.helix.gpo.web_crm.testimonial.TestimonialSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
class WebsiteTestimonialController {

    private final TestimonialApi testimonialApi;

    @GetMapping("/api/public/testimonials")
    List<TestimonialSummary> findVisibleTestimonials() {
        return testimonialApi.findAllVisibleOnWebsite();
    }

}
