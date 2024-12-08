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
public class AntragstellerWorker {

    private static final Logger logger = LogManager.getLogger(AntragstellerWorker.class);

    private final SendMessageService sendMessageService;

    public AntragstellerWorker(SendMessageService sendMessageService) {
        this.sendMessageService = sendMessageService;
    }

    @JobWorker(type = "dienstreiseantrag")
    public void handleSaveAndForward(final JobClient client, final ActivatedJob job) {
        logger.info("Start: Formular weiterleiten an den Vorgesetzten");

        // 1. Formulardaten aus Prozessvariablen abrufen
        Map<String, Object> variables = job.getVariablesAsMap();

        // Variablen mit Prüfung auf Existenz und Inhalt
        String name = getVariable(variables, "name", client, job);
        String department = getVariable(variables, "department", client, job);
        String position = getVariable(variables, "position", client, job);
        String employeeNumber = getVariable(variables, "employeeNumber", client, job);
        String purposeOfTrip = getVariable(variables, "purposeOfTrip", client, job);
        Double estimatedCost = getDoubleVariable(variables, "estimatedCost", client, job);

        logger.info("Formulardaten: Name={}, Department={}, Position={}, EmployeeNumber={}, PurposeOfTrip={}, EstimatedCost={}",
                name, department, position, employeeNumber, purposeOfTrip, estimatedCost);

        // 2. Nachricht an den Vorgesetzten senden
        String correlationKey = employeeNumber; // Eindeutiger Schlüssel
        String messageName = "FormularWeiterleitung"; // Nachrichtentyp
        String formData = variables.toString(); // Formulardaten als JSON-String

        sendMessageService.sendMessage(messageName, correlationKey, formData);

        logger.info("Nachricht an den Vorgesetzten gesendet: MessageName={}, CorrelationKey={}", messageName, correlationKey);

        // 3. Job abschließen
        client.newCompleteCommand(job.getKey()).send().join();

        logger.info("Ende: Formular erfolgreich weitergeleitet.");
    }

    @JobWorker(type = "rechnung-eintragen")
    public void handleRechnungEintragen(final JobClient client, final ActivatedJob job) {
        logger.info("Start: Rechnung verarbeiten und weiterleiten an das Abrechnungswesen");

        // 1. Rechnungsdaten aus Prozessvariablen abrufen
        Map<String, Object> variables = job.getVariablesAsMap();

        Double gesamtRechnung = getDoubleVariable(variables, "invoice", client, job); //ursprünglich amount !
        String employeeNumber = getVariable(variables, "employeeNumber", client, job);

        logger.info("Rechnungsdaten: GesamtRechnung={}, EmployeeNumber={}", gesamtRechnung, employeeNumber);

        // 2. Nachricht an das Abrechnungswesen senden
        String correlationKey = employeeNumber; // Eindeutiger Schlüssel
        String messageName = "RechnungWeiterleiten"; // Nachrichtentyp
        String formData = variables.toString();

        // Nachricht mit den Daten senden
//        String formData = String.format("{\"gesamtRechnung\": %f, \"employeeNumber\": \"%s\"}", gesamtRechnung, employeeNumber);
        sendMessageService.sendMessage(messageName, correlationKey, formData);

        logger.info("Nachricht an das Abrechnungswesen gesendet: MessageName={}, CorrelationKey={}", messageName, correlationKey);

        // 3. Job abschließen
        client.newCompleteCommand(job.getKey()).send().join();

        logger.info("Ende: Rechnung erfolgreich weitergeleitet.");
    }

    // Hilfsmethoden für die Prüfung von Variablen
    private String getVariable(Map<String, Object> variables, String key, JobClient client, ActivatedJob job) {
        if (variables.containsKey(key) && variables.get(key) != null) {
            return variables.get(key).toString();
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
