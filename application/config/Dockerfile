# application/config/Dockerfile
# Use Eclipse Temurin JRE 17 (multi-arch: arm64/amd64)
FROM eclipse-temurin:17-jre

WORKDIR /app
COPY build/libs/*.jar app.jar

EXPOSE 8888
ENTRYPOINT ["java","-jar","/app/app.jar"]
