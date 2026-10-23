package com.helix.gpo.web_crm.notification;

public record EmployeeInvitationMail(String to, String firstName, String temporaryPassword) implements OutgoingMail {
}
