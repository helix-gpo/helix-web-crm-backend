package com.helix.gpo.web_crm.notification.internal;

import com.helix.gpo.web_crm.notification.*;
import com.helix.gpo.web_crm.notification.internal.config.NotificationProperties;
import com.helix.gpo.web_crm.storage.StorageApi;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.RawMessage;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

@Component
@RequiredArgsConstructor
class EmailService implements NotificationApi {

    private final SesV2Client sesV2Client;
    private final NotificationProperties properties;
    private final TemplateEngine templateEngine;
    private final StorageApi storageApi;

    @Override
    public void send(OutgoingMail mail) {
        switch (mail) {
            case EmployeeInvitationMail m -> sendRaw(new EmailMessage(m.to(),
                    "Dein Zugang zum Helix GPO CRM",
                    "Dein CRM-Zugang wurde eingerichtet.",
                    render("mail/employee-invitation",
                            "firstName", m.firstName(), "email", m.to(), "temporaryPassword", m.temporaryPassword())));
            case TestimonialInvitationMail m -> sendRaw(new EmailMessage(m.to(),
                    "Wir würden uns über Ihre Referenz freuen – " + m.companyName(),
                    "Teilen Sie in wenigen Minuten Ihre Erfahrung mit uns.",
                    render("mail/testimonial-invitation", "firstName", m.firstName(), "link", m.link())));
            case InvoiceMail m -> sendRaw(new EmailMessage(m.to(),
                    "Rechnung " + m.invoiceNumber() + " – " + m.senderName(),
                    "Ihre Rechnung " + m.invoiceNumber() + " finden Sie im Anhang.",
                    render("mail/invoice", "invoiceNumber", m.invoiceNumber(), "senderName", m.senderName()),
                    List.of(new EmailAttachment(m.invoiceNumber() + ".pdf",
                            storageApi.download(m.documentKey()), "application/pdf"))));
        }
    }

    // key/value pairs instead of Map.of, because values like firstName may be null
    private String render(String template, Object... keyValues) {
        Context context = new Context(Locale.GERMAN);
        for (int i = 0; i < keyValues.length; i += 2) {
            context.setVariable((String) keyValues[i], keyValues[i + 1]);
        }
        return templateEngine.process(template, context);
    }

    private void sendRaw(EmailMessage message) {
        try {
            MimeMessage mimeMessage = buildMimeMessage(message);
            sesV2Client.sendEmail(toSendRequest(mimeMessage));
        } catch (MessagingException e) {
            throw new NotificationException("Konnte E-Mail nicht aufbauen: " + message.subject(), e);
        }
    }

    private MimeMessage buildMimeMessage(EmailMessage message) throws MessagingException {
        Session session = Session.getInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);

        try {
            mimeMessage.setFrom(new InternetAddress(properties.senderEmail(), properties.senderName(), "UTF-8"));
            if (properties.replyToEmail() != null && !properties.replyToEmail().isBlank()) {
                mimeMessage.setReplyTo(new InternetAddress[]{
                        new InternetAddress(properties.replyToEmail())
                });
            }
        } catch (java.io.UnsupportedEncodingException e) {
            throw new NotificationException("Ungültige Absenderadresse konfiguriert", e);
        }

        mimeMessage.setRecipients(Message.RecipientType.TO, message.to());
        mimeMessage.setSubject(message.subject(), "UTF-8");

        String fullHtml = EmailLayout.wrap(message.preheader(), message.bodyHtml());

        MimeMultipart relatedMultipart = new MimeMultipart("related");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(fullHtml, "text/html; charset=UTF-8");
        relatedMultipart.addBodyPart(htmlPart);

        MimeBodyPart logoPart = new MimeBodyPart();
        logoPart.setContent(loadLogoBytes(), "image/png");
        logoPart.setContentID("<logo>");
        logoPart.setDisposition(MimeBodyPart.INLINE);
        logoPart.setFileName("logo.png");
        relatedMultipart.addBodyPart(logoPart);

        if (message.attachments().isEmpty()) {
            mimeMessage.setContent(relatedMultipart);
        } else {
            MimeMultipart mixedMultipart = new MimeMultipart("mixed");

            MimeBodyPart relatedWrapper = new MimeBodyPart();
            relatedWrapper.setContent(relatedMultipart);
            mixedMultipart.addBodyPart(relatedWrapper);

            for (EmailAttachment attachment : message.attachments()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setFileName(attachment.filename());
                attachmentPart.setContent(attachment.content(), attachment.contentType());
                mixedMultipart.addBodyPart(attachmentPart);
            }

            mimeMessage.setContent(mixedMultipart);
        }

        return mimeMessage;
    }

    private byte[] loadLogoBytes() {
        try (InputStream in = new ClassPathResource("notification/logo.png").getInputStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new NotificationException("Logo-Datei konnte nicht geladen werden", e);
        }
    }

    private SendEmailRequest toSendRequest(MimeMessage mimeMessage) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mimeMessage.writeTo(outputStream);

            return SendEmailRequest.builder()
                    .content(content -> content.raw(
                            RawMessage.builder()
                                    .data(SdkBytes.fromByteArray(outputStream.toByteArray()))
                                    .build()
                    ))
                    .build();
        } catch (MessagingException | IOException e) {
            throw new NotificationException("Konnte E-Mail nicht serialisieren", e);
        }
    }

}
