# Stage 1: Build User and install parent POM
FROM maven:3.9.4-eclipse-temurin-21 AS user_build
WORKDIR /app
COPY pom.xml .
COPY User/ User/
COPY Auth/ Auth/
COPY Gateway/ Gateway/
# Install parent POM only
RUN mvn install -N -DskipTests
# Build User
RUN mvn clean install -DskipTests -pl User

# Stage 2: Build Auth
FROM maven:3.9.4-eclipse-temurin-21 AS auth_build
WORKDIR /app
COPY pom.xml .
COPY Auth/ Auth/
COPY Gateway/ Gateway/
COPY --from=user_build /root/.m2 /root/.m2
RUN mvn clean package -DskipTests -pl Auth

# Stage 3: Runtime
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=auth_build /app/Auth/target/*.jar authService.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "authService.jar"]
