FROM amazoncorretto:11.0.20-alpine3.17

# Set the working directory inside the container
WORKDIR /app


# Install sbt (if not already installed)
RUN apk update && apk add bash
RUN wget https://github.com/sbt/sbt/releases/download/v1.10.0/sbt-1.10.0.tgz
RUN tar -xvzf sbt-1.10.0.tgz

# Copy the JAR file into the container
COPY FusionSRE-1.0-SNAPSHOT.jar /app

# Expose the port your application listens on (if needed)
EXPOSE 9000

CMD ["java", "-jar", "/app/FusionSRE-1.0-SNAPSHOT.jar"]
