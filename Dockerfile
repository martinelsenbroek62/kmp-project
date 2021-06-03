FROM openjdk:8-alpine

WORKDIR /app

ADD ./target/aiml-service-0.0.1-SNAPSHOT.jar /app

CMD  ["java", "-jar", "aiml-service-0.0.1-SNAPSHOT.jar"]
