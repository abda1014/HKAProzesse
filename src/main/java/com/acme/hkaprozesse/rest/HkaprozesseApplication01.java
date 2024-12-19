package com.acme.hkaprozesse.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HkaprozesseApplication01 {

    private static final Logger LOG = LoggerFactory.getLogger(HkaprozesseApplication01.class);

    public static void main(String[] args) {
        // Log a message to indicate the server is starting
        LOG.info("Starting Hkaprozesse Spring Boot application...");

        // Start Spring Boot application
        SpringApplication.run(HkaprozesseApplication01.class, args);

        // Log a message to confirm the application has started
        LOG.info("Hkaprozesse Spring Boot application started successfully.");
    }
}
