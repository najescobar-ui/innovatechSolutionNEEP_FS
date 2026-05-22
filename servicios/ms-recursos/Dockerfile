# syntax=docker/dockerfile:1.7
#
# Multi-stage build for ms-recursos.
# Build context: repository ROOT. In docker-compose.yml:
#   build:
#     context: .
#     dockerfile: servicios/ms-recursos/Dockerfile

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl servicios/ms-recursos -am package -DskipTests -B

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/servicios/ms-recursos/target/ms-recursos.jar app.jar

# Usuario no root (buena práctica de seguridad: el contenedor no corre como root)
RUN groupadd --system spring && useradd --system --gid spring spring \
    && chown -R spring:spring /app
USER spring


EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
