package com.helix.gpo.web_crm.notification;

public sealed interface OutgoingMail permits EmployeeInvitationMail, TestimonialInvitationMail, InvoiceMail {
    String to();
}
