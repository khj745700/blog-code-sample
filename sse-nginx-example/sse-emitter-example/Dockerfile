FROM gradle:8.9.0-jdk17 AS builder
WORKDIR /sse-emitter-example
COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew bootJar

FROM amazoncorretto:17

COPY --from=builder /sse-emitter-example/build/libs/*.jar /app.jar

EXPOSE 8080

CMD ["java", "-jar", "/app.jar"]