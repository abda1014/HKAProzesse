package com.acme.hkaprozesse;

import com.acme.hkaprozesse.service.EmailSenderService;
import io.camunda.zeebe.client.ZeebeClient;
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
public class EmailWorker {
    @Autowired
    private ZeebeClient zeebeClient;

    private static final Logger logger = LogManager.getLogger(EmailWorker.class);

    private final EmailSenderService emailSenderService;
    private final SupervisorRoleService supervisorRoleService;

    public EmailWorker(EmailSenderService emailSenderService, SupervisorRoleService supervisorRoleService) {
        this.emailSenderService = emailSenderService;
        this.supervisorRoleService = supervisorRoleService;
    }

    @JobWorker(type = "emailsenden")
    public void sendEmail(final JobClient client, final ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();

        try {
            // Prozessvariablen abrufen
            String processid = getVariable(variables, "processid", client, job);
            String employeeNumber = getVariable(variables, "employeeNumber", client, job);

            // Supervisor-E-Mail abrufen
            String supervisorEmail = supervisorRoleService.getSupervisor(processid, employeeNumber);

            // E-Mail senden
            String subject = "Dienstreiseantrag";
            String body = "Es gibt eine neue Task";
//            emailSenderService.sendEmail(supervisorEmail, subject, body);

            String messageName = "FormularWeiterleitung";

            zeebeClient.newPublishMessageCommand()
                    .messageName(messageName)
                    .correlationKey(employeeNumber)
                    .variables(variables)
                    .send()
                    .join();

            // Job erfolgreich abschließen
            client.newCompleteCommand(job.getKey()).send().join();
            logger.info("E-Mail erfolgreich versendet an {}", supervisorEmail);

        } catch (Exception e) {
            // Job fehlschlagen und Fehler dokumentieren
            logger.error("Fehler beim E-Mail-Versand: {}", e.getMessage(), e);
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("E-Mail-Versand fehlgeschlagen: " + e.getMessage())
                    .send().join();
        }
    }

    // Hilfsmethode zur Prüfung und Abruf von Variablen
    private String getVariable(Map<String, Object> variables, String key, JobClient client, ActivatedJob job) {
        if (variables.containsKey(key) && variables.get(key) != null) {
            return variables.get(key).toString();
        } else {
            logger.error("Fehler: '{}' wurde nicht in den Prozessvariablen gefunden!", key);
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Fehlende Prozessvariable: " + key)
                    .send()
                    .join();
            throw new IllegalStateException("Prozessvariable fehlt: " + key);
        }
    }
}
