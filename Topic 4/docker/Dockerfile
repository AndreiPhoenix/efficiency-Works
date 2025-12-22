# Dockerfile
# Этап сборки
FROM maven:3.9.4-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Этап выполнения
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring

# Устанавливаем curl для healthcheck
RUN apk add --no-cache curl

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY entrypoint.sh .

RUN chown -R spring:spring /app && chmod +x entrypoint.sh

USER spring:spring

EXPOSE 8080 9090

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["./entrypoint.sh"]