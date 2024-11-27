package com.acme.hkaprozesse.worker;

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

    public FinanzabteilungWorker(SendMessageService sendMessageService) {
        this.sendMessageService = sendMessageService;
    }

    @JobWorker(type = "ueberweisung-durchfuehren")
    public void handleUeberweisungDurchfuehren(final JobClient client, final ActivatedJob job) {
        logger.info("Start: Überweisung durchführen und Bestätigung senden");

        // 1. Prozessvariablen abrufen
        Map<String, Object> variables = job.getVariablesAsMap();
        String rechnungsbetrag = (String) variables.get("gesamtRechnung");
        String kontoinhaber = (String) variables.get("kontoinhaber");
        String kontonummer = (String) variables.get("kontonummer");

        logger.info("Rechnungsdaten erhalten: Betrag={}, Kontoinhaber={}, Kontonummer={}",
                rechnungsbetrag, kontoinhaber, kontonummer);

        // 2. Überweisung durchführen (Simulation)
        logger.info("Überweisung von {} Euro an Konto {} (Inhaber: {}) wird durchgeführt...",
                rechnungsbetrag, kontonummer, kontoinhaber);

        // 3. Bestätigung an das Abrechnungswesen senden
        String correlationKey = (String) variables.get("employeeNumber"); // Eindeutiger Schlüssel
        String messageName = "UeberweisungBestaetigung"; // Nachrichtentyp
        String confirmationData = "{\"status\": \"success\", \"betrag\": \"" + rechnungsbetrag + "\"}";

        sendMessageService.sendMessage(messageName, correlationKey, confirmationData);
        logger.info("Bestätigung an das Abrechnungswesen gesendet: MessageName={}, CorrelationKey={}", messageName, correlationKey);

        // 4. Job abschließen
        client.newCompleteCommand(job.getKey()).send().join();
        logger.info("Ende: Überweisung erfolgreich durchgeführt und Bestätigung gesendet.");
    }

}
