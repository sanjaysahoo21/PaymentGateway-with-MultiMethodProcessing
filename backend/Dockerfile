# Multi-stage build Dockerfile for Spring Boot Payment Gateway API
# Stage 1: Build JAR file using Maven
# Stage 2: Run lightweight Java 17 JRE container with just the application

# ===== Build Stage =====
# Use Maven 3.9.6 with Java 17 for compilation
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
# Copy source code and compile
COPY src ./src
# Build JAR without running tests for faster builds
RUN mvn -q -B package -DskipTests

# ===== Runtime Stage =====
# Use lightweight Java 17 JRE (not full JDK)
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy compiled JAR from build stage
COPY --from=build /app/target/payment-gateway-0.0.1-SNAPSHOT.jar app.jar
# Expose API port
EXPOSE 8000
# Set port environment variable (can be overridden)
ENV PORT=8000
# Run Spring Boot application
ENTRYPOINT ["java","-jar","/app/app.jar"]
