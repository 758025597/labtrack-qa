# syntax=docker/dockerfile:1
# ------------------------------------------------------------
# LabTrack – imagen para Render (https://render.com)
# Java 17: coincide con <java.version> del pom.xml.
# Render inyecta la variable PORT; la app la lee en application.properties
# (server.port=${PORT:8080}).
# ------------------------------------------------------------

# Etapa 1: compilar con Maven + JDK 17
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Primero el pom para aprovechar la caché de dependencias entre builds
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# Etapa 2: imagen ligera solo con el JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Ejecutar sin privilegios de root
RUN addgroup -S labtrack && adduser -S labtrack -G labtrack
COPY --from=build /app/target/labtrack-*.jar app.jar
RUN chown labtrack:labtrack app.jar
USER labtrack

ENV PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
