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
 * Worker-Klasse, die für das Benachrichtigen des Antragstellers zuständig ist, basierend auf der Entscheidung,
 * die durch das DMN-Modell (Entscheidungsmodell und Notation) im Camunda-Workflow getroffen wurde.
 * Sie sendet entweder eine Genehmigungs- oder Ablehnungsbenachrichtigung an den Antragsteller.
 */
@Component
public class VerantwortlicherWorker {

    private static final Logger logger = LogManager.getLogger(VerantwortlicherWorker.class);
    private final SendMessageService sendMessageService;
    private final EmailSenderService emailSenderService;

    /**
     * Konstruktor für die Dependency Injection der benötigten Services.
     *
     * @param sendMessageService der Service, der für das Senden von Nachrichten zuständig ist
     * @param emailSenderService der Service, der für das Senden von E-Mails zuständig ist
     */
    public VerantwortlicherWorker(SendMessageService sendMessageService, EmailSenderService emailSenderService) {
        this.sendMessageService = sendMessageService;
        this.emailSenderService = emailSenderService;
    }

    /**
     * Verarbeitet den Job "antrag-benachrichtigen", der dafür zuständig ist, den Antragsteller
     * über die Entscheidung zu informieren, basierend auf dem DMN-Entscheidungsergebnis.
     * Es wird eine Genehmigungs- oder Ablehnungsnachricht per E-Mail gesendet.
     *
     * @param client der JobClient, der den Job verwaltet
     * @param job    der aktivierte Job, der bearbeitet wird
     */
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

            String applicantEmail = employeeNumber + "@gmail.com";

            logger.info("Entscheidung aus DMN: {}, EmployeeNumber: {}", decisionResult, employeeNumber);

            // Nachricht basierend auf Entscheidung senden
            if (Boolean.TRUE.equals(decisionResult)) {
                logger.info("Antrag genehmigt. Nachricht wird gesendet...");
                emailSenderService.sendEmailAndPublishMessage(applicantEmail, "Dienstreiseantrag", "Der Antrag wurde genehmigt", "AntragGenehmigt", employeeNumber);
                logger.info("Nachricht 'AntragGenehmigt' wurde erfolgreich gesendet.");
            } else if (Boolean.FALSE.equals(decisionResult)) {
                logger.info("Antrag abgelehnt. Nachricht wird gesendet...");
                emailSenderService.sendEmailAndPublishMessage(applicantEmail, "Dienstreiseantrag", "Ihr Antrag wurde abgelehnt", "AntragAbgelehn", employeeNumber);
                logger.info("Nachricht 'AntragAbgelehnt' wurde erfolgreich gesendet.");
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
