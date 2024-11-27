package com.acme.hkaprozesse;

import com.acme.hkaprozesse.service.SendMessageService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AbrechnungswesenWorker {

    private static final Logger logger = LogManager.getLogger(AbrechnungswesenWorker.class);

    private final SendMessageService sendMessageService;

    public AbrechnungswesenWorker(SendMessageService sendMessageService) {
        this.sendMessageService = sendMessageService;
    }

    @JobWorker(type = "rechnung-ueberpruefen")
    public void handleRechnungPrüfen(final JobClient client, final ActivatedJob job) {
        logger.info("Start: Rechnung prüfen");

        // 1. Prozessvariablen abrufen
        Map<String, Object> variables = job.getVariablesAsMap();
        String entscheidung = (String) variables.get("entscheidung");
        String rechnungInhalt = (String) variables.get("rechnungInhalt");
        String employeeNumber = (String) variables.get("employeeNumber");

        logger.info("Empfangene Daten: Entscheidung={}, RechnungInhalt={}, EmployeeNumber={}", entscheidung, rechnungInhalt, employeeNumber);

        // 2. Entscheidung verarbeiten
        if ("ja".equalsIgnoreCase(entscheidung)) {
            // Rechnung an die Finanzabteilung weiterleiten
            String messageName = "RechnungFinanzabteilung";
            sendMessageService.sendMessage(messageName, employeeNumber, rechnungInhalt);
            logger.info("Rechnung an Finanzabteilung weitergeleitet.");
        } else {
            // Nachricht an den Antragsteller senden
            String messageName = "RechnungKorrektur";
            sendMessageService.sendMessage(messageName, employeeNumber, "Bitte überprüfen Sie die Rechnung.");
            logger.info("Nachricht an Antragsteller gesendet, Rechnung überprüfen.");
        }

        // 3. Job abschließen
        client.newCompleteCommand(job.getKey()).send().join();
        logger.info("Ende: Rechnung geprüft und verarbeitet.");
    }

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

        sendMessageService.sendMessage(messageName, correlationKey, messageData);

        logger.info("Antragsteller informiert: MessageName={}, CorrelationKey={}", messageName, correlationKey);

        // 4. Job abschließen
        client.newCompleteCommand(job.getKey()).send().join();

        logger.info("Ende: Bestätigung verarbeitet, archiviert und Antragsteller informiert.");
    }
}
