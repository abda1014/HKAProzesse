package de.hka.camunda_prozess.camunda;

//Service Worker für den Testprozess

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceTaskWorker {

    private final ZeebeClient zeebeClient;

    public void registerWorker() {
        log.info("Registriere Worker für 'eingabe' und 'note'");

        JobWorker outputWorker = zeebeClient.newWorker()
            .jobType("output")
            .handler((jobClient, job) -> {
                final var eingabe = job.getVariablesAsMap().get("eingabe");
                if (eingabe != null) {
                    String benutzerEingabe = eingabe.toString();
                    log.info("Benutzer Eingabe: " + benutzerEingabe);
                } else {
                    log.info("Variable 'eingabe' nicht gefunden.");
                }

                jobClient.newCompleteCommand(job.getKey()).send().join();
            })
            .open();

        JobWorker noteWorker = zeebeClient.newWorker()
            .jobType("note")
            .handler((jobClient, job) -> {
                final var eindruck = job.getVariablesAsMap().get("gegebene_note");
                if (eindruck != null) {
                    String benutzerEindruck = eindruck.toString();
                    log.info("Benutzer Eindruck: " + benutzerEindruck);
                } else {
                    log.info("Variable 'note' nicht gefunden.");
                }

                jobClient.newCompleteCommand(job.getKey()).send().join();
            })
            .open();
    }
}
