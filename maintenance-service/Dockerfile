FROM gradle:9.4.1-jdk21-alpine AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle bootJar -x test --no-daemon && \
    cp build/libs/$(ls build/libs | grep -v plain | head -n 1) /app/app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget
COPY --from=builder /app/app.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
