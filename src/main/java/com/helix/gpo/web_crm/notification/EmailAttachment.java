package com.helix.gpo.web_crm.notification;

public record EmailAttachment(
        String filename,
        byte[] content,
        String contentType
) {
}