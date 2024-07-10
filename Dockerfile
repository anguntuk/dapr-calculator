# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Spring Boot jar file into the container
COPY target/CalculatorSampleApp-0.0.1-SNAPSHOT.jar app.jar
COPY components /app/components

# Install curl and Dapr CLI
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://raw.githubusercontent.com/dapr/cli/master/install/install.sh | /bin/bash && \
    mv /usr/local/bin/daprd  /root/.dapr/bin/daprd && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Expose the Spring Boot port and Dapr ports
EXPOSE 8080 3500 50001

# Command to run both Dapr and the Spring Boot application
CMD ["sh", "-c", "dapr run --app-id calculator-app --app-port 8080 --dapr-http-port 3500 --dapr-grpc-port 50001 --resources-path ./components -- java -jar /app/app.jar"]
