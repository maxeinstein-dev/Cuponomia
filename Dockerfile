# === Build stage ===
FROM eclipse-temurin:25-jdk AS build

ARG MODULE=coupon-management-service
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY coupon-contracts/pom.xml coupon-contracts/pom.xml
COPY coupon-management-service/pom.xml coupon-management-service/pom.xml
COPY coupon-validation-service/pom.xml coupon-validation-service/pom.xml

COPY coupon-contracts/src/ coupon-contracts/src/
COPY coupon-management-service/src/ coupon-management-service/src/
COPY coupon-validation-service/src/ coupon-validation-service/src/

RUN chmod +x mvnw && ./mvnw -pl ${MODULE} -am clean package -DskipTests -q

# === Runtime stage ===
FROM eclipse-temurin:25-jre

ARG MODULE=coupon-management-service
WORKDIR /app

COPY --from=build /app/${MODULE}/target/*.jar app.jar

EXPOSE 8081 8082

ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
