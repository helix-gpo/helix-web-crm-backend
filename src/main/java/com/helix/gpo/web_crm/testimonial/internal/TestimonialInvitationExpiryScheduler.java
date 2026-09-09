package com.helix.gpo.web_crm.testimonial.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
class TestimonialInvitationExpiryScheduler {

    private final TestimonialInvitationRepository invitationRepository;

    // daily cronjob - after invoice cronjob - marks overdue testimonials as expired
    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    void expireInvitations() {
        List<TestimonialInvitation> candidates = invitationRepository
                .findAllByStatusAndExpiresAtBefore(InvitationStatus.PENDING, Instant.now());

        if (candidates.isEmpty()) {
            return;
        }

        candidates.forEach(TestimonialInvitation::expire);
        invitationRepository.saveAll(candidates);

        log.info("{} Einladung(en) automatisch als abgelaufen markiert.", candidates.size());
    }

}
