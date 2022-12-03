FROM openjdk:11 
VOLUME /tmp 
VOLUME /conf 
VOLUME /logs 
COPY target/game-session-service-1.0-SNAPSHOT.jar game-session-service.jar 
ENTRYPOINT ["java", "-Dspring.config.location=/conf/game-session-service.properties", "-jar", "/game-session-service.jar"] 
