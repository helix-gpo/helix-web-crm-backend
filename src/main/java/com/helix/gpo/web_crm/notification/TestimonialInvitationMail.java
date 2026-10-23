package com.helix.gpo.web_crm.notification;

public record TestimonialInvitationMail(String to, String firstName, String companyName, String link) implements OutgoingMail {
}
