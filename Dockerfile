# Build mərhələsi: Gradle ilə build edirik
FROM gradle:8.5-jdk21 AS build
COPY . .
RUN ./gradlew clean build -DskipTests

# Run mərhələsi: Java 21 image-i
FROM eclipse-temurin:21-jre-alpine
COPY --from=build /home/gradle/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]