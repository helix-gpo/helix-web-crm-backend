package com.helix.gpo.web_crm.notification;

import java.util.List;

public record EmailMessage(
        String to,
        String subject,
        String htmlBody,
        List<EmailAttachment> attachments
) {

    public EmailMessage {
        attachments = attachments != null ? List.copyOf(attachments) : List.of();
    }

    public EmailMessage(String to, String subject, String htmlBody) {
        this(to, subject, htmlBody, List.of());
    }

}
