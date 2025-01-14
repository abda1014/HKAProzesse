package com.acme.hkaprozesse;

import com.acme.hkaprozesse.service.AccountingRoleService;
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
 * Worker-Klasse, die mit Camunda Zeebe zusammenarbeitet, um Geschäftsprozesse zu bearbeiten.
 * Sie verarbeitet Rechnungsprüfungen und Bestätigungen, sendet E-Mails und führt Archivierungsprozesse durch.
 */
@Component
public class AbrechnungswesenWorker {

    private static final Logger logger = LogManager.getLogger(AbrechnungswesenWorker.class);

    private final EmailSenderService emailSenderService;
    private final AccountingRoleService accountingRoleService;

    /**
     * Konstruktor, der die benötigten Service-Klassen injiziert.
     *
     * @param sendMessageService   die Instanz von SendMessageService, die für die Nachrichtenverarbeitung verantwortlich ist
     * @param emailSenderService   die Instanz von EmailSenderService, die für das Senden von E-Mails zuständig ist
     * @param accountingRoleService die Instanz von AccountingRoleService, die für das Abrufen der E-Mail der Finanzabteilung zuständig ist
     */
    public AbrechnungswesenWorker(SendMessageService sendMessageService, EmailSenderService emailSenderService, AccountingRoleService accountingRoleService) {
        this.emailSenderService = emailSenderService;
        this.accountingRoleService = accountingRoleService;
    }

    /**
     * Verarbeitet den Job "rechnung-ueberpruefen", der für die Prüfung und Weiterleitung einer Rechnung zuständig ist.
     * Je nach Entscheidung wird die Rechnung entweder an die Finanzabteilung weitergeleitet oder eine Nachricht an den Antragsteller gesendet.
     *
     * @param client der JobClient, der den Job verwaltet
     * @param job    der aktivierte Job, der bearbeitet wird
     * @throws Exception wenn ein Fehler beim Verarbeiten des Jobs auftritt
     */
    @JobWorker(type = "rechnung-ueberpruefen")
    public void handleRechnungPrüfen(final JobClient client, final ActivatedJob job) throws Exception {
        logger.info("Start: Rechnung prüfen");

        // 1. Prozessvariablen abrufen
        Map<String, Object> variables = job.getVariablesAsMap();
        Boolean entscheidung = (Boolean) variables.get("isProcess");
        String rechnungInhalt = (String) variables.get("invoice");
        String employeeNumber = (String) variables.get("employeeNumber");

        // E-Mail der Person im Abrechnungswesen ermitteln
        String emailAccounting = accountingRoleService.getAccountingEmail("RA", employeeNumber);

        logger.info("Empfangene Daten: Entscheidung={}, RechnungInhalt={}, EmployeeNumber={}", entscheidung, rechnungInhalt, employeeNumber);

        // 2. Entscheidung verarbeiten
        if (Boolean.TRUE.equals(entscheidung)) {
            // Rechnung an die Finanzabteilung weiterleiten
            String messageName = "RechnungFinanzabteilung";
            emailSenderService.sendEmailAndPublishMessage(
                    "scdo0008@gmail.com",
                    "Dienstreiseantrag von " + employeeNumber,
                    "Der Betrag " + rechnungInhalt + " € soll an " + employeeNumber + " überwiesen werden.",
                    messageName,
                    employeeNumber
            );
            logger.info("Rechnung an Finanzabteilung weitergeleitet.");
        } else {
            // Nachricht an den Antragsteller senden
            String messageName = "RechnungKorrektur";
            emailSenderService.sendEmailAndPublishMessage(
                    employeeNumber + "@gmail.com",
                    "Korrektur der Rechnung",
                    "Überprüfen Sie bitte nochmal die Rechnung.",
                    messageName,
                    employeeNumber
            );
            logger.info("Nachricht an Antragsteller gesendet, Rechnung überprüfen.");
        }

        // 3. Job abschließen
        client.newCompleteCommand(job.getKey()).send().join();
        logger.info("Ende: Rechnung geprüft und verarbeitet.");
    }

    /**
     * Verarbeitet den Job "bestaetigung-erhalten", der für das Erhalten der Bestätigung und die Archivierung des Vorgangs zuständig ist.
     * Informiert den Antragsteller nach der Archivierung und schließt den Job ab.
     *
     * @param client der JobClient, der den Job verwaltet
     * @param job    der aktivierte Job, der bearbeitet wird
     */
    @JobWorker(type = "bestaetigung-erhalten")
    public void handleBestaetigungErhalten(final JobClient client, final ActivatedJob job) {
        logger.info("Start: Bestätigung erhalten und Archivierung beginnen");

        // 1. Bestätigungsdaten aus Prozessvariablen abrufen
        Map<String, Object> variables = job.getVariablesAsMap();
        String status = (String) variables.get("status");
        String betrag = (String) variables.get("betrag");
        String employeeNumber = (String) variables.get("employeeNumber");

        logger.info("Bestätigungsdaten: Status={}, Betrag={}, EmployeeNumber={}", status, betrag, employeeNumber);

        // 2. Archivierung simulieren
        logger.info("Archivierung für 10 Jahre wird durchgeführt...");

        // 3. Antragsteller informieren
        String correlationKey = employeeNumber; // Eindeutiger Schlüssel
        String messageName = "AntragstellerInformieren"; // Nachrichtentyp
        String messageData = "{\"status\": \"" + status + "\", \"betrag\": \"" + betrag + "\"}";

        logger.info("Antragsteller informiert: MessageName={}, CorrelationKey={}", messageName, correlationKey);

        // 4. Job abschließen
        client.newCompleteCommand(job.getKey()).send().join();

        logger.info("Ende: Bestätigung verarbeitet, archiviert und Antragsteller informiert.");
    }
}
