package com.acme.hkaprozesse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import com.acme.hkaprozesse.service.FinanceRoleService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

class FinanceRoleServiceTest {

    @Test
    void testGetFinanceEmail() throws Exception {
        // Mock das RestTemplate
        RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);

        // Beispielantwort simulieren
        String mockResponse = """
                {
                    "roles": [
                        {
                            "roleName": "Finanz Abteilung",
                            "roleId": "FA0001",
                            "users": [
                                {
                                    "_id": "6741dcb41415751666618c0d",
                                    "functionName": "Leitung (Finanzen)",
                                    "user": {
                                        "_id": "6770f9bf0df837decdf8ee9a",
                                        "userId": "scdo0001",
                                        "userType": "employee",
                                        "userRole": "adminTechnicalStaff",
                                        "orgUnit": "A0021",
                                        "active": true,
                                        "employee": {
                                            "costCenter": "A0021",
                                            "department": "Hochschulverwaltung"
                                        }
                                    }
                                }
                            ]
                        }
                    ]
                }
                """;

        // Mock das Verhalten des RestTemplate
        Mockito.when(restTemplateMock.getForObject(anyString(), Mockito.eq(String.class))).thenReturn(mockResponse);

        // Instanziere den Service mit dem gemockten RestTemplate
        FinanceRoleService financeRoleService = new FinanceRoleService(restTemplateMock);

        // Testparameter
        String processId = "RA0001";
        String userId = "grme0001";

        // Methode aufrufen und Ergebnis überprüfen
        String expectedEmail = "scdo0001@gmail.com";
        String actualEmail = financeRoleService.getFinanceEmail(processId, userId);

        assertEquals(expectedEmail, actualEmail, "Die extrahierte E-Mail-Adresse stimmt nicht mit der erwarteten überein.");
    }
}
