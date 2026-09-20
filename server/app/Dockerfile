FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew :app:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /workspace/app/build/libs/*.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
