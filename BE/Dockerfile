# 1단계: 빌드
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# gradle wrapper 및 의존성 캐싱
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon || return 0

# 실제 소스 복사 후 빌드
COPY . .
RUN ./gradlew bootJar --no-daemon

# 2단계: 실행
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
