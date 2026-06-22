FROM eclipse-temurin:25-jdk-alpine
LABEL authors="julio"
ARG JAR_FILE=target/Reservaciones-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app_reservaciones.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app_reservaciones.jar"]