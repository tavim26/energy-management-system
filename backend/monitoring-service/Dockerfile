FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY src ./src

# Tests are skipped because the existing ones start the whole application
# and need the databases and RabbitMQ to be running
RUN --mount=type=cache,target=/root/.m2 mvn -B package -DskipTests

# Runtime stage: only a JRE and the built jar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
COPY --from=build /build/target/*.jar app.jar
USER app

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]