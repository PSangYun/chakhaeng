# --- Build stage ---
FROM gradle:8.7-jdk21 AS builder
WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew || true
# asciidoctor 태스크가 없다면 제외 옵션 빼기
RUN ./gradlew clean bootJar -x test --no-daemon

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
ENV TZ=Asia/Seoul
WORKDIR /app
# 단일 모듈 기준: build/libs/*.jar
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
