package com.acme.hkaprozesse;

import com.acme.hkaprozesse.service.EmailSenderService;
import com.acme.hkaprozesse.service.SendMessageService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FinanzabteilungWorker {

    private static final Logger logger = LogManager.getLogger(FinanzabteilungWorker.class);
    private final SendMessageService sendMessageService;
    private final EmailSenderService emailSenderService;

    public FinanzabteilungWorker(SendMessageService sendMessageService,EmailSenderService emailSenderService) {
        this.sendMessageService = sendMessageService;
        this.emailSenderService = emailSenderService;
    }

    @JobWorker(type = "ueberweisungdurchfuehren")
    public void handleUeberweisungDurchfuehren(final JobClient client, final ActivatedJob job) {
        logger.info("Start: Überweisung durchführen und Bestätigung senden");

        // 1. Prozessvariablen abrufen
        Map<String, Object> variables = job.getVariablesAsMap();
          String rechnungsbetrag = (String) variables.get("invoice");


        logger.info("Rechnungsdaten erhalten: Betrag={}, Kontoinhaber={}, Kontonummer={}",
                rechnungsbetrag);

        // 2. Überweisung durchführen (Simulation)
        logger.info("Überweisung von {} Euro an Konto {} (Inhaber: {}) wird durchgeführt...",
                rechnungsbetrag);

        // 3. Bestätigung an das Abrechnungswesen senden
        String employeeNumber = (String) variables.get("employeeNumber"); // Eindeutiger Schlüssel
        String messageName = "UeberweisungBestaetigung"; // Nachrichtentyp
        String confirmationData = variables.toString();

        sendMessageService.sendMessage(messageName, employeeNumber, confirmationData);
        emailSenderService.sendEmailAndPublishMessage(
                "kodo2101@gmail",
                "Dienstreiseantrag",
                "Das Geld wurde an "+ employeeNumber+" gesendet.",
                messageName,
                employeeNumber
                );
        logger.info("Bestätigung an das Abrechnungswesen gesendet: MessageName={}, CorrelationKey={}", messageName, employeeNumber);

        // 4. Job abschließen
        client.newCompleteCommand(job.getKey()).send().join();
        logger.info("Ende: Überweisung erfolgreich durchgeführt und Bestätigung gesendet.");
    }
    private Double getDoubleVariable(Map<String, Object> variables, String key, JobClient client, ActivatedJob job) {
        if (variables.containsKey(key) && variables.get(key) != null) {
            try {
                return Double.valueOf(variables.get(key).toString());
            } catch (NumberFormatException e) {
                logger.error("Fehler: '{}' ist keine gültige Zahl!", key);
                client.newFailCommand(job.getKey())
                        .retries(job.getRetries() - 1)
                        .errorMessage("Invalid number format for variable: " + key)
                        .send()
                        .join();
                throw new IllegalStateException("Ungültiges Zahlenformat für Variable: " + key);
            }
        } else {
            logger.error("Fehler: '{}' wurde nicht in den Prozessvariablen gefunden!", key);
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Missing process variable: " + key)
                    .send()
                    .join();
            throw new IllegalStateException("Prozessvariable fehlt: " + key);
        }
    }

}
