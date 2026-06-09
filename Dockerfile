# Use Java runtime
FROM eclipse-temurin:21-jdk

# Copy jar file
COPY target/stockportfolioalerts-0.0.1-SNAPSHOT.jar app.jar

# Run application
ENTRYPOINT ["java", "-jar", "/app.jar"]