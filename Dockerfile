# 1. Build mühiti
FROM gradle:8.5-jdk21-alpine AS build
COPY . /app
WORKDIR /app
# İcazəni təmin etmək üçün
RUN chmod +x gradlew
RUN ./gradlew clean build -DskipTests

# 2. Çalışdırma mühiti
FROM eclipse-temurin:21-jre-alpine
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]