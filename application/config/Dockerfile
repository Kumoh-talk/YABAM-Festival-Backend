# application/config/Dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java","-Duser.timezone=Asia/Seoul","-jar","/app/app.jar"]
