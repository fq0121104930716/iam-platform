FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY iam-admin-server/target/*.jar app.jar

EXPOSE 9002 9006

ENTRYPOINT ["java", "-jar", "app.jar"]
