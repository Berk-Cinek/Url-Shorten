FROM eclipse-temurin:26-jdk AS build
WORKDIR /app
COPY gradle gradle/
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon
COPY src src/
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:26-jre
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring
COPY --chown=spring:spring --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
