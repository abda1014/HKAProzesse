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
public class VerantwortlicherWorker {

    private static final Logger logger = LogManager.getLogger(VerantwortlicherWorker.class);
    private final SendMessageService sendMessageService;

    // Konstruktor für Dependency Injection des SendMessageService
    public VerantwortlicherWorker(SendMessageService sendMessageService) {
        this.sendMessageService = sendMessageService;
    }

    @JobWorker(type = "antrag-pruefen")
    public void handleAntragPruefen(final JobClient client, final ActivatedJob job) {
        logger.info("Start: Antrag prüfen");

        // Prozessvariablen abrufen
        Map<String, Object> variables = job.getVariablesAsMap();
        String decisionResult = (String) variables.get("decisionResult"); // Ergebnis der DMN-Tabelle
        String employeeNumber = (String) variables.get("employeeNumber"); // Korrelationsschlüssel
        String formData = variables.toString(); // Alle Variablen als JSON-String

        logger.info("DMN-Entscheidung: {}, EmployeeNumber: {}", decisionResult, employeeNumber);

        // Entscheidung basierend auf dem DMN-Result
        if ("Akzeptiert".equalsIgnoreCase(decisionResult)) {
            logger.info("Antrag akzeptiert. Nachricht an Antragsteller wird gesendet...");
            sendMessageService.sendMessage("AntragGenehmigt", employeeNumber, formData);
        } else if ("Abgelehnt".equalsIgnoreCase(decisionResult)) {
            logger.info("Antrag abgelehnt. Nachricht an Antragsteller wird gesendet...");
            sendMessageService.sendMessage("AntragAbgelehnt", employeeNumber, formData);
        } else {
            logger.warn("Unbekannte Entscheidung: {}", decisionResult);
        }

        // Job abschließen
        client.newCompleteCommand(job.getKey()).send().join();
        logger.info("Ende: Antrag geprüft und Entscheidung weitergeleitet.");
    }
}
