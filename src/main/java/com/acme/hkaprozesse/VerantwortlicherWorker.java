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

    @JobWorker(type = "antrag-benachrichtigen")
    public void handleAntragBenachrichtigung(final JobClient client, final ActivatedJob job) {
        logger.info("Start: Antragsteller benachrichtigen");

        try {
            // Prozessvariablen abrufen
            Map<String, Object> variables = job.getVariablesAsMap();
            logger.info("Prozessvariablen: {}", variables);

            // DMN-Result aus Prozessvariablen abrufen
            Boolean decisionResult = (Boolean) variables.get("eligibleTravel"); // Genehmigt oder Abgelehnt
            String employeeNumber = (String) variables.get("employeeNumber"); // Korrelationsschlüssel

            logger.info("Entscheidung aus DMN: {}, EmployeeNumber: {}", decisionResult, employeeNumber);

            // Nachricht basierend auf Entscheidung senden
            if (Boolean.TRUE.equals(decisionResult)) {
                logger.info("Antrag genehmigt. Nachricht wird gesendet...");
                sendMessageService.sendMessage("AntragGenehmigt", employeeNumber, "Ihr Antrag wurde genehmigt.");
                logger.info("Nachricht 'AntragGenehmigt' wurde erfolgreich gesendet.");
            } else if (Boolean.FALSE.equals(decisionResult)) {
                logger.info("Antrag abgelehnt. Nachricht wird gesendet...");
                sendMessageService.sendMessage("AntragAbgelehnt", employeeNumber, "Ihr Antrag wurde abgelehnt.");
                logger.info("Nachricht 'AntragAbgelehnt' wurde erfolgreich gesendet.");
            } else {
                logger.warn("Unbekannte Entscheidung: {}", decisionResult);
                sendMessageService.sendMessage("UnbekannteEntscheidung", employeeNumber, "Es gab ein Problem bei der Verarbeitung Ihres Antrags.");
                logger.info("Nachricht 'UnbekannteEntscheidung' wurde gesendet.");
            }

            // Job abschließen
            client.newCompleteCommand(job.getKey()).variables(variables).send().join();
            logger.info("Ende: Antragsteller benachrichtigt.");

        } catch (Exception e) {
            logger.error("Fehler bei der Verarbeitung des Jobs: {}", e.getMessage(), e);
            throw e; // Fehler weiterwerfen, damit er im Prozess sichtbar wird
        }
    }
}
