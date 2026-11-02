package com.helix.gpo.web_crm.notification.internal;

import com.helix.gpo.web_crm.notification.OutgoingMail;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

// runs after the publishing transaction committed, in its own thread;
// an exception keeps the publication incomplete so it gets retried
@Component
@RequiredArgsConstructor
class OutgoingMailListener {

    private final EmailService emailService;

    @ApplicationModuleListener
    void on(OutgoingMail mail) {
        emailService.send(mail);
    }

}
