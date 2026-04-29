# === Build stage ===
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copy Maven wrapper and POM first (layer caching for dependencies)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:resolve -q

# Copy source and build
COPY src/ src/
RUN ./mvnw clean package -DskipTests -q

# === Runtime stage ===
FROM eclipse-temurin:25-jre

WORKDIR /app

# Create data directory for H2 file-based storage
RUN mkdir -p /data

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS=""

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
