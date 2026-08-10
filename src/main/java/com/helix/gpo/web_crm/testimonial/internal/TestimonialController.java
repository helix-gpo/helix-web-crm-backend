package com.helix.gpo.web_crm.testimonial.internal;

import com.helix.gpo.web_crm.testimonial.internal.dto.TestimonialDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
class TestimonialController {

    private final TestimonialService testimonialService;

    // ---- CRM (intern, benötigt später JWT-Auth) ----

    @PostMapping("/api/crm/testimonial-invitations")
    InvitationResponse createInvitation(@Valid @RequestBody CreateInvitationRequest request) {
        return testimonialService.createInvitation(request);
    }

    @PostMapping("/api/crm/testimonial-invitations/{id}/revoke")
    void revokeInvitation(@PathVariable UUID id) {
        testimonialService.revokeInvitation(id);
    }

    @GetMapping("/api/crm/testimonials")
    List<TestimonialResponse> findAll(@RequestParam(required = false) UUID tenantId) {
        return tenantId != null ? testimonialService.findAllByTenant(tenantId) : testimonialService.findAll();
    }

    @PostMapping("/api/crm/testimonials/{id}/approve")
    TestimonialResponse approve(@PathVariable UUID id) {
        return testimonialService.approve(id);
    }

    @PostMapping("/api/crm/testimonials/{id}/reject")
    TestimonialResponse reject(@PathVariable UUID id) {
        return testimonialService.reject(id);
    }

    // ---- Öffentlich (Website-Feedback-Formular), Legitimation via Token statt API-Key ----

    @PostMapping("/api/public/testimonials/submit")
    TestimonialResponse submit(@Valid @RequestBody SubmitTestimonialRequest request) {
        return testimonialService.submit(request);
    }

}
