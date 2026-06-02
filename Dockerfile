FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Create upload directory
RUN mkdir -p /app/uploads
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]