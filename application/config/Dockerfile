# application/config/Dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app
COPY build/libs/*.jar app.jar     # ← 컨텍스트 내부 경로만!
EXPOSE 8888
ENTRYPOINT ["java","-jar","/app/app.jar"]
