package com.helix.gpo.web_crm.notification;

import java.util.List;

public record EmailMessage(
        String to,
        String subject,
        String preheader,
        String bodyHtml,
        List<EmailAttachment> attachments
) {
    public EmailMessage {
        attachments = attachments != null ? List.copyOf(attachments) : List.of();
    }

    public EmailMessage(String to, String subject, String preheader, String bodyHtml) {
        this(to, subject, preheader, bodyHtml, List.of());
    }
}
