# Build stage
FROM maven:3.9.9-amazoncorretto-23-debian-bookworm AS build

WORKDIR /app

COPY pom.xml .

# Download all required dependencies into one layer
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

# Run stage
FROM amazoncorretto:23.0.2-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar bootiful-audio.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "bootiful-audio.jar"]
