package com.helix.gpo.web_crm.notification.internal;

import com.helix.gpo.web_crm.notification.EmailAttachment;
import com.helix.gpo.web_crm.notification.EmailMessage;
import com.helix.gpo.web_crm.notification.NotificationApi;
import com.helix.gpo.web_crm.notification.internal.config.NotificationProperties;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.RawMessage;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Component
@RequiredArgsConstructor
class EmailService implements NotificationApi {

    private final SesV2Client sesV2Client;
    private final NotificationProperties properties;

    @Override
    public void send(EmailMessage message) {
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
        } catch (UnsupportedEncodingException e) {
            throw new NotificationException("Ungültige Absenderadresse konfiguriert", e);
        }

        mimeMessage.setRecipients(Message.RecipientType.TO, message.to());
        mimeMessage.setSubject(message.subject(), "UTF-8");

        MimeMultipart multipart = new MimeMultipart("mixed");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(message.htmlBody(), "text/html; charset=UTF-8");
        multipart.addBodyPart(htmlPart);

        for (EmailAttachment attachment : message.attachments()) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setFileName(attachment.filename());
            attachmentPart.setContent(attachment.content(), attachment.contentType());
            multipart.addBodyPart(attachmentPart);
        }

        mimeMessage.setContent(multipart);
        return mimeMessage;
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
