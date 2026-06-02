# Build mühiti
FROM gradle:8.5-jdk21-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Gradle wrapper-ə icazə veririk və build-i işə salırıq
RUN chmod +x gradlew && ./gradlew clean build -x test

# Run mühiti
FROM eclipse-temurin:21-jre-alpine
# Build qovluğundan yaradılmış jar faylını kopyalayırıq
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]