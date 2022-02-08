FROM openjdk:8-jre-alpine
WORKDIR /app/
COPY build/libs/*.jar app.jar
CMD ["java", "-Dlogstash.url=logstash.paas-9259d4fb-5313-4c32-ac42-558f9c550794-caas.svc.cluster.local:8088", "-jar", "app.jar"]
