package com.acme.hkaprozesse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AccountingRoleService {

    private final RestTemplate restTemplate;

    public AccountingRoleService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAccountingEmail(String processId, String userId) throws Exception {
        // URL des externen Backends mit Prozess-ID und User-ID
        String backendUrl = UriComponentsBuilder.fromHttpUrl("https://localhost:3000/rolemapper/process-roles")
                .queryParam("processId", processId)
                .queryParam("userId", userId)
                .toUriString();

        try {
            // REST-GET-Aufruf
            String response = restTemplate.getForObject(backendUrl, String.class);

            // JSON-Antwort verarbeiten, um die E-Mail-Adresse zu extrahieren
            return extractAccountingEmail(response);
        } catch (Exception e) {
            throw new Exception("Fehler beim Abrufen der E-Mail-Adresse für die Rolle 'Rechnungsprüfung': " + e.getMessage(), e);
        }
    }

    private String extractAccountingEmail(String jsonResponse) throws Exception {
        // JSON mit Jackson verarbeiten
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Sicherstellen, dass "roles" existiert und ein Array ist
        JsonNode rolesNode = rootNode.path("roles");
        if (!rolesNode.isArray()) {
            throw new Exception("Die Antwort enthält keine gültige 'roles'-Struktur.");
        }

        // Suche nach der Rolle "Rechnungsprüfung"
        JsonNode accountingNode = null;
        for (JsonNode roleNode : rolesNode) {
            if ("Rechnungsprüfung".equals(roleNode.path("roleName").asText())) {
                accountingNode = roleNode;
                break;
            }
        }

        if (accountingNode == null) {
            throw new Exception("Die Rolle 'Rechnungsprüfung' wurde nicht in der Antwort gefunden.");
        }

        // Sicherstellen, dass "users" existiert und ein Array ist
        JsonNode usersNode = accountingNode.path("users");
        if (!usersNode.isArray() || usersNode.isEmpty()) {
            throw new Exception("Keine Benutzer für die Rolle 'Rechnungsprüfung' gefunden.");
        }

        // Extrahiere die E-Mail-Adresse aus der ersten Benutzer-ID
        JsonNode userNode = usersNode.get(0).path("user");
        String userId = userNode.path("userId").asText(null);

        if (userId == null || userId.isEmpty()) {
            throw new Exception("Die 'userId' für die Rolle 'Rechnungsprüfung' konnte nicht extrahiert werden.");
        }

        // E-Mail-Adresse zusammenstellen
        return userId + "@gmail.com"; // Beispiel-Domain hinzufügen
    }
}
