package de.hka.camunda_prozess.camunda;

import de.hka.camunda_prozess.mail.EmailService;
import de.hka.camunda_prozess.mongodb.service.ReadService;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class VorgesetzterErmittelnWorker {

    private final ZeebeClient zeebeClient;
    private final ReadService readService;
    private final EmailService emailService;

    public void registerWorker() {
        log.info("Registriere Worker für 'vorgesetzter_ermitteln'");

        JobWorker worker = zeebeClient.newWorker()
            .jobType("vorgesetzter_ermitteln")
            .handler((jobClient, job) -> {
                Map<String, Object> variables = job.getVariablesAsMap();
                String fakultaetId = (String) variables.get("fakultaet");
                String nameAntragsteller = (String) variables.get("name");
                String vornameAntragsteller = (String) variables.get("vorname");

                try {
                    // Vorgesetzten ermitteln
                    final var vorgesetzter = readService.findDekanByFakultätId(fakultaetId);
                    log.debug("VorgesetzterErmittelnWorker: vorgesetzte={}", vorgesetzter);

                    // Ergebnisvariablen
                    Map<String, Object> resultVariables = Map.of(
                        "vorgesetzterName", vorgesetzter.getName(),
                        "vorgesetzterVorname", vorgesetzter.getVorname(),
                        "vorgesetzterEmail", vorgesetzter.getEmail()
                    );

                    // Job abschließen
                    jobClient.newCompleteCommand(job.getKey())
                        .variables(resultVariables)
                        .send()
                        .join();

                    log.info("Job erfolgreich abgeschlossen: {}", resultVariables);

                    // E-Mail senden
                    String emailContent = String.format(
                        "<h1>Hallo %s %s</h1><p>Das ist eine Testnachricht.<br>Dein Dekan ist %s %s und ist erreichbar unter der email %s </p>",
                        nameAntragsteller,
                        vornameAntragsteller,
                        vorgesetzter.getVorname(),
                        vorgesetzter.getName(),
                        vorgesetzter.getEmail()
                    );

                    emailService.sendEmail(
                        "caleb_g@outlook.de",
                        "Information zu Ihrer Function",
                        emailContent
                    );

                } catch (Exception e) {
                    log.error("Fehler beim Ermitteln des Vorgesetzten: {}", e.getMessage(), e);
                    jobClient.newFailCommand(job.getKey())
                        .retries(job.getRetries() - 1)
                        .errorMessage(e.getMessage())
                        .send()
                        .join();
                }
            })
            .open();

        log.info("Worker für 'vorgesetzter_ermitteln' ist aktiv.");
    }
}
