package com.acme.hkaprozesse;

import io.camunda.zeebe.client.ZeebeClient;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ZeebeConnectionTest {

    @Test
    void testZeebeClientConnection() {
        try (ZeebeClient client = ZeebeClient.newClientBuilder()
                .gatewayAddress("localhost:26500")
                .usePlaintext()
                .build()) {

            // Verbindung testen
            var topology = client.newTopologyRequest().send().join();
            assertNotNull(topology, "Topology should not be null");
            System.out.println("Connected to Zeebe with topology: " + topology);

            // Prozessinstanz starten
            var result = client.newCreateInstanceCommand()
                    .bpmnProcessId("Process_0ofgjc0")
                    .latestVersion()
                    .variables(Map.of("key", "value"))
                    .send()
                    .join();
            assertNotNull(result, "Process instance should not be null");
            System.out.println("Process started: " + result.getProcessInstanceKey());

        } catch (Exception e) {
            System.err.println("Connection to Zeebe Broker failed:");
            e.printStackTrace();
        }
    }
}
