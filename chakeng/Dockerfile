# --- Build stage ---
FROM gradle:8.7-jdk21 AS builder
WORKDIR /workspace
COPY . .
# wrapper 실행 권한
RUN chmod +x ./gradlew || true
# Spring REST Docs 쓰면 -x asciidoctor 유지, 아니면 빼도 됨
RUN ./gradlew clean bootJar -x test -x asciidoctor --no-daemon

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
ENV TZ=Asia/Seoul
WORKDIR /app
# 빌드 산출물 복사 (단일 모듈 기준: build/libs/*.jar)
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080
# 프로필 하나만 쓸 거면 굳이 지정하지 않음
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
