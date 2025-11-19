# Stage 1: Build the JAR
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
# A. Copy the parent pom.xml
COPY pom.xml .
# B. Copy ALL module folders listed in the parent pom
COPY User/ User/
COPY Auth/ Auth/
COPY Gateway/ Gateway/
# C. Explicitly build ONLY the User module from the root
RUN mvn clean package -DskipTests -pl User -am \
    -Dopenapipath=/app/User/src/main/resources/userOpenApi.yaml

# Stage 2: Run the JAR
FROM eclipse-temurin:21-jdk
# Set working directory
WORKDIR /app
# Copy the built JAR file into the container
COPY --from=builder /app/User/target/*.jar userService.jar
# Copy SSL files into the runtime container
#COPY ssl/ ssl/

# Expose the internal port your app runs on
EXPOSE 8081

LABEL authors="piotr"

# Run the application
ENTRYPOINT ["java", "-jar", "userService.jar"]
