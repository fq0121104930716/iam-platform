FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY iam-bff-server/target/*.jar app.jar

EXPOSE 9010

ENTRYPOINT ["java", "-jar", "app.jar"]
