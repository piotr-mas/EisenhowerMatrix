# Stage 1: Build the JAR
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY ms-models/ ms-models/
COPY User/ User/
COPY Auth/ Auth/
COPY Gateway/ Gateway/

RUN mvn clean package -DskipTests -pl Gateway -am

# Stage 2: Run the JAR
FROM eclipse-temurin:25-jdk
# Set working directory
WORKDIR /app
# Copy the built JAR file into the container
COPY --from=builder /app/Gateway/target/*.jar gatewayService.jar

# Expose the internal port your app runs on
EXPOSE 8080

LABEL authors="piotr"

# Run the application
ENTRYPOINT ["java", "-jar", "gatewayService.jar"]
