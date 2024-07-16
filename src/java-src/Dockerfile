FROM openjdk:latest

WORKDIR /usr/src/myapp
COPY . .

RUN mvn build
CMD ["java", "-jar", "target/bubbling_dataplane.jar"]
