package com.acme.hkaprozesse.service;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service-Klasse zum Senden von E-Mails und Veröffentlichen von Nachrichten an den Camunda-Workflow.
 */
@Service
public class EmailSenderService {

    private static final Logger logger = LogManager.getLogger(EmailSenderService.class);

    private final JavaMailSender mailSender;
    private final ZeebeClient zeebeClient;

    /**
     * Konstruktor, der die Abhängigkeiten für den E-Mail-Versand und den Zeebe-Client injiziert.
     *
     * @param mailSender die Instanz von JavaMailSender zum Senden von E-Mails
     * @param zeebeClient die Instanz von ZeebeClient für die Interaktion mit dem Camunda-Workflow
     */
    public EmailSenderService(JavaMailSender mailSender, ZeebeClient zeebeClient) {
        this.mailSender = mailSender;
        this.zeebeClient = zeebeClient;
    }

    /**
     * Sendet eine E-Mail und veröffentlicht eine Nachricht an den Camunda-Workflow.
     * Falls ein Fehler beim Senden der E-Mail oder Veröffentlichen der Nachricht auftritt,
     * wird der Fehler für die Workflow-Behandlung weitergegeben.
     *
     * @param toEmail        die E-Mail-Adresse des Empfängers
     * @param subject        der Betreff der E-Mail
     * @param body           der Text der E-Mail
     * @param messageName    der Name der Nachricht für den Workflow
     * @param correlationKey der Korrelationsschlüssel für den Workflow
     */
    public void sendEmailAndPublishMessage(String toEmail, String subject, String body, String messageName, String correlationKey) {
        // E-Mail senden
        try {
            sendEmail(toEmail, subject, body);
        } catch (EmailSendException e) {
            logger.error("E-Mail konnte nicht gesendet werden: {}", e.getMessage(), e);
            throw e; // Weitergeben für die Workflow-Behandlung
        }

        // Nachricht an den Workflow veröffentlichen
        try {
            publishMessageToWorkflow(messageName, correlationKey);
        } catch (MessagePublishException e) {
            logger.error("Nachricht konnte nicht veröffentlicht werden: {}", e.getMessage(), e);
            throw e; // Weitergeben für die Workflow-Behandlung
        }
    }

    /**
     * Sendet eine E-Mail an die angegebene E-Mail-Adresse.
     *
     * @param toEmail die E-Mail-Adresse des Empfängers
     * @param subject der Betreff der E-Mail
     * @param body    der Text der E-Mail
     * @throws EmailSendException wenn ein Fehler beim Senden der E-Mail auftritt
     */
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

    /**
     * Veröffentlicht eine Nachricht an den Camunda-Workflow.
     *
     * @param messageName    der Name der Nachricht
     * @param correlationKey der Korrelationsschlüssel für den Workflow
     * @throws MessagePublishException wenn ein Fehler beim Veröffentlichen der Nachricht auftritt
     */
    private void publishMessageToWorkflow(String messageName, String correlationKey) {
        try {
            PublishMessageResponse response = zeebeClient.newPublishMessageCommand()
                    .messageName(messageName)       // Setzt den Namen der Nachricht
                    .correlationKey(correlationKey) // Setzt den Korrelationsschlüssel
                    .send()
                    .join(); // Wartet auf den Abschluss
            logger.info("Nachricht '{}' mit CorrelationKey '{}' wurde erfolgreich veröffentlicht. Message Key: {}",
                    messageName, correlationKey, response.getMessageKey());
        } catch (Exception e) {
            String errorMessage = "Fehler beim Veröffentlichen der Nachricht '" + messageName +
                    "' mit CorrelationKey '" + correlationKey + "': " + e.getMessage();
            throw new MessagePublishException(errorMessage, e);
        }
    }

    // Eigene Ausnahmen zur besseren Fehlerdifferenzierung

    /**
     * Ausnahme, die beim Fehler beim Senden der E-Mail geworfen wird.
     */
    public static class EmailSendException extends RuntimeException {
        public EmailSendException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Ausnahme, die beim Fehler beim Veröffentlichen der Nachricht an den Workflow geworfen wird.
     */
    public static class MessagePublishException extends RuntimeException {
        public MessagePublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
