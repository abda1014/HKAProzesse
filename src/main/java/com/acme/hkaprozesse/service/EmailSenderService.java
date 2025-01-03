package com.acme.hkaprozesse.service;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    private static final Logger logger = LogManager.getLogger(EmailSenderService.class);

    private final JavaMailSender mailSender;
    private final ZeebeClient zeebeClient;

    public EmailSenderService(JavaMailSender mailSender, ZeebeClient zeebeClient) {
        this.mailSender = mailSender;
        this.zeebeClient = zeebeClient;
    }

    /**
     * Sende eine Email udn veröffentliche eine Nachricht an den Camunda workflow
     *
     * @param toEmail         Recipient email address
     * @param subject         Email subject
     * @param body            Email body
     * @param messageName     The message name for the workflow
     * @param correlationKey  The correlation key for the workflow
     */
    public void sendEmailAndPublishMessage(String toEmail, String subject, String body, String messageName, String correlationKey) {
        // Send email
        try {
            sendEmail(toEmail, subject, body);
        } catch (EmailSendException e) {
            logger.error("E-Mail konnte nicht gesendet werden: {}", e.getMessage(), e);
            throw e; // Re-throw for workflow handling
        }

        // Publish message to workflow
        try {
            publishMessageToWorkflow(messageName, correlationKey);
        } catch (MessagePublishException e) {
            logger.error("Nachricht konnte nicht veröffentlicht werden: {}", e.getMessage(), e);
            throw e; // Re-throw for workflow handling
        }
    }

    private void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("davidabraaaham123@gmail.com");
            message.setTo(toEmail);
            message.setText(body);
            message.setSubject(subject);
            mailSender.send(message);
            logger.info("E-Mail erfolgreich an {} gesendet.", toEmail);
        } catch (Exception e) {
            String errorMessage = "Fehler beim Senden der E-Mail an " + toEmail + ": " + e.getMessage();
            throw new EmailSendException(errorMessage, e);
        }
    }

    private void publishMessageToWorkflow(String messageName, String correlationKey) {
        try {
            PublishMessageResponse response = zeebeClient.newPublishMessageCommand()
                    .messageName(messageName)       // Set the name of the message
                    .correlationKey(correlationKey) // Set the correlation key
                    .send()
                    .join(); // Wait for completion
            logger.info("Nachricht '{}' mit CorrelationKey '{}' wurde erfolgreich veröffentlicht. Message Key: {}",
                    messageName, correlationKey, response.getMessageKey());
        } catch (Exception e) {
            String errorMessage = "Fehler beim Veröffentlichen der Nachricht '" + messageName +
                    "' mit CorrelationKey '" + correlationKey + "': " + e.getMessage();
            throw new MessagePublishException(errorMessage, e);
        }
    }

    // Custom exceptions for better error differentiation
    public static class EmailSendException extends RuntimeException {
        public EmailSendException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class MessagePublishException extends RuntimeException {
        public MessagePublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
