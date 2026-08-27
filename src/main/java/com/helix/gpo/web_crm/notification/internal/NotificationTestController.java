package com.helix.gpo.web_crm.notification.internal;

import com.helix.gpo.web_crm.notification.EmailMessage;
import com.helix.gpo.web_crm.notification.NotificationApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class NotificationTestController {

    private final NotificationApi notificationApi;

    @PostMapping("/api/public/notifications/test")
    void test(@RequestParam String to) {
        notificationApi.send(new EmailMessage(
                to,
                "Helix GPO CRM – Testmail",
                "Test der neuen E-Mail-Vorlage",
                "<p>Wenn du das liest, funktioniert der SES-Versand über die verifizierte Domain.</p>"
        ));
    }

}
