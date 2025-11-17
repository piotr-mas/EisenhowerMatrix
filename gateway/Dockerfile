# Stage 1: Build the JAR
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY User/ User/
COPY Auth/ Auth/
COPY Gateway/ Gateway/

RUN mvn clean package -DskipTests -pl Gateway -am

# Stage 2: Run the JAR
FROM eclipse-temurin:21-jdk
# Set working directory
WORKDIR /app
# Copy the built JAR file into the container
COPY --from=builder /app/Gateway/target/*.jar gatewayService.jar

# Expose the internal port your app runs on
EXPOSE 8080

LABEL authors="piotr"

# Run the application
ENTRYPOINT ["java", "-jar", "gatewayService.jar"]
