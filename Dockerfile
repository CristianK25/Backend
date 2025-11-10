# ---- Etapa 1: Build con Gradle ----
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies
COPY . .
RUN ./gradlew clean build -x test

# ---- Etapa 2: Imagen final liviana ----
FROM openjdk:21-jdk-slim
WORKDIR /app

# Crea un usuario no-root por seguridad
RUN useradd -ms /bin/bash spring
USER spring

# Copia solo el jar generado (evita el -plain)
COPY --from=builder /app/build/libs/BackendE_Commerce-0.0.1-SNAPSHOT.jar app.jar

# Render asigna el puerto mediante $PORT
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]
