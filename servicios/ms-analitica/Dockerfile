# syntax=docker/dockerfile:1.7

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl servicios/ms-analitica -am package -DskipTests -B

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/servicios/ms-analitica/target/ms-analitica.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
