package com.acme.hkaprozesse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import com.acme.hkaprozesse.service.AccountingRoleService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

class AccountingRoleServiceTest {

    @Test
    void testGetAccountingEmail() throws Exception {
        // Mock das RestTemplate
        RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);

        // Beispielantwort simulieren
        String mockResponse = """
                {
                    "roles": [
                        {
                            "roleName": "Rechnungsprüfung",
                            "roleId": "RP0001",
                            "users": [
                                {
                                    "_id": "6741dcb41415751666618c0d",
                                    "functionName": "Leitung (Finanzen)",
                                    "user": {
                                        "_id": "673ede38e1746bf8e6aa1b35",
                                        "userId": "kodo0001",
                                        "userType": "employee",
                                        "userRole": "adminTechnicalStaff",
                                        "orgUnit": "A0021",
                                        "active": true,
                                        "validFrom": "2010-03-01T00:00:00.000Z",
                                        "validUntil": "2100-12-31T23:59:59.000Z",
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
        AccountingRoleService accountingRoleService = new AccountingRoleService(restTemplateMock);

        // Testparameter
        String processId = "RA0001";
        String userId = "grme0001";

        // Methode aufrufen und Ergebnis überprüfen
        String expectedEmail = "kodo0001@gmail.com";
        String actualEmail = accountingRoleService.getAccountingEmail(processId, userId);

        assertEquals(expectedEmail, actualEmail, "Die extrahierte E-Mail-Adresse stimmt nicht mit der erwarteten überein.");
    }
}
