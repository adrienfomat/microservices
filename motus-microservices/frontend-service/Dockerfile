# Build stage: compile the module and produce the executable jar.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY . .
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -pl frontend-service -am -DskipTests clean package

# Runtime stage: run the jar only.
FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY --from=build /app/frontend-service/target/frontend-service-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
