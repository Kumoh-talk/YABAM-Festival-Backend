# application/config/Dockerfile
FROM eclipse-temurin:17-jre          # ← alpine 대신 jre (multi-arch)
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java","-jar","/app/app.jar"]
