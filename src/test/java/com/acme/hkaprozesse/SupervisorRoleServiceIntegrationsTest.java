package com.acme.hkaprozesse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SupervisorRoleServiceIntegrationsTest {
    @Test
    void testGetSupervisorFromBackend() throws Exception {
        // Backend URL (muss mit dem laufenden Backend übereinstimmen)
        String backendUrl = "https://localhost:3000/rolemapper/process-roles?processId=DA0001&userId=grme0001";

        // RestTemplate instanziieren (keine Mocking, echte HTTP-Anfrage)
        RestTemplate restTemplate = new RestTemplate();

        // Erwartetes JSON-Ergebnis (wie in deinem Beispiel)
        String expectedResponse = """
                {
                     "roles": [
                         {
                             "roleName": "Antragssteller",
                             "users": [
                                 {
                                     "_id": "6740a653f3e876cdd20d8658",
                                     "functionName": "Professor",
                                     "user": {
                                         "_id": "6770f9bf0df837decdf8ee81",
                                         "userId": "grme0001",
                                         "userType": "employee",
                                         "userRole": "professor",
                                         "orgUnit": "A0004",
                                         "active": true,
                                         "employee": {
                                             "costCenter": "A0004",
                                             "department": "Fakultät für Informatik und Wirtschaftsinformatik"
                                         },
                                         "profile": {
                                             "firstName": "Melissa",
                                             "lastName": "Gruber"
                                         }
                                     }
                                 }
                             ]
                         },
                         {
                             "roleName": "Vorgesetzter",
                             "users": [
                                 {
                                     "_id": "6740a653f3e876cdd20d8658",
                                     "functionName": "Dekan",
                                     "user": {
                                         "_id": "673ede38e1746bf8e6aa1ade",
                                         "userId": "nefr0002",
                                         "userType": "employee",
                                         "userRole": "professor",
                                         "orgUnit": "A0004",
                                         "active": true,
                                         "validFrom": "1995-09-01T00:00:00.000Z",
                                         "validUntil": "2100-12-31T00:00:00.000Z",
                                         "employee": {
                                             "costCenter": "A0004",
                                             "department": "Fakultät für Informatik und Wirtschaftsinformatik"
                                         },
                                         "profile": {
                                             "firstName": "Franz",
                                             "lastName": "Nees"
                                         }
                                     }
                                 }
                             ]
                         }
                     ],
                     "_links": {
                         "self": {
                             "href": "https://localhost:3000/rolemapper/process-roles?processId=DA0001&userId=grme0001"
                         },
                         "Antragssteller": {
                             "grme0001": {
                                 "href": "https://localhost:3000/rolemapper/USERS/data?field=userId&operator=EQ&value=grme0001"
                             }
                         },
                         "Vorgesetzter": {
                             "nefr0002": {
                                 "href": "https://localhost:3000/rolemapper/USERS/data?field=userId&operator=EQ&value=nefr0002"
                             }
                         }
                     }
                 }
""";



        // HTTP-Anfrage senden
        String actualResponse = restTemplate.getForObject(backendUrl, String.class);

        // JSON-Objekte vergleichen (parsen und vergleichen)
        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals(
                objectMapper.readTree(expectedResponse),
                objectMapper.readTree(actualResponse),
                "Die Antwort des Backends entspricht nicht dem erwarteten Ergebnis"
        );
    }
}
