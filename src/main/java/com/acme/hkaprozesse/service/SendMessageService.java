package com.acme.hkaprozesse.service;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.stereotype.Service;

@Service
public class SendMessageService {

    private final ZeebeClient zeebeClient;

    public SendMessageService(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }

    public void sendMessage(String messageName, String correlationKey, String formData) {
        zeebeClient.newPublishMessageCommand()
                .messageName(messageName) // Der Name der Nachricht
                .correlationKey(correlationKey) // Der Key, um die Nachricht zu korrelieren
                .variables("{\"formData\": \"" + formData + "\"}") // Die übergebenen Daten als JSON
                .send()
                .join();
    }
}

