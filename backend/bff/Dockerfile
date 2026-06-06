# syntax=docker/dockerfile:1.7
#
# Multi-stage build for bff.
# Build context: repository ROOT. In docker-compose.yml:
#   build:
#     context: .
#     dockerfile: bff/Dockerfile

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl bff -am package -DskipTests -B

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/bff/target/bff.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
