# Etapa 1: compilar con Maven
FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: correr el .jar generado
FROM eclipse-temurin:25-jre-alpine
LABEL authors="julio"
WORKDIR /app
COPY --from=build /app/target/Reservaciones-0.0.1-SNAPSHOT.jar app_reservaciones.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app_reservaciones.jar"]