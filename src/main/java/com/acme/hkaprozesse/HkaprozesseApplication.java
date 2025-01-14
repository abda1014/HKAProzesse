package com.acme.hkaprozesse;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

/**
 * Hauptanwendung für die Verwaltung der Geschäftsprozesse (HKA-Prozesse).
 * Diese Klasse startet die Anwendung, stellt eine Verbindung zum Zeebe-Client her
 * und startet eine Prozessinstanz basierend auf der BPMN-Datei.
 */
@SpringBootApplication
@Deployment(resources = {"classpath:process/HKAProzesse.bpmn",
        "classpath:forms/Dienstreiseantrag.form",
        "classpath:forms/Rechnungsprüfung.form",
        "classpath:forms/Prüfung.form",
        "classpath:forms/Rechnung.form",
        "classpath:dmn/Abrechnungvalidieren.dmn",
        "classpath:dmn/PruefungdesAntrags.dmn",
})
public class HkaprozesseApplication implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(HkaprozesseApplication.class);

    @Autowired
    private ZeebeClient zeebeClient;

    /**
     * Die main-Methode startet die Spring Boot-Anwendung.
     *
     * @param args die Argumente, die beim Starten der Anwendung übergeben werden
     */
    public static void main(String[] args) {
        SpringApplication.run(HkaprozesseApplication.class, args);
    }

    /**
     * Wird beim Starten der Anwendung ausgeführt. Fragt die Broker-Topologie ab und startet
     * eine Prozessinstanz. Dabei wird auch eine Beispiel-Variable an den Prozess übergeben.
     *
     * @param args die Argumente, die beim Starten der Anwendung übergeben werden
     */
    @Override
    public void run(String... args) {
        // Topologie abfragen
        LOG.info("Fetching broker topology...");
        var topology = zeebeClient.newTopologyRequest().send().join();
        topology.getBrokers().forEach(broker -> {
            LOG.info("Broker: {} (Version: {})", broker.getAddress(), broker.getVersion());
            broker.getPartitions().forEach(partition -> {
                LOG.info("Partition: {}, Role: {}", partition.getPartitionId(), partition.getRole());
            });
        });

        // Starte eine Prozessinstanz
        var bpmnProcessId = "Process_0ofgjc0"; // Ersetze mit deinem BPMN-Prozess-ID
        try {
            var event = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId(bpmnProcessId)
                    .latestVersion()
                    .variables(Map.of("total", 100)) // Variablen an den Prozess übergeben
                    .send()
                    .join();

            LOG.info("Started a process instance: {}", event.getProcessInstanceKey());
        } catch (Exception e) {
            LOG.error("Failed to start process instance. Make sure the process is deployed correctly.", e);
        }
    }
}
