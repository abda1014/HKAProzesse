package com.acme.hkaprozesse;

import com.acme.hkaprozesse.service.AccountingRoleService;
import com.acme.hkaprozesse.service.EmailSenderService;
import com.acme.hkaprozesse.service.SendMessageService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.acme.hkaprozesse.service.SupervisorRoleService;

import java.util.Map;

@Component
public class AntragstellerWorker {

    private static final Logger logger = LogManager.getLogger(AntragstellerWorker.class);
    private final SupervisorRoleService supervisorRoleService;
    private final EmailSenderService emailSenderService;
    private final AccountingRoleService accountingRoleService;

    public AntragstellerWorker(SupervisorRoleService supervisorRoleService, EmailSenderService emailSenderService, AccountingRoleService accountingRoleService) {
        this.supervisorRoleService = supervisorRoleService;
        this.emailSenderService = emailSenderService;
        this.accountingRoleService = accountingRoleService;

    }

    @JobWorker(type = "dienstreiseantrag")
    public void handleSaveAndForward(final JobClient client, final ActivatedJob job) throws Exception {
        logger.info("Start: Formular weiterleiten an den Vorgesetzten");

        // 1. Formulardaten aus Prozessvariablen abrufen
        Map<String, Object> variables = job.getVariablesAsMap();

        // Variablen mit Prüfung auf Existenz und Inhalt
        String processid = getVariable(variables, "processid", client, job);
        String name = getVariable(variables, "name", client, job);
        String department = getVariable(variables, "department", client, job);
        String position = getVariable(variables, "position", client, job);
        String employeeNumber = getVariable(variables, "employeeNumber", client, job);
        String purposeOfTrip = getVariable(variables, "purposeOfTrip", client, job);
        Double estimatedCost = getDoubleVariable(variables, "estimatedCost", client, job);

        logger.info("Formulardaten:Processid={}, Name={}, Department={}, Position={}, EmployeeNumber={}, PurposeOfTrip={}, EstimatedCost={}",
                processid,name, department, position, employeeNumber, purposeOfTrip, estimatedCost);

        String supervisorEmail = supervisorRoleService.getSupervisor(processid,employeeNumber);

        // 2. Nachricht an den Vorgesetzten senden
        String correlationKey = employeeNumber; // Eindeutiger Schlüssel
        String messageName = "FormularWeiterleitung"; // Nachrichtentyp
        String formData = variables.toString(); // Formulardaten als JSON-String

        emailSenderService.sendEmailAndPublishMessage(supervisorEmail,"Dienstreiseantrag",formData,messageName,correlationKey);

        logger.info("Nachricht an den Vorgesetzten gesendet: MessageName={}, CorrelationKey={}", messageName, correlationKey);

        // 3. Job abschließen
        client.newCompleteCommand(job.getKey()).send().join();

        logger.info("Formular erfolgreich weitergeleitet.");
    }

    @JobWorker(type = "rechnungeintragen")
    public void handleRechnungEintragen(final JobClient client, final ActivatedJob job) throws Exception {
        logger.info("Start: Rechnung verarbeiten und weiterleiten an das Abrechnungswesen");

        // 1. Rechnungsdaten aus Prozessvariablen abrufen
        Map<String, Object> variables = job.getVariablesAsMap();

        Double gesamtRechnung = getDoubleVariable(variables, "invoice", client, job); //ursprünglich amount !
        String employeeNumber = getVariable(variables, "employeeNumber", client, job);

        logger.info("Rechnungsdaten: GesamtRechnung={}, EmployeeNumber={}", gesamtRechnung, employeeNumber);

//        String accountingemail=accountingRoleService.getAccountingEmail(processid,employeeNumber);

        // 2. Nachricht an das Abrechnungswesen senden
        // Eindeutiger Schlüssel
        String correlationKey = employeeNumber;
        String messageName = "RechnungWeiterleiten"; // Nachrichtentyp
        String formData = variables.toString();
        String emailMessage="Dienstreise in Höhe von "+ gesamtRechnung +" € wurde getätigt!";

        emailSenderService.sendEmailAndPublishMessage("kodo2101@gmail.com","Dienstreiseantrag von "+ employeeNumber,emailMessage,messageName,correlationKey);

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
