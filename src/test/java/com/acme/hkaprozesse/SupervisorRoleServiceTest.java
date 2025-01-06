package com.acme.hkaprozesse;


import com.acme.hkaprozesse.service.SupervisorRoleService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertEquals;


class SupervisorRoleServiceTest {

    @Test
    void testGetSupervisorEmail_Success() throws Exception {
        // Mock für RestTemplate erstellen
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

        // Beispielantwort simulieren
        String mockResponse = """
        {
          "roles": [
            {
              "roleName": "Antragsteller",
              "users": [
                {
                  "user": {
                    "userId": "muud0001"
                  }
                }
              ]
            },
            {
              "roleName": "Vorgesetzter",
              "users": [
                {
                  "user": {
                    "userId": "nefu0002"
                  }
                }
              ]
            }
          ]
        }
        """;

        // Mock-Response einrichten
        Mockito.when(restTemplate.getForObject(anyString(), Mockito.eq(String.class))).thenReturn(mockResponse);

        // SupervisorRoleService mit dem gemockten RestTemplate erstellen
        SupervisorRoleService service = new SupervisorRoleService(restTemplate);

        // Parameter
        String processId = "DA0001";
        String userId = "muud0001";

        // Methode aufrufen und Ergebnis überprüfen
        String email = service.getSupervisor(processId, userId);
        assertEquals("nefu0002@gmail.com", email);

        // Verifizieren, dass RestTemplate korrekt aufgerufen wurde
        Mockito.verify(restTemplate).getForObject(
                "https://localhost:3000/rolemapper/process-roles?processId=DA0001&userId=muud0001",
                String.class
        );
    }

    @Test
    void testGetSupervisorEmail_Exception() {
        // Mock für RestTemplate erstellen
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

        // Simuliere eine Exception beim REST-Aufruf
        Mockito.when(restTemplate.getForObject(anyString(), Mockito.eq(String.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        // SupervisorRoleService mit dem gemockten RestTemplate erstellen
        SupervisorRoleService service = new SupervisorRoleService(restTemplate);

        // Parameter
        String processId = "DA0001";
        String userId = "muud0001";

        // Überprüfen, dass eine Exception geworfen wird
        Exception exception = assertThrows(Exception.class, () -> {
            service.getSupervisor(processId, userId);
        });

        assertTrue(exception.getMessage().contains("Fehler beim Abrufen der Vorgesetztenrolle"));
    }
}







