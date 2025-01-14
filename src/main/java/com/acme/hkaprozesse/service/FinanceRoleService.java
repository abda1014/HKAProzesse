package com.acme.hkaprozesse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Service-Klasse zum Abrufen der E-Mail-Adresse eines Benutzers in der Finanzabteilung
 * mit der Funktion "Leitung (Finanzen)" aus einer externen API.
 */
@Service
public class FinanceRoleService {

    private final RestTemplate restTemplate;

    /**
     * Konstruktor, der die Instanz von RestTemplate injiziert.
     *
     * @param restTemplate die Instanz von RestTemplate, die für HTTP-Anfragen verwendet wird
     */
    public FinanceRoleService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Holt die E-Mail-Adresse eines Benutzers in der Rolle "Finanz Abteilung" mit der Funktion
     * "Leitung (Finanzen)" für einen gegebenen Prozess- und Benutzer-ID.
     *
     * @param processId die ID des Prozesses
     * @param userId    die ID des Benutzers
     * @return die E-Mail-Adresse des Benutzers in der Finanzabteilung
     * @throws Exception wenn ein Fehler beim Abrufen oder Verarbeiten der E-Mail-Adresse auftritt
     */
    public String getFinanceEmail(String processId, String userId) throws Exception {
        // URL des externen Backends mit Prozess-ID und User-ID
        String backendUrl = UriComponentsBuilder.fromHttpUrl("https://localhost:3000/rolemapper/process-roles")
                .queryParam("processId", processId)
                .queryParam("userId", userId)
                .toUriString();

        try {
            // REST-GET-Aufruf
            String response = restTemplate.getForObject(backendUrl, String.class);

            // JSON-Antwort verarbeiten, um die E-Mail der Finanzabteilung zu extrahieren
            return extractFinanceEmail(response);
        } catch (Exception e) {
            throw new Exception("Fehler beim Abrufen der E-Mail der Finanzabteilung: " + e.getMessage(), e);
        }
    }

    /**
     * Verarbeitet die JSON-Antwort und extrahiert die E-Mail-Adresse eines Benutzers in der Rolle
     * "Finanz Abteilung" mit der Funktion "Leitung (Finanzen)".
     *
     * @param jsonResponse die JSON-Antwort des externen Backends
     * @return die E-Mail-Adresse des Benutzers in der Finanzabteilung
     * @throws Exception wenn ein Fehler beim Verarbeiten der Antwort auftritt
     */
    private String extractFinanceEmail(String jsonResponse) throws Exception {
        // JSON mit Jackson verarbeiten
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Sicherstellen, dass "roles" existiert und ein Array ist
        JsonNode rolesNode = rootNode.path("roles");
        if (!rolesNode.isArray()) {
            throw new Exception("Die Antwort enthält keine gültige 'roles'-Struktur.");
        }

        // Suche nach der Rolle "Finanz Abteilung"
        JsonNode financeRoleNode = null;
        for (JsonNode roleNode : rolesNode) {
            if ("Finanabteilung".equals(roleNode.path("roleName").asText())) {
                financeRoleNode = roleNode;
                break;
            }
        }

        if (financeRoleNode == null) {
            throw new Exception("Die Rolle 'Finanz Abteilung' wurde nicht in der Antwort gefunden.");
        }

        // Sicherstellen, dass "users" existiert und ein Array ist
        JsonNode usersNode = financeRoleNode.path("users");
        if (!usersNode.isArray() || usersNode.isEmpty()) {
            throw new Exception("Keine Benutzer für die Rolle 'Finanz Abteilung' gefunden.");
        }

        // Suche nach einem Benutzer mit der Funktion "Leitung (Finanzen)"
        for (JsonNode userNode : usersNode) {
            if ("Leitung (Finanzen)".equals(userNode.path("functionName").asText())) {
                String userId = userNode.path("user").path("userId").asText(null);
                if (userId == null || userId.isEmpty()) {
                    throw new Exception("Die 'userId' des Benutzers konnte nicht extrahiert werden.");
                }
                // E-Mail-Adresse zusammenstellen
                return userId + "@gmail.com"; // Beispiel-Domain hinzufügen
            }
        }

        throw new Exception("Kein Benutzer mit der Funktion 'Leitung (Finanzen)' gefunden.");
    }
}
