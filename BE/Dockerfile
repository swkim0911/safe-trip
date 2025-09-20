# 1단계: 빌드
FROM gradle:8.9-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# 2단계: 실행
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
