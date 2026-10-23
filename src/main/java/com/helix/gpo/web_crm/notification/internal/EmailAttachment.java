package com.helix.gpo.web_crm.notification.internal;

public record EmailAttachment(
        String filename,
        byte[] content,
        String contentType
) {
}