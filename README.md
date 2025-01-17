# HKAProzesse

Das Projekt **HKAProzesse** implementiert einen Dienstreiseantragsprozess mithilfe von Camunda. Der Workflow ermöglicht die Verwaltung und Verfolgung von Dienstreiseanträgen, Rechnungsprüfungen und Finanzabwicklungen. Die Cluster-Informationen sind lokal hinterlegt, und die gesamte Prozessausführung kann über das Camunda Operate-Tool überwacht werden.

## Voraussetzungen

### Software
- **Camunda Modeler**:  
  Um den Workflow zu erstellen, zu bearbeiten und zu deployen, ist ein Login im Camunda Modeler erforderlich.
  
- **Camunda Operate**:  
  Für die Überwachung und Nachverfolgung des Prozesses.
  
- **Java Development Kit (JDK)**: Version 21  
  Für die Ausführung des Codes.
  
- **Maven**:  
  Zum Bauen und Ausführen des Projekts.
  
- **RoleMapper**:  
  Das Projekt ist eine notwendige Abhängigkeit für die Benutzerrollen und Berechtigungen.  
  [RoleMapper GitHub](https://github.com/FlowCraft-AG/RoleMapper)  
  Stelle sicher, dass das Zertifikat (`certificat.crt`), das im Verzeichnis `.volumes/keys/` im RoleMapper-Projekt liegt, in den Truststore des Projekts aufgenommen wird.  
  Die Bedingung für die Rollenermittlung ist, dass das Zertifikat vom RoleMapper akzeptiert wird.



## Schritte zur Ausführung

1. **Projektverzeichnis aufrufen**  
   Navigiere in das Verzeichnis, in dem sich das Projekt befindet:

   ```bash
   cd hkaprozesse

    //Starte die benötigten Docker-Container:
    docker-compose up
    //Baue das Projekt mit Maven und starte es:
    mvn spring-boot:run

    // Wechsle in das Verzeichnis des RoleMapper-Projekts und starte es:
    cd RoleMapper
    npm run dev

