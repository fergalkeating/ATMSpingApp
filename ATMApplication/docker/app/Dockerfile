ARG OPENJDK_VERSION=11.0.11-jre-slim
FROM openjdk:${OPENJDK_VERSION} as builder
WORKDIR app
ARG JAR_FILE=files/ATMApplication/ATMApplication.jar
COPY ${JAR_FILE} atmapplication.jar
RUN java -Djarmode=layertools -jar atmapplication.jar extract

FROM openjdk:${OPENJDK_VERSION}
WORKDIR app
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
RUN true
COPY --from=builder /app/application/ ./
ENTRYPOINT ["java", "-Djdk.tls.client.protocols=TLSv1.2", "org.springframework.boot.loader.JarLauncher"]
