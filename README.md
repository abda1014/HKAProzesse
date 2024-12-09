# HKAProzesse

Das Projekt **HKAProzesse** implementiert einen Dienstreiseantragsprozess mithilfe von Camunda Cloud. Der Workflow ermöglicht die Verwaltung und Verfolgung von Dienstreiseanträgen, Rechnungsprüfungen und Finanzabwicklungen. Die Cluster-Informationen sind im Main-Code hinterlegt, und die gesamte Prozessausführung kann über das Camunda Operate-Tool überwacht werden.

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

### Zugangsdaten
- **Camunda Cloud Cluster Informationen**:  
  Die Cluster-Details (`Cluster-ID`, `Client-ID`, und `Client-Secret`) müssen in der `main()`-Methode und in der `application.yml` hinterlegt sein.

## Schritte zur Ausführung

1. **Projektverzeichnis aufrufen**  
   Navigieren Sie in das Verzeichnis, in dem sich das Projekt befindet. In diesem Fall:

   ```bash
   cd hkaprozesse
   mvn spring-boot:run
