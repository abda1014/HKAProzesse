# Wähle ein OpenJDK-Image als Basis
FROM openjdk:21-jdk-slim AS build

# Setze das Arbeitsverzeichnis im Container
WORKDIR /app

# Kopiere die Maven-Dateien, um die Abhängigkeiten zu installieren
COPY pom.xml .

# Installiere Maven, um die Abhängigkeiten zu bauen
RUN apt-get update && apt-get install -y maven && \
    mvn clean install -DskipTests

# Kopiere den gesamten Code ins Arbeitsverzeichnis
COPY ./src ./src
COPY src/main/resources ./resources

# Baue die Anwendung
RUN mvn clean package -DskipTests

# ------------------------
# Produktions-Image für die Anwendung
# ------------------------
FROM openjdk:21-jdk-slim

# Setze das Arbeitsverzeichnis im Container
WORKDIR /app

# Kopiere das gebaute JAR-File aus dem vorherigen Schritt
COPY --from=build /app/target/hkaprozesse-0.0.1-SNAPSHOT.jar /app/hkaprozesse.jar


# Setze die Umgebungsvariable für die Anwendung
ENV SPRING_PROFILES_ACTIVE=production

# Exponiere den Port, den die Anwendung nutzt
EXPOSE 8079

# Starte die Anwendung
CMD ["java", "-jar", "/app/hkaprozesse.jar"]
