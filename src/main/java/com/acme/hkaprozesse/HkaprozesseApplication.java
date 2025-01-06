package com.acme.hkaprozesse;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@SpringBootApplication
public class HkaprozesseApplication {

    private static final Logger LOG = LoggerFactory.getLogger(HkaprozesseApplication.class);

    // Verbindungseinstellungen für Zeebe Cloud
    private static final String ZEEBE_ADDRESS = "83d96c80-028e-41e3-9005-8029fc1fc274.bru-2.zeebe.camunda.io:443";
    private static final String CLIENT_ID = "80iA1_8DDNEd0LVwLf5hwOu7GPmFcM8Z";
    private static final String CLIENT_SECRET = "UFtlmvCfk21A_OO6C0VlZOyuMVElWvH-hVFgxnnJ4z95cXgfBIhQTMAUbEjy0kFk";

    public static void main(String[] args) {
        // Start Spring Boot application
        SpringApplication.run(HkaprozesseApplication.class, args);

        // ZeebeClient mit Cloud Builder erstellen
        try (ZeebeClient client = ZeebeClient.newCloudClientBuilder()
                .withClusterId(ZEEBE_ADDRESS.split("\\.")[0]) // Extrahiere die Cluster-ID aus der Adresse
                .withClientId(CLIENT_ID)
                .withClientSecret(CLIENT_SECRET)
                .defaultRequestTimeout(Duration.ofMinutes(10))
                .build()) {

            // Topologie abfragen mit Timeout
            LOG.info("Fetching broker topology...");
            try {
                var topologyFuture = client.newTopologyRequest().send();
                Topology topology = topologyFuture.toCompletableFuture().get(180, TimeUnit.SECONDS);

                topology.getBrokers().forEach(broker -> {
                    LOG.info("Broker: {} (Version: {})", broker.getAddress(), broker.getVersion());
                    broker.getPartitions().forEach(partition -> {
                        LOG.info("Partition: {}, Role: {}", partition.getPartitionId(), partition.getRole());
                    });
                });
            } catch (Exception e) {
                LOG.error("Failed to fetch topology within the timeout period.", e);
            }

            // Prozessinstanz starten mit Timeout
            var bpmnProcessId = "Process_0ofgjc0"; // Deine BPMN-Prozess-ID
            try {
                var eventFuture = client.newCreateInstanceCommand()
                        .bpmnProcessId(bpmnProcessId)
                        .latestVersion()
                        .variables(Map.of("total", 100)) // Variablen an den Prozess übergeben
                        .send();

                var event = eventFuture.toCompletableFuture().get(90, TimeUnit.SECONDS);
                LOG.info("Started a process instance: {}", event.getProcessInstanceKey());
            } catch (Exception e) {
                LOG.error("Failed to start process instance within the timeout period.", e);
            }

        } catch (Exception e) {
            LOG.error("Failed to connect to Zeebe Cluster", e);
        }
    }
}
