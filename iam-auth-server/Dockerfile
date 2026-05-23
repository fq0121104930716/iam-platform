FROM eclipse-temurin:21-jre

WORKDIR /app

COPY iam-auth-server/target/*.jar app.jar

EXPOSE 9001 9005

ENTRYPOINT ["java", "-jar", "app.jar"]
