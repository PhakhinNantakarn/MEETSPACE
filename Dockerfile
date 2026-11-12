# MEETSPACE — Spring Boot 3.3.5 / Java 17
# Build the executable Spring Boot JAR using Maven.
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package

# Run with a smaller Java 17 runtime image.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/target/*.jar /app/app.jar

# H2 file database requires a writable directory.
# NOTE: Cloud Run's local filesystem is EPHEMERAL; data is not durable.
RUN mkdir -p /app/data && chown -R 10001:10001 /app
USER 10001:10001

ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Bangkok -XX:MaxRAMPercentage=75.0"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
