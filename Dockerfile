# ---- Etapa 1: Build con Gradle ----
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

# Copiamos los archivos necesarios para resolver dependencias
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Descargamos dependencias para aprovechar la caché
RUN ./gradlew dependencies

# Copiamos el resto del código fuente
COPY . .

# 🔹 Ahora damos permiso de ejecución al gradlew (DESPUÉS del último COPY)
RUN chmod +x gradlew

# Compilamos el proyecto y generamos el .jar (sin correr tests)
RUN ./gradlew clean build -x test


# ---- Etapa 2: Imagen final liviana ----
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Crea un usuario no-root por seguridad
RUN useradd -ms /bin/bash spring
USER spring

# Copia solo el jar generado
COPY --from=builder /app/build/libs/BackendE_Commerce-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
