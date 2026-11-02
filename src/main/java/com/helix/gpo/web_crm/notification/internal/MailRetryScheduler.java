package com.helix.gpo.web_crm.notification.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
class MailRetryScheduler {

    private final FailedEventPublications failedEventPublications;

    // retries failed mails every 10 minutes for up to one day, then gives up
    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
    void retryFailedMails() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(1));
        failedEventPublications.resubmit(ResubmissionOptions.defaults()
                .withMinAge(Duration.ofMinutes(5))
                .withFilter(publication -> publication.getPublicationDate().isAfter(cutoff)));
    }

}
