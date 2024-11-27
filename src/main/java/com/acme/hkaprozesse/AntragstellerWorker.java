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
        String name = variables.get("name").toString(); // String
        String department = variables.get("department").toString(); // String
        String position = variables.get("position").toString(); // String
        String employeeNumber = variables.get("employeeNumber").toString(); // String
        String purposeOfTrip = variables.get("purposeOfTrip").toString(); // String
        Double estimatedCost = Double.valueOf(variables.get("estimatedCost").toString());

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

        // Zugriff auf die Rechnungsdaten aus dem Formularfeld mit dem Key 'textfield_gna92o'
        Double gesamtRechnung = null;
        if (variables.containsKey("textfield_gna92o")) {
            gesamtRechnung = Double.valueOf(variables.get("textfield_gna92o").toString());
        } else {
            logger.error("Fehler: 'textfield_gna92o' wurde nicht in den Prozessvariablen gefunden!");
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Missing form variable 'textfield_gna92o'")
                    .send()
                    .join();
            return;
        }

        // Zusätzliche Prozessvariable z.B. Employee Number
        String employeeNumber = (String) variables.get("employeeNumber");

        logger.info("Rechnungsdaten: GesamtRechnung={}, EmployeeNumber={}", gesamtRechnung, employeeNumber);

        // 2. Nachricht an das Abrechnungswesen senden
        String correlationKey = employeeNumber; // Eindeutiger Schlüssel
        String messageName = "RechnungWeiterleiten"; // Nachrichtentyp

        // Nachricht mit den Daten senden
        String formData = String.format("{\"gesamtRechnung\": %f, \"employeeNumber\": \"%s\"}", gesamtRechnung, employeeNumber);
        sendMessageService.sendMessage(messageName, correlationKey, formData);

        logger.info("Nachricht an das Abrechnungswesen gesendet: MessageName={}, CorrelationKey={}", messageName, correlationKey);

        // 3. Job abschließen
        client.newCompleteCommand(job.getKey()).send().join();

        logger.info("Ende: Rechnung erfolgreich weitergeleitet.");
    }

}
