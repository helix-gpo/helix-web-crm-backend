package com.helix.gpo.web_crm.notification;

// pdf is loaded from storage, so the archived document is sent, not a re-render
public record InvoiceMail(String to, String invoiceNumber, String senderName, String documentKey) implements OutgoingMail {
}
