# Stage 1: Build the JAR
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY ms-models/ ms-models/
COPY User/ User/
COPY Auth/ Auth/
COPY Gateway/ Gateway/

RUN mvn clean package -DskipTests -pl Auth -am \
    -DopenApiPath=/app/Auth/src/main/resources/authOpenApi.yaml

# Stage 2: Run the JAR
FROM eclipse-temurin:21-jdk
# Set working directory
WORKDIR /app
# Copy the built JAR file into the container
COPY --from=builder /app/Auth/target/*.jar authService.jar

# Expose the internal port your app runs on
EXPOSE 8088

LABEL authors="piotr"

# Run the application
ENTRYPOINT ["java", "-jar", "authService.jar"]
