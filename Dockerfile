# ---- Etapa 1: Build con Gradle ----
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

# Copiamos los archivos necesarios para resolver dependencias
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# 🔹 Damos permiso de ejecución al wrapper (soluciona el error de "Permission denied")
RUN chmod +x gradlew

# 🔹 Descargamos dependencias (mejora la caché de Docker)
RUN ./gradlew dependencies

# Copiamos el resto del código fuente
COPY . .

# 🔹 Compilamos el proyecto y generamos el .jar (sin correr tests)
RUN ./gradlew clean build -x test


# ---- Etapa 2: Imagen final liviana ----
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# 🔹 Crea un usuario no-root por seguridad
RUN useradd -ms /bin/bash spring
USER spring

# 🔹 Copia solo el jar generado (evita copiar archivos innecesarios)
COPY --from=builder /app/build/libs/BackendE_Commerce-0.0.1-SNAPSHOT.jar app.jar

# 🔹 Render usa $PORT automáticamente, así que lo exponemos
EXPOSE 8080

# 🔹 Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]
