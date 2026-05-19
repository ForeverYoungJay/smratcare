# syntax=docker/dockerfile:1.7
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests -Dmaven.repo.local=/root/.m2/repository dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests -Dmaven.repo.local=/root/.m2/repository package
RUN cp /build/target/*.jar /build/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /build/app.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
