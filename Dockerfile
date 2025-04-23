# Stage 1: Build the native executable using GraalVM
FROM ghcr.io/graalvm/jdk-community:24 AS builder

WORKDIR /workspace

# Copy Maven wrapper and pom.xml first for dependency caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Grant execute permission to mvnw
RUN chmod +x mvnw

# Download dependencies (using resolve avoids compiling anything yet)
# This layer is cached as long as pom.xml doesn't change
RUN ./mvnw dependency:resolve

# Copy the rest of the application source code
COPY src ./src

# Build the native executable using the 'native' profile
# -DskipTests is common in Docker builds unless tests are specifically designed for it
# Ensure your pom.xml's native profile is correctly configured
RUN   ./mvnw -Pnative -DskipTests spring-boot:build-image

# --- Stage 2: Create the minimal runtime image ---
FROM alpine:latest

# Install glibc compatibility layer for Alpine
# Needed because the native image is likely linked against glibc
RUN apk update && apk add --no-cache libc6-compat

WORKDIR /app

# Create a non-root user and group for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the native executable from the builder stage
# Adjust the path if your artifactId in pom.xml is different
COPY --from=builder /workspace/target/bootiful-audio .

# Ensure the executable is owned by the non-root user
RUN chown appuser:appgroup bootiful-audio

# Switch to the non-root user
USER appuser

# Expose the application port
EXPOSE 8080

# Command to run the application
# The executable is now in the current directory (/app)
CMD ["./bootiful-audio"]
