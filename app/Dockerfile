# ================================================================
# STAGE 1: BUILD STAGE
# Purpose: Compile Java source code into a JAR file
# This stage is DISCARDED after build — not in final image
# ================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory inside the container
WORKDIR /build

# Copy Maven wrapper and pom.xml FIRST (separate layer)
# Why? Docker caches layers. If pom.xml hasn't changed,
# Docker reuses the cached dependency download layer.
# This makes rebuilds much faster.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make mvnw executable
RUN chmod +x mvnw

# Download all dependencies (cached layer if pom.xml unchanged)
RUN ./mvnw dependency:go-offline -B

# Now copy source code (changes more often than pom.xml)
COPY src ./src

# Build the JAR, skip tests (tests run in CI pipeline separately)
RUN ./mvnw clean package -DskipTests -B

# ================================================================
# STAGE 2: RUNTIME STAGE
# Purpose: Run the JAR file
# This is the FINAL image that gets deployed
# ================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security best practice: never run as root
# Create a dedicated user for the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy ONLY the JAR from the builder stage
# Nothing else from builder comes with it
COPY --from=builder /build/target/*.jar app.jar

# Change ownership to our non-root user
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

# Document which port the app uses (informational)
EXPOSE 8083

# Health check — Docker will monitor this
# If it fails 3 times, container is marked unhealthy
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget -q --spider http://localhost:8083/actuator/health || exit 1

# Run the application
# Using exec form (JSON array) — not shell form
# This ensures signals (like Ctrl+C / docker stop) reach Java properly
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
