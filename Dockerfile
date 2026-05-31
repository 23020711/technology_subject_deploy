# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy backend pom.xml and source
COPY backend/pom.xml ./pom.xml
COPY backend/src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Add non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the built jar
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
