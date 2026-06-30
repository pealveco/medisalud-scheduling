FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY . .

RUN chmod +x ./gradlew \
    && ./gradlew :app-service:bootJar --no-daemon --no-configuration-cache -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /workspace/applications/app-service/build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

USER app

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
