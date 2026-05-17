FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY iam-gateway/target/*.jar app.jar

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "app.jar"]
