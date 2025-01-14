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

/**
 * Worker-Klasse für die Bearbeitung des Prozesses "Überweisung durchführen" im Camunda-Workflow.
 * Diese Klasse ist zuständig für das Simulieren einer Überweisung und das Versenden einer Bestätigung an das Abrechnungswesen.
 */
@Component
public class FinanzabteilungWorker {

    private static final Logger logger = LogManager.getLogger(FinanzabteilungWorker.class);
    private final SendMessageService sendMessageService;
    private final EmailSenderService emailSenderService;

    /**
     * Konstruktor, der die benötigten Service-Klassen injiziert.
     *
     * @param sendMessageService    der Service für das Senden von Nachrichten
     * @param emailSenderService    der Service für das Senden von E-Mails
     */
    public FinanzabteilungWorker(SendMessageService sendMessageService, EmailSenderService emailSenderService) {
        this.sendMessageService = sendMessageService;
        this.emailSenderService = emailSenderService;
    }

    /**
     * Verarbeitet den Job "ueberweisungdurchfuehren", der für das Durchführen einer Überweisung und das Senden einer Bestätigung zuständig ist.
     * Es werden Rechnungsdaten abgerufen und eine Bestätigung an das Abrechnungswesen sowie eine E-Mail gesendet.
     *
     * @param client der JobClient, der den Job verwaltet
     * @param job    der aktivierte Job, der bearbeitet wird
     */
    @JobWorker(type = "ueberweisungdurchfuehren")
    public void handleUeberweisungDurchfuehren(final JobClient client, final ActivatedJob job) {
        logger.info("Start: Überweisung durchführen und Bestätigung senden");

        // 1. Prozessvariablen abrufen
        Map<String, Object> variables = job.getVariablesAsMap();
        String invoiceAmount = (String) variables.get("invoice");

        logger.info("Rechnungsdaten erhalten: Betrag={}", invoiceAmount);

        // 2. Überweisung durchführen (Simulation)
        logger.info("Überweisung von {} Euro an Konto {} (Inhaber: {}) wird durchgeführt...", invoiceAmount);

        // 3. Bestätigung an das Abrechnungswesen senden
        String employeeNumber = (String) variables.get("employeeNumber"); // Eindeutiger Schlüssel
        String messageName = "UeberweisungBestaetigung"; // Nachrichtentyp
        String confirmationData = variables.toString();

        sendMessageService.sendMessage(messageName, employeeNumber, confirmationData);
        emailSenderService.sendEmailAndPublishMessage(
                "kodo2101@gmail",
                "Dienstreiseantrag",
                "Das Geld wurde an " + employeeNumber + " gesendet.",
                messageName,
                employeeNumber
        );
        logger.info("Bestätigung an das Abrechnungswesen gesendet: MessageName={}, CorrelationKey={}", messageName, employeeNumber);

        // 4. Job abschließen
        client.newCompleteCommand(job.getKey()).send().join();
        logger.info("Ende: Überweisung erfolgreich durchgeführt und Bestätigung gesendet.");
    }

    /**
     * Hilfsmethode zum Abrufen einer Double-Variable aus den Prozessvariablen und zum Überprüfen des Formats.
     * Wenn die Variable fehlt oder das Format ungültig ist, wird der Job als fehlgeschlagen markiert.
     *
     * @param variables die Prozessvariablen
     * @param key       der Name der Variable
     * @param client    der JobClient
     * @param job       der aktivierte Job
     * @return der Wert der Double-Variable
     * @throws IllegalStateException wenn die Variable fehlt oder das Format ungültig ist
     */
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
