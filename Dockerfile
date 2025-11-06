# ---- Etapa 1: Build con Gradle ----
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# ---- Etapa 2: Imagen final liviana ----
FROM openjdk:21-jdk-slim
WORKDIR /app
# Copia solo el jar generado (el correcto, no el -plain)
COPY --from=builder /app/build/libs/BackendE_Commerce-0.0.1-SNAPSHOT.jar app.jar

# Render setea la variable de entorno $PORT automáticamente
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]