# syntax=docker/dockerfile:1.7
#
# Multi-stage build for ms-resources.
# Build context: backend/. In docker-compose.yml:
#   build:
#     context: .
#     dockerfile: services/ms-resources/Dockerfile

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl services/ms-resources -am package -DskipTests -B

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/services/ms-resources/target/ms-resources.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
