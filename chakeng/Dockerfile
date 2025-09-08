FROM ubuntu:latest

WORKDIR /app
COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew clean build -x test -x asciidoctor

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar /app/app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]
