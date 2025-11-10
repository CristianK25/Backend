FROM gradle:8.7-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle --no-daemon clean build -x test

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
RUN useradd -ms /bin/bash spring
USER spring
COPY --from=builder /app/build/libs/BackendE_Commerce-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
