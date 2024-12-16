package de.hka.camunda_prozess.camunda.config;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeConfig {

    @Bean
    public ZeebeClient zeebeClient() {
        return ZeebeClient.newClientBuilder()
            .usePlaintext()
            .build();
    }
}
