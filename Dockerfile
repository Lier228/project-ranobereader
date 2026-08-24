FROM amazoncorretto:21-alpine-jdk

MAINTAINER madiar

COPY backendDockerTest.jar my-backend-spring.jar

ENTRYPOINT ["java", "-jar", "my-backend-spring.jar"]