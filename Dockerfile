# 1단계: 빌드 스테이지 (Gradle 빌드)
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon

# 2단계: 실행 스테이지 (초경량 JRE 이미지 사용)
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080

# 백엔드 기동
ENTRYPOINT ["java", "-jar", "app.jar"]
