FROM maven:3.8-amazoncorretto-17 as maven_builder
RUN mkdir -p /app
WORKDIR /app
ADD pom.xml /app
RUN mvn -B dependency:resolve dependency:resolve-plugins
ADD src /app/src
RUN mvn package
