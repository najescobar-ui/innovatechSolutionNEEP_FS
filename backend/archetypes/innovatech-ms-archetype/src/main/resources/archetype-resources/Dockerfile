# syntax=docker/dockerfile:1.7
#
# Multi-stage build for ${artifactId} (standalone).
# Build context: this microservice's own directory.

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/target/${artifactId}.jar app.jar

# Non-root user (security best practice: the container does not run as root).
RUN groupadd --system spring && useradd --system --gid spring spring \
    && chown -R spring:spring /app
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
