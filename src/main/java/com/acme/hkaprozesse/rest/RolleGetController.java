package com.acme.hkaprozesse.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class RolleGetController {

    private static final Logger logger = LoggerFactory.getLogger(RolleGetController.class);

    @GetMapping("/process-roles")
    public String getProcessRoles(
            @RequestParam String processId,
            @RequestParam String userId
    ) {
        logger.info("Received request for processId: {} and userId: {}", processId, userId);

        // URL des TypeScript-Backends
        String url = "https://localhost:3000/rolemapper/process-roles?processId=" + processId + "&userId=" + userId;

        // RestTemplate für den GET-Aufruf
        RestTemplate restTemplate = new RestTemplate();
        try {
            // JSON-Daten von der URL abrufen
            String response = restTemplate.getForObject(url, String.class);

            // Logs ausgeben
            logger.info("Response from TypeScript backend: {}", response);

            return response; // Rückgabe der JSON-Daten
        } catch (Exception e) {
            logger.error("Error fetching roles from TypeScript backend: {}", e.getMessage());
            return "Error fetching roles from backend.";
        }
    }
}






//Auf Basis der User id im Form soll quasi ein Abfrage an den Backend des RoleMapper Server geschickt werden und ich erhalte die Vorgesetzten und die !
   // 1. Brauchen eine Methode die aus dem Form die ID entnimmt
    // 2. Brauchen eine Methode die die ID so verarbeitet,sodass wir aus dem typescript backend die Rollen erhalten

    // Aggregate root
    // tag::get-aggregate-root[]









