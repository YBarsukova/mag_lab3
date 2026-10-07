FROM maven:3.10.0-eclipse-temurin-27 AS build

WORKDIR /build

COPY pom.xml .

RUN mvn -q dependency:go-offline

COPY src ./src

RUN mvn -q -DskipTests package


FROM eclipse-temurin:27-jre-alpine

WORKDIR /app

RUN mkdir -p /app/logs

COPY --from=build /build/target/app.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]