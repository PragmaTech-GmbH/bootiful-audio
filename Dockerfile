FROM amazoncorretto:23.0.2-alpine

WORKDIR /app

COPY target/bootiful-audio.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
