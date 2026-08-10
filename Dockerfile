FROM ubuntu:latest
LABEL authors="trouli"

ENTRYPOINT ["top", "-b"]

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 10000

CMD ["sh", "-c", "java -jar app.jar"]