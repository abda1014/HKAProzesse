package com.acme.hkaprozesse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Service-Klasse zum Abrufen der E-Mail-Adresse eines Vorgesetzten aus einer externen API
 * basierend auf einer gegebenen Prozess- und Benutzer-ID.
 */
@Service
public class SupervisorRoleService {

    private final RestTemplate restTemplate;

    /**
     * Konstruktor, der die Instanz von RestTemplate injiziert.
     *
     * @param restTemplate die Instanz von RestTemplate, die für HTTP-Anfragen verwendet wird
     */
    public SupervisorRoleService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Holt die E-Mail-Adresse eines Vorgesetzten für einen gegebenen Prozess- und Benutzer-ID.
     *
     * @param processId die ID des Prozesses
     * @param userId    die ID des Benutzers
     * @return die E-Mail-Adresse des Vorgesetzten
     * @throws Exception wenn ein Fehler beim Abrufen oder Verarbeiten der E-Mail-Adresse auftritt
     */
    public String getSupervisor(String processId, String userId) throws Exception {
        // URL des externen Backends mit Prozess-ID und User-ID
        String backendUrl = UriComponentsBuilder.fromHttpUrl("https://localhost:3000/rolemapper/process-roles")
                .queryParam("processId", processId)
                .queryParam("userId", userId)
                .toUriString();

        try {
            // REST-GET-Aufruf
            String response = restTemplate.getForObject(backendUrl, String.class);

            // JSON-Antwort verarbeiten, um die Vorgesetztenrolle zu extrahieren
            return extractSupervisorEmail(response);
        } catch (Exception e) {
            throw new Exception("Fehler beim Abrufen der Vorgesetztenrolle: " + e.getMessage(), e);
        }
    }

    /**
     * Verarbeitet die JSON-Antwort und extrahiert die E-Mail-Adresse des Vorgesetzten.
     *
     * @param jsonResponse die JSON-Antwort des externen Backends
     * @return die E-Mail-Adresse des Vorgesetzten
     * @throws Exception wenn ein Fehler beim Verarbeiten der Antwort auftritt
     */
    private String extractSupervisorEmail(String jsonResponse) throws Exception {
        // JSON mit Jackson verarbeiten
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Sicherstellen, dass "roles" existiert und ein Array ist
        JsonNode rolesNode = rootNode.path("roles");
        if (!rolesNode.isArray()) {
            throw new Exception("Die Antwort enthält keine gültige 'roles'-Struktur.");
        }

        // Suche nach der Rolle "Vorgesetzter"
        JsonNode supervisorNode = null;
        for (JsonNode roleNode : rolesNode) {
            if ("Vorgesetzte:r".equals(roleNode.path("roleName").asText())) {
                supervisorNode = roleNode;
                break;
            }
        }

        if (supervisorNode == null) {
            throw new Exception("Die Rolle 'Vorgesetzter' wurde nicht in der Antwort gefunden.");
        }

        // Sicherstellen, dass "users" existiert und ein Array ist
        JsonNode usersNode = supervisorNode.path("users");
        if (!usersNode.isArray() || usersNode.isEmpty()) {
            throw new Exception("Keine Benutzer für die Rolle 'Vorgesetzter' gefunden.");
        }

        // Extrahiere die E-Mail-Adresse aus der ersten Benutzer-ID
        JsonNode userNode = usersNode.get(0).path("user");
        String userId = userNode.path("userId").asText(null);

        if (userId == null || userId.isEmpty()) {
            throw new Exception("Die 'userId' des Vorgesetzten konnte nicht extrahiert werden.");
        }

        // E-Mail-Adresse zusammenstellen
        return userId + "@gmail.com"; // Beispiel-Domain hinzufügen
    }

}
