# 1단계: 빌드 스테이지 (Gradle 빌드)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon

# 2단계: 실행 스테이지 (초경량 JRE 이미지 사용)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080

# 백엔드 기동
ENTRYPOINT ["java", "-jar", "app.jar"]
