FROM eclipse-temurin:21-jre

WORKDIR /app

COPY iam-audit-server/target/*.jar app.jar

EXPOSE 9003 9004

ENTRYPOINT ["java", "-jar", "app.jar"]
