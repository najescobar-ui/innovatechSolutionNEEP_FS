# syntax=docker/dockerfile:1.7
#
# Multi-stage build for eureka-server.
# Build context: repository ROOT. In docker-compose.yml:
#   build:
#     context: .
#     dockerfile: eureka-server/Dockerfile
#
# Uses BuildKit cache-mount on ~/.m2 so dependency downloads survive
# across builds (no re-download of the Spring Cloud BOM tree each time).

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# Copy the whole repo (filtered by .dockerignore).
# Reason: the parent POM declares sibling modules, and Maven needs
# every <module> path to exist on disk before it can scope to one.
COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl eureka-server -am package -DskipTests -B

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/eureka-server/target/eureka-server.jar app.jar

# Usuario no root (buena práctica de seguridad: el contenedor no corre como root)
RUN groupadd --system spring && useradd --system --gid spring spring \
    && chown -R spring:spring /app
USER spring

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
