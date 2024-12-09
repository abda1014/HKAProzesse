# HKAProzesse

Das Projekt HKAProzesse implementiert einen Dienstreiseantragsprozess mithilfe von Camunda Cloud. Der Workflow ermöglicht die Verwaltung und Verfolgung von Dienstreiseanträgen, Rechnungsprüfungen und Finanzabwicklungen. Die Cluster-Informationen sind im Main-Code hinterlegt, und die gesamte Prozessausführung kann über das Camunda Operate-Tool überwacht werden.

Voraussetzungen
Software
Camunda Modeler:
Um den Workflow zu erstellen, zu bearbeiten und zu deployen, muss ein Login in den Camunda Modeler erforderlich sein.
Camunda Operate:
Für die Überwachung des Prozesses.
Java Development Kit (JDK): Version 21 
Maven: Zum Bauen und Ausführen des Projekts.
Zugangsdaten
Camunda Cloud Cluster Informationen:
Die Cluster-Details (Cluster-ID, Client-ID, und Client-Secret) müssen im main()-Methodenblock des Codes und in der application.yml korrekt eingetragen sein.


Schritte zur Ausführung
Projektverzeichnis aufrufen: Navigieren Sie in das Verzeichnis, in dem sich das Projekt befindet. In diesem Fall:

bash
Code kopieren
cd hkaprozesse
Spring Boot Anwendung starten: Starten Sie das Projekt mit Maven:

bash
Code kopieren
mvn spring-boot:run
Prozess-ID im Log überprüfen:

Die Prozessinstanznummer wird im Log ausgegeben.
Diese kann verwendet werden, um die spezifische Prozessausführung im Camunda Operate nachzuverfolgen.
