# Maven Build
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run jar
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Spring Boot default port
EXPOSE 8080

# Command to run application
ENTRYPOINT ["java", "-jar", "app.jar"]
