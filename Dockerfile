# Stage 1: Build the Maven multi-module project
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy parent pom
COPY pom.xml .

# Copy module poms
COPY eureka_server/pom.xml eureka_server/
# COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY user-service/pom.xml user-service/
COPY tts-service/pom.xml tts-service/
COPY test-service/pom.xml test-service/
COPY speech-recognition-service/pom.xml speech-recognition-service/
COPY image-service/pom.xml image-service/
COPY classroom-service/pom.xml classroom-service/
COPY lesson-service/pom.xml lesson-service/
COPY textbook-service/pom.xml textbook-service/
COPY translate-service/pom.xml translate-service/

# Download dependencies offline (cache layer)
RUN mvn dependency:go-offline -B

# Copy all source files
COPY eureka_server/src eureka_server/src
# COPY api-gateway/src api-gateway/src
COPY auth-service/src auth-service/src
COPY user-service/src user-service/src
COPY tts-service/src tts-service/src
COPY test-service/src test-service/src
COPY speech-recognition-service/src speech-recognition-service/src
COPY image-service/src image-service/src
COPY classroom-service/src classroom-service/src
COPY lesson-service/src lesson-service/src
COPY textbook-service/src textbook-service/src
COPY translate-service/src translate-service/src

# Package all modules, skipping tests
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# The specific module to run (e.g. auth-service, user-service, etc.)
ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

# Copy the generated JAR from the builder stage
COPY --from=builder /build/${SERVICE_NAME}/target/*.jar app.jar

# Define default entrypoint
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
